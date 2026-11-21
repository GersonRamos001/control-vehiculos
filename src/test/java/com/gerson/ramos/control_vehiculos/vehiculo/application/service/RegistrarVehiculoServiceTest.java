package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarVehiculoComando;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

class RegistrarVehiculoServiceTest {

	private final EscenarioVehiculos escenario = new EscenarioVehiculos();

	private RegistrarVehiculoComando comando(String placa, String codigoEquipo, long localidadId, long tipoUsoId, Long rutinaId) {
		return new RegistrarVehiculoComando(placa, codigoEquipo, localidadId, tipoUsoId, rutinaId);
	}

	@Test
	void registra_un_vehiculo_con_rutina() {
		Vehiculo vehiculo = escenario.registrar.registrar(comando(" abc-123 ", "eq-001", escenario.idLocalidadActiva(),
				escenario.idTipoUsoActivo(), escenario.idRutinaActiva()));

		assertThat(vehiculo.id()).isNotNull();
		assertThat(vehiculo.placa().valor()).isEqualTo("ABC-123");
		assertThat(vehiculo.codigoEquipo().valor()).isEqualTo("EQ-001");
		assertThat(vehiculo.activo()).isTrue();
		assertThat(vehiculo.ultimaRutinaId()).isEqualTo(escenario.rutinaActiva.id());
		assertThat(escenario.vehiculos.listarTodos()).hasSize(1);
	}

	@Test
	void registra_un_vehiculo_sin_rutina() {
		Vehiculo vehiculo = escenario.registrarVehiculo("ABC-123", "EQ-001");

		assertThat(vehiculo.ultimaRutinaId()).isNull();
	}

	@Test
	void rechaza_placa_duplicada_aunque_cambie_el_formato() {
		escenario.registrarVehiculo("ABC-123", "EQ-001");

		assertThatThrownBy(() -> escenario.registrarVehiculo(" abc-123", "EQ-002"))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("placa"));
		assertThat(escenario.vehiculos.listarTodos()).hasSize(1);
	}

	@Test
	void rechaza_codigo_de_equipo_duplicado() {
		escenario.registrarVehiculo("ABC-123", "EQ-001");

		assertThatThrownBy(() -> escenario.registrarVehiculo("XYZ-999", "eq-001"))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("codigoEquipo"));
	}

	@Test
	void rechaza_localidad_inactiva_o_inexistente() {
		long inactiva = escenario.localidadInactiva.id().valor();

		assertThatThrownBy(() -> escenario.registrar.registrar(comando("ABC-123", "EQ-001", inactiva, escenario.idTipoUsoActivo(), null)))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("localidadId"));
		assertThatThrownBy(() -> escenario.registrar.registrar(comando("ABC-123", "EQ-001", 999, escenario.idTipoUsoActivo(), null)))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("localidadId"));
	}

	@Test
	void rechaza_tipo_de_uso_inactivo() {
		long inactivo = escenario.tipoUsoInactivo.id().valor();

		assertThatThrownBy(() -> escenario.registrar.registrar(comando("ABC-123", "EQ-001", escenario.idLocalidadActiva(), inactivo, null)))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("tipoUsoId"));
	}

	@Test
	void rechaza_rutina_inactiva() {
		long inactiva = escenario.rutinaInactiva.id().valor();

		assertThatThrownBy(() -> escenario.registrar.registrar(
				comando("ABC-123", "EQ-001", escenario.idLocalidadActiva(), escenario.idTipoUsoActivo(), inactiva)))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("rutinaId"));
	}

	@Test
	void rechaza_placa_con_formato_invalido() {
		assertThatThrownBy(() -> escenario.registrarVehiculo("AB", "EQ-001"))
			.isInstanceOf(ReglaDeNegocioException.class)
			.isNotInstanceOf(ConflictoDeDatosException.class);
	}
}
