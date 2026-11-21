package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

class RegistrarRutinaRealizadaServiceTest {

	private final EscenarioVehiculos escenario = new EscenarioVehiculos();

	@Test
	void registra_la_rutina_realizada_y_sube_la_version() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");

		Vehiculo actualizado = escenario.rutinaRealizada.registrarRutinaRealizada(registrado.id().valor(), escenario.idRutinaActiva());

		assertThat(actualizado.ultimaRutinaId()).isEqualTo(escenario.rutinaActiva.id());
		assertThat(actualizado.version()).isEqualTo(registrado.version() + 1);
	}

	@Test
	void si_la_rutina_ya_es_la_registrada_no_guarda_de_nuevo() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");
		Vehiculo conRutina = escenario.rutinaRealizada.registrarRutinaRealizada(registrado.id().valor(), escenario.idRutinaActiva());

		Vehiculo igual = escenario.rutinaRealizada.registrarRutinaRealizada(registrado.id().valor(), escenario.idRutinaActiva());

		assertThat(igual.version()).isEqualTo(conRutina.version());
	}

	@Test
	void rechaza_una_rutina_inactiva() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");

		assertThatThrownBy(() -> escenario.rutinaRealizada.registrarRutinaRealizada(registrado.id().valor(),
				escenario.rutinaInactiva.id().valor()))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("rutinaId"));
	}

	@Test
	void rechaza_un_vehiculo_inactivo_y_uno_inexistente() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");
		escenario.cambiarEstado.desactivar(registrado.id().valor());

		assertThatThrownBy(() -> escenario.rutinaRealizada.registrarRutinaRealizada(registrado.id().valor(), escenario.idRutinaActiva()))
			.isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> escenario.rutinaRealizada.registrarRutinaRealizada(999, escenario.idRutinaActiva()))
			.isInstanceOf(RecursoNoEncontradoException.class);
	}
}
