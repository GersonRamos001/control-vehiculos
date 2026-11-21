package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ActualizarVehiculoComando;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

class CambiarEstadoYConsultarVehiculosServiceTest {

	private final EscenarioVehiculos escenario = new EscenarioVehiculos();

	@Test
	void desactivar_y_activar_son_idempotentes() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");
		long id = registrado.id().valor();

		Vehiculo inactivo = escenario.cambiarEstado.desactivar(id);
		Vehiculo otraVez = escenario.cambiarEstado.desactivar(id);
		assertThat(inactivo.activo()).isFalse();
		assertThat(otraVez.version()).isEqualTo(inactivo.version());

		Vehiculo activo = escenario.cambiarEstado.activar(id);
		Vehiculo activoDeNuevo = escenario.cambiarEstado.activar(id);
		assertThat(activo.activo()).isTrue();
		assertThat(activoDeNuevo.version()).isEqualTo(activo.version());
	}

	@Test
	void falla_si_el_vehiculo_no_existe() {
		assertThatThrownBy(() -> escenario.cambiarEstado.desactivar(99)).isInstanceOf(RecursoNoEncontradoException.class);
		assertThatThrownBy(() -> escenario.cambiarEstado.activar(99)).isInstanceOf(RecursoNoEncontradoException.class);
		assertThatThrownBy(() -> escenario.consultar.buscarPorId(99)).isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void listar_filtra_por_estado_y_ordena_por_placa() {
		escenario.registrarVehiculo("ZZZ-111", "EQ-003");
		Vehiculo aaa = escenario.registrarVehiculo("AAA-111", "EQ-001");
		Vehiculo mmm = escenario.registrarVehiculo("MMM-111", "EQ-002");
		escenario.cambiarEstado.desactivar(mmm.id().valor());

		List<VehiculoDetalle> activos = escenario.consultar.listar(FiltroEstado.ACTIVOS);
		List<VehiculoDetalle> todos = escenario.consultar.listar(FiltroEstado.TODOS);

		assertThat(activos).extracting(VehiculoDetalle::placa).containsExactly("AAA-111", "ZZZ-111");
		assertThat(todos).extracting(VehiculoDetalle::placa).containsExactly("AAA-111", "MMM-111", "ZZZ-111");
		assertThat(todos).filteredOn(v -> !v.activo()).extracting(VehiculoDetalle::id).containsExactly(mmm.id().valor());
		assertThat(aaa.id()).isNotNull();
	}

	@Test
	void el_detalle_resuelve_los_nombres_de_los_catalogos() {
		Vehiculo registrado = escenario.registrarVehiculo("ABC-123", "EQ-001");
		escenario.actualizar.actualizar(new ActualizarVehiculoComando(registrado.id().valor(), registrado.version(),
				"ABC-123", "EQ-001", escenario.idLocalidadActiva(), escenario.idTipoUsoActivo(), escenario.idRutinaActiva()));

		VehiculoDetalle detalle = escenario.consultar.listar(FiltroEstado.ACTIVOS).get(0);

		assertThat(detalle.localidad()).isEqualTo("San Pedro Sula");
		assertThat(detalle.tipoUso()).isEqualTo("Operativo");
		assertThat(detalle.rutina()).isEqualTo("10K");
	}

	@Test
	void vehiculo_sin_rutina_muestra_rutina_nula() {
		escenario.registrarVehiculo("ABC-123", "EQ-001");

		VehiculoDetalle detalle = escenario.consultar.listar(FiltroEstado.ACTIVOS).get(0);

		assertThat(detalle.rutinaId()).isNull();
		assertThat(detalle.rutina()).isNull();
	}
}
