package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryParametrosAlertasRepository;

class GestionarParametrosAlertasServiceTest {

	private final GestionarParametrosAlertasService servicio =
			new GestionarParametrosAlertasService(new InMemoryParametrosAlertasRepository());

	@Test
	void arranca_con_1000_km_30_dias_y_7_dias_de_lectura_pendiente() {
		assertThat(servicio.obtener()).isEqualTo(new ParametrosAlertas(1_000, 30, 7));
	}

	@Test
	void actualiza_los_umbrales() {
		ParametrosAlertas actualizados = servicio.actualizar(1_500, 45, 10);

		assertThat(actualizados).isEqualTo(new ParametrosAlertas(1_500, 45, 10));
		assertThat(servicio.obtener()).isEqualTo(actualizados);
	}

	@Test
	void rechaza_valores_no_positivos_sin_cambiar_los_vigentes() {
		assertThatThrownBy(() -> servicio.actualizar(0, 30, 7)).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> servicio.actualizar(1_000, 30, -1)).isInstanceOf(ReglaDeNegocioException.class);

		assertThat(servicio.obtener()).isEqualTo(new ParametrosAlertas(1_000, 30, 7));
	}
}
