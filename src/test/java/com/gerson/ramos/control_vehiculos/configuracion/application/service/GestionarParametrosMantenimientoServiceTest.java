package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryParametrosMantenimientoRepository;

class GestionarParametrosMantenimientoServiceTest {

	private final GestionarParametrosMantenimientoService servicio =
			new GestionarParametrosMantenimientoService(new InMemoryParametrosMantenimientoRepository());

	@Test
	void arranca_con_10000_km_y_6_meses() {
		assertThat(servicio.obtener()).isEqualTo(new ParametrosMantenimiento(10_000, 6));
	}

	@Test
	void actualiza_los_intervalos() {
		ParametrosMantenimiento actualizados = servicio.actualizar(12_000, 4);

		assertThat(actualizados).isEqualTo(new ParametrosMantenimiento(12_000, 4));
		assertThat(servicio.obtener()).isEqualTo(actualizados);
	}

	@Test
	void rechaza_intervalos_no_positivos_sin_cambiar_los_vigentes() {
		assertThatThrownBy(() -> servicio.actualizar(0, 6)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> servicio.actualizar(10_000, -1)).isInstanceOf(ReglaDeNegocioException.class);

		assertThat(servicio.obtener()).isEqualTo(new ParametrosMantenimiento(10_000, 6));
	}
}
