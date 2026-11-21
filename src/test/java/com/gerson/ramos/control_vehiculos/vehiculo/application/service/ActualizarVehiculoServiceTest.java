package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeConcurrenciaException;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ActualizarVehiculoComando;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

class ActualizarVehiculoServiceTest {

	private final EscenarioVehiculos escenario = new EscenarioVehiculos();

	private ActualizarVehiculoComando comando(Vehiculo actual, String placa, String codigoEquipo, long localidadId, Long rutinaId) {
		return new ActualizarVehiculoComando(actual.id().valor(), actual.version(), placa, codigoEquipo, localidadId,
				actual.tipoUsoId().valor(), rutinaId);
	}

	@Test
	void actualiza_los_datos_y_sube_la_version() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");
		Localidad otra = escenario.localidades.guardar(Localidad.crear(new CodigoCatalogo("LCB"), "La Ceiba"));

		Vehiculo actualizado = escenario.actualizar.actualizar(
				comando(registrado, "xyz-999", "eq-002", otra.id().valor(), escenario.idRutinaActiva()));

		assertThat(actualizado.placa().valor()).isEqualTo("XYZ-999");
		assertThat(actualizado.codigoEquipo().valor()).isEqualTo("EQ-002");
		assertThat(actualizado.localidadId()).isEqualTo(otra.id());
		assertThat(actualizado.ultimaRutinaId()).isEqualTo(escenario.rutinaActiva.id());
		assertThat(actualizado.version()).isEqualTo(registrado.version() + 1);
	}

	@Test
	void permite_conservar_la_misma_placa_y_codigo() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");

		Vehiculo actualizado = escenario.actualizar
			.actualizar(comando(registrado, "abc-123", "eq-001", escenario.idLocalidadActiva(), null));

		assertThat(actualizado.placa().valor()).isEqualTo("ABC-123");
	}

	@Test
	void rechaza_placa_o_codigo_de_otro_vehiculo() {
		Vehiculo primero = escenario.registrarVehiculo("ABC-123", "EQ-001");
		escenario.registrarVehiculo("XYZ-999", "EQ-002");

		assertThatThrownBy(() -> escenario.actualizar.actualizar(comando(primero, "XYZ-999", "EQ-001", escenario.idLocalidadActiva(), null)))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("placa"));
		assertThatThrownBy(() -> escenario.actualizar.actualizar(comando(primero, "ABC-123", "EQ-002", escenario.idLocalidadActiva(), null)))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("codigoEquipo"));
	}

	@Test
	void rechaza_una_version_obsoleta() {
		Vehiculo leido = escenario.registrarVehiculo("ABC-123", "EQ-001");
		escenario.actualizar.actualizar(comando(leido, "ABC-123", "EQ-001", escenario.idLocalidadActiva(), null));

		// `leido` conserva la versión anterior: otro usuario ya guardó cambios.
		assertThatThrownBy(() -> escenario.actualizar.actualizar(comando(leido, "ABC-123", "EQ-005", escenario.idLocalidadActiva(), null)))
			.isInstanceOf(ConflictoDeConcurrenciaException.class);
	}

	@Test
	void no_permite_editar_un_vehiculo_inactivo() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");
		Vehiculo inactivo = escenario.cambiarEstado.desactivar(registrado.id().valor());

		assertThatThrownBy(() -> escenario.actualizar.actualizar(comando(inactivo, "ABC-123", "EQ-009", escenario.idLocalidadActiva(), null)))
			.isInstanceOf(ReglaDeNegocioException.class);
	}

	@Test
	void conserva_la_localidad_actual_aunque_se_haya_desactivado() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");
		Localidad sps = escenario.localidades.buscarPorId(escenario.localidadActiva.id()).orElseThrow();
		sps.desactivar();
		escenario.localidades.guardar(sps);

		Vehiculo actualizado = escenario.actualizar
			.actualizar(comando(registrado, "ABC-123", "EQ-777", escenario.idLocalidadActiva(), null));

		assertThat(actualizado.codigoEquipo().valor()).isEqualTo("EQ-777");
		assertThat(actualizado.localidadId()).isEqualTo(escenario.localidadActiva.id());
	}

	@Test
	void rechaza_cambiar_a_una_localidad_inactiva() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");

		assertThatThrownBy(() -> escenario.actualizar.actualizar(
				comando(registrado, "ABC-123", "EQ-001", escenario.localidadInactiva.id().valor(), null)))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("localidadId"));
	}

	@Test
	void sin_rutina_en_el_comando_conserva_la_registrada() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");
		Vehiculo conRutina = escenario.actualizar.actualizar(
				comando(registrado, "ABC-123", "EQ-001", escenario.idLocalidadActiva(), escenario.idRutinaActiva()));

		Vehiculo sinCambio = escenario.actualizar
			.actualizar(comando(conRutina, "ABC-123", "EQ-001", escenario.idLocalidadActiva(), null));

		assertThat(sinCambio.ultimaRutinaId()).isEqualTo(escenario.rutinaActiva.id());
	}

	@Test
	void falla_si_el_vehiculo_no_existe() {
		assertThatThrownBy(() -> escenario.actualizar.actualizar(new ActualizarVehiculoComando(99, 0, "ABC-123", "EQ-001",
				escenario.idLocalidadActiva(), escenario.idTipoUsoActivo(), null)))
			.isInstanceOf(RecursoNoEncontradoException.class);
	}
}
