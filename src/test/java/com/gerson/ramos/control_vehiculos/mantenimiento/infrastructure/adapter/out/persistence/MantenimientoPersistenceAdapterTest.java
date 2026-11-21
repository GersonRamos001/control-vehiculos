package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.TestcontainersConfiguration;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.LocalidadRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.RutinaMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.TipoUsoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.out.MantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Integración contra MySQL real (Testcontainers): migración V4, mapeo JPA y consultas de "último mantenimiento". */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class MantenimientoPersistenceAdapterTest {

	private static final LocalDate HOY = LocalDate.of(2026, 11, 21);

	@Autowired
	MantenimientoRepositoryPort mantenimientos;

	@Autowired
	VehiculoRepositoryPort vehiculos;

	@Autowired
	LocalidadRepositoryPort localidades;

	@Autowired
	TipoUsoRepositoryPort tiposUso;

	@Autowired
	RutinaMantenimientoRepositoryPort rutinas;

	private Vehiculo vehiculo;
	private RutinaMantenimientoId rutina10k;
	private RutinaMantenimientoId rutina20k;

	@BeforeEach
	void preparar() {
		Localidad localidad = localidades.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "San Pedro Sula"));
		var operativo = tiposUso.buscarPorCodigo(new CodigoCatalogo("OPERATIVO")).orElseThrow();
		vehiculo = vehiculos.guardar(Vehiculo.registrar(new Placa("ABC-123"), new CodigoEquipo("EQ-001"), localidad.id(), operativo.id()));
		rutina10k = rutinas.buscarPorCodigo(new CodigoCatalogo("10K")).orElseThrow().id();
		rutina20k = rutinas.buscarPorCodigo(new CodigoCatalogo("20K")).orElseThrow().id();
	}

	private Mantenimiento guardar(VehiculoId vehiculoId, LocalDate fecha, int km, RutinaMantenimientoId rutina, String observaciones) {
		return mantenimientos.guardar(Mantenimiento.registrar(vehiculoId, rutina, fecha, km, observaciones, HOY));
	}

	@Test
	void guarda_y_recupera_el_historial_del_mas_reciente_al_mas_antiguo() {
		guardar(vehiculo.id(), LocalDate.of(2026, 1, 10), 90_000, rutina10k, "primero");
		Mantenimiento ultimo = guardar(vehiculo.id(), LocalDate.of(2026, 7, 10), 100_000, rutina20k, null);

		assertThat(ultimo.id()).isNotNull();
		assertThat(mantenimientos.historialDe(vehiculo.id())).extracting(Mantenimiento::kilometraje).containsExactly(100_000, 90_000);
		assertThat(mantenimientos.historialDe(vehiculo.id()).get(1).observaciones()).isEqualTo("primero");
		assertThat(mantenimientos.historialDe(vehiculo.id()).get(0).rutinaId()).isEqualTo(rutina20k);
	}

	@Test
	void el_ultimo_se_decide_por_fecha_y_luego_por_id() {
		guardar(vehiculo.id(), LocalDate.of(2026, 7, 10), 100_000, rutina10k, null);
		guardar(vehiculo.id(), LocalDate.of(2026, 1, 10), 90_000, rutina10k, null); // insertado después, pero más antiguo
		Mantenimiento mismaFecha = guardar(vehiculo.id(), LocalDate.of(2026, 7, 10), 100_000, rutina20k, null);

		assertThat(mantenimientos.ultimoDe(vehiculo.id())).hasValueSatisfying(m -> assertThat(m.id()).isEqualTo(mismaFecha.id()));
	}

	@Test
	void sin_mantenimientos_no_hay_ultimo() {
		assertThat(mantenimientos.ultimoDe(vehiculo.id())).isEmpty();
		assertThat(mantenimientos.historialDe(vehiculo.id())).isEmpty();
	}

	@Test
	void el_ultimo_de_cada_vehiculo_devuelve_uno_por_vehiculo() {
		Vehiculo otro = vehiculos.guardar(Vehiculo.registrar(new Placa("DEF-456"), new CodigoEquipo("EQ-002"),
				vehiculo.localidadId(), vehiculo.tipoUsoId()));
		Vehiculo sinMantenimientos = vehiculos.guardar(Vehiculo.registrar(new Placa("GHI-789"), new CodigoEquipo("EQ-003"),
				vehiculo.localidadId(), vehiculo.tipoUsoId()));
		guardar(vehiculo.id(), LocalDate.of(2026, 1, 10), 90_000, rutina10k, null);
		Mantenimiento ultimoPrimero = guardar(vehiculo.id(), LocalDate.of(2026, 7, 10), 100_000, rutina20k, null);
		Mantenimiento ultimoOtro = guardar(otro.id(), LocalDate.of(2026, 3, 5), 40_000, rutina10k, null);

		var ultimos = mantenimientos.ultimosDeTodosLosVehiculos();

		assertThat(ultimos).extracting(Mantenimiento::id).containsExactlyInAnyOrder(ultimoPrimero.id(), ultimoOtro.id());
		assertThat(ultimos).extracting(Mantenimiento::vehiculoId).doesNotContain(sinMantenimientos.id());
	}

	@Test
	void realizados_entre_incluye_ambos_limites_y_ordena_del_mas_reciente_al_mas_antiguo() {
		Vehiculo otro = vehiculos.guardar(Vehiculo.registrar(new Placa("DEF-456"), new CodigoEquipo("EQ-002"),
				vehiculo.localidadId(), vehiculo.tipoUsoId()));
		guardar(vehiculo.id(), LocalDate.of(2026, 2, 28), 80_000, rutina10k, "fuera del rango");
		Mantenimiento enElInicio = guardar(vehiculo.id(), LocalDate.of(2026, 3, 1), 90_000, rutina10k, null);
		Mantenimiento delOtro = guardar(otro.id(), LocalDate.of(2026, 6, 15), 40_000, rutina20k, null);
		Mantenimiento enElFinal = guardar(vehiculo.id(), LocalDate.of(2026, 9, 1), 100_000, rutina20k, null);
		guardar(vehiculo.id(), LocalDate.of(2026, 9, 2), 101_000, rutina10k, "fuera del rango");

		var realizados = mantenimientos.realizadosEntre(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 1));

		assertThat(realizados).extracting(Mantenimiento::id).containsExactly(enElFinal.id(), delOtro.id(), enElInicio.id());
	}

	@Test
	void realizados_entre_con_la_misma_fecha_ordena_por_id_descendente() {
		Mantenimiento primero = guardar(vehiculo.id(), LocalDate.of(2026, 5, 1), 90_000, rutina10k, null);
		Mantenimiento segundo = guardar(vehiculo.id(), LocalDate.of(2026, 5, 1), 90_000, rutina20k, null);

		var realizados = mantenimientos.realizadosEntre(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 1));

		assertThat(realizados).extracting(Mantenimiento::id).containsExactly(segundo.id(), primero.id());
	}

	@Test
	void no_se_puede_guardar_de_nuevo_un_mantenimiento_ya_persistido() {
		Mantenimiento guardado = guardar(vehiculo.id(), LocalDate.of(2026, 1, 10), 90_000, rutina10k, null);

		assertThatThrownBy(() -> mantenimientos.guardar(guardado)).isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void la_base_de_datos_rechaza_un_vehiculo_inexistente() {
		assertThatThrownBy(() -> guardar(new VehiculoId(999_999), LocalDate.of(2026, 1, 10), 90_000, rutina10k, null))
			.isInstanceOf(DataIntegrityViolationException.class);
	}
}
