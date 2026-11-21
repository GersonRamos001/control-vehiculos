package com.gerson.ramos.control_vehiculos.vehiculo.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.TestcontainersConfiguration;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.LocalidadRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.RutinaMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.TipoUsoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

/** Integración contra MySQL real (Testcontainers). Cada test corre en una transacción que se revierte. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class VehiculoPersistenceAdapterTest {

	@Autowired
	VehiculoRepositoryPort vehiculos;

	@Autowired
	LocalidadRepositoryPort localidades;

	@Autowired
	TipoUsoRepositoryPort tiposUso;

	@Autowired
	RutinaMantenimientoRepositoryPort rutinas;

	private Localidad localidad;
	private TipoUso operativo;

	@BeforeEach
	void prepararCatalogos() {
		localidad = localidades.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "San Pedro Sula"));
		operativo = tiposUso.buscarPorCodigo(new CodigoCatalogo("OPERATIVO")).orElseThrow();
	}

	private Vehiculo nuevo(String placa, String codigoEquipo) {
		return Vehiculo.registrar(new Placa(placa), new CodigoEquipo(codigoEquipo), localidad.id(), operativo.id());
	}

	@Test
	void guarda_y_recupera_un_vehiculo() {
		Vehiculo guardado = vehiculos.guardar(nuevo("abc-123", "eq-001"));

		assertThat(guardado.id()).isNotNull();
		assertThat(guardado.version()).isZero();
		assertThat(guardado.placa().valor()).isEqualTo("ABC-123");
		assertThat(guardado.activo()).isTrue();
		assertThat(guardado.ultimaRutinaId()).isNull();

		Vehiculo leido = vehiculos.buscarPorId(guardado.id()).orElseThrow();
		assertThat(leido.codigoEquipo().valor()).isEqualTo("EQ-001");
		assertThat(leido.localidadId()).isEqualTo(localidad.id());
		assertThat(leido.tipoUsoId()).isEqualTo(operativo.id());
	}

	@Test
	void busca_por_placa_y_verifica_existencia() {
		vehiculos.guardar(nuevo("ABC-123", "EQ-001"));

		assertThat(vehiculos.buscarPorPlaca(new Placa("abc-123"))).isPresent();
		assertThat(vehiculos.existePorPlaca(new Placa("ABC-123"))).isTrue();
		assertThat(vehiculos.existePorPlaca(new Placa("ZZZ-999"))).isFalse();
		assertThat(vehiculos.existePorCodigoEquipo(new CodigoEquipo("EQ-001"))).isTrue();
		assertThat(vehiculos.existePorCodigoEquipo(new CodigoEquipo("EQ-777"))).isFalse();
	}

	@Test
	void rechaza_placa_duplicada() {
		vehiculos.guardar(nuevo("ABC-123", "EQ-001"));

		assertThatThrownBy(() -> vehiculos.guardar(nuevo("ABC-123", "EQ-002"))).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rechaza_codigo_de_equipo_duplicado() {
		vehiculos.guardar(nuevo("ABC-123", "EQ-001"));

		assertThatThrownBy(() -> vehiculos.guardar(nuevo("XYZ-999", "EQ-001"))).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rechaza_localidad_inexistente() {
		Vehiculo huerfano = Vehiculo.registrar(new Placa("ABC-123"), new CodigoEquipo("EQ-001"), new LocalidadId(999_999),
				operativo.id());

		assertThatThrownBy(() -> vehiculos.guardar(huerfano)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void desactivar_es_baja_logica() {
		Vehiculo guardado = vehiculos.guardar(nuevo("ABC-123", "EQ-001"));
		guardado.desactivar();
		vehiculos.guardar(guardado);

		assertThat(vehiculos.listarActivos()).isEmpty();
		assertThat(vehiculos.listarTodos()).hasSize(1);
		assertThat(vehiculos.buscarPorId(guardado.id()).orElseThrow().activo()).isFalse();
	}

	@Test
	void registra_la_rutina_realizada() {
		RutinaMantenimiento rutina20k = rutinas.buscarPorCodigo(new CodigoCatalogo("20K")).orElseThrow();
		Vehiculo guardado = vehiculos.guardar(nuevo("ABC-123", "EQ-001"));

		guardado.registrarRutinaRealizada(rutina20k.id());
		Vehiculo actualizado = vehiculos.guardar(guardado);

		assertThat(actualizado.ultimaRutinaId()).isEqualTo(rutina20k.id());
		assertThat(actualizado.version()).isEqualTo(1L);
	}

	@Test
	void detecta_modificacion_concurrente_por_version_obsoleta() {
		Localidad otra = localidades.guardar(Localidad.crear(new CodigoCatalogo("TGU"), "Tegucigalpa"));
		Vehiculo guardado = vehiculos.guardar(nuevo("ABC-123", "EQ-001"));
		Vehiculo copiaA = vehiculos.buscarPorId(guardado.id()).orElseThrow();
		Vehiculo copiaB = vehiculos.buscarPorId(guardado.id()).orElseThrow();

		copiaA.reasignarLocalidad(otra.id());
		vehiculos.guardar(copiaA);

		copiaB.desactivar();
		assertThatThrownBy(() -> vehiculos.guardar(copiaB)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
	}
}
