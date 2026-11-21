package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.TestcontainersConfiguration;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.LocalidadRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.ParametrosAlertasRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.ParametrosMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.RutinaMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.TipoUsoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;

/** Integración contra MySQL real (Testcontainers): migraciones Flyway + validación del mapeo JPA + adaptadores. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CatalogosPersistenceTest {

	@Autowired
	LocalidadRepositoryPort localidades;

	@Autowired
	TipoUsoRepositoryPort tiposUso;

	@Autowired
	RutinaMantenimientoRepositoryPort rutinas;

	@Autowired
	ParametrosMantenimientoRepositoryPort parametros;

	@Autowired
	ParametrosAlertasRepositoryPort parametrosAlertas;

	@Test
	void migracion_siembra_los_umbrales_de_alerta_1000_km_30_dias_y_7_dias() {
		assertThat(parametrosAlertas.obtener()).isEqualTo(new ParametrosAlertas(1_000, 30, 7));
	}

	@Test
	void los_umbrales_de_alerta_se_actualizan() {
		parametrosAlertas.guardar(new ParametrosAlertas(1_500, 45, 10));

		assertThat(parametrosAlertas.obtener()).isEqualTo(new ParametrosAlertas(1_500, 45, 10));
	}

	@Test
	void migracion_siembra_los_parametros_de_mantenimiento_10000_km_y_6_meses() {
		assertThat(parametros.obtener()).isEqualTo(new ParametrosMantenimiento(10_000, 6));
	}

	@Test
	void los_parametros_de_mantenimiento_se_actualizan() {
		parametros.guardar(new ParametrosMantenimiento(12_000, 4));

		assertThat(parametros.obtener()).isEqualTo(new ParametrosMantenimiento(12_000, 4));
	}

	@Test
	void migracion_siembra_los_tipos_de_uso() {
		assertThat(tiposUso.listarActivos()).extracting(t -> t.codigo().valor()).containsExactlyInAnyOrder("OPERATIVO", "RELEVO");
	}

	@Test
	void migracion_siembra_las_rutinas_10k_20k_y_30k() {
		assertThat(rutinas.listarActivas()).extracting(r -> r.codigo().valor()).containsExactly("10K", "20K", "30K");
		assertThat(rutinas.buscarPorCodigo(new CodigoCatalogo("10K"))).isPresent();
	}

	@Test
	void localidad_se_crea_actualiza_y_desactiva() {
		Localidad creada = localidades.guardar(Localidad.crear(new CodigoCatalogo("sps"), "San Pedro Sula"));

		assertThat(creada.id()).isNotNull();
		assertThat(creada.codigo().valor()).isEqualTo("SPS");
		assertThat(localidades.existePorCodigo(new CodigoCatalogo("SPS"))).isTrue();

		creada.renombrar("SPS - Centro");
		creada.desactivar();
		Localidad actualizada = localidades.guardar(creada);

		assertThat(actualizada.nombre()).isEqualTo("SPS - Centro");
		assertThat(actualizada.activo()).isFalse();
		assertThat(localidades.listarActivas()).isEmpty();
		assertThat(localidades.listarTodas()).hasSize(1);
	}

	@Test
	void codigo_de_localidad_duplicado_es_rechazado() {
		localidades.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "San Pedro Sula"));

		assertThatThrownBy(() -> localidades.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "Otra")))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void tipo_de_uso_nuevo_se_persiste() {
		TipoUso creado = tiposUso.guardar(TipoUso.crear(new CodigoCatalogo("TEMPORAL"), "Temporal"));

		assertThat(creado.id()).isNotNull();
		assertThat(tiposUso.buscarPorId(creado.id())).isPresent();
	}
}
