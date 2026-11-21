package com.gerson.ramos.control_vehiculos.alertas.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.AlertaDetalle;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ConsultarAlertasUseCase;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.PanelDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ResumenDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.TipoAlerta;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

/** La insignia de la barra nunca debe romper ni retrasar innecesariamente una página. */
class ContadorDeAlertasAdviceTest {

	private static final Principal USUARIO = () -> "admin";

	@SuppressWarnings("unchecked")
	private final ObjectProvider<ConsultarAlertasUseCase> proveedor = mock(ObjectProvider.class);

	private final ConsultarAlertasUseCase casoDeUso = mock(ConsultarAlertasUseCase.class);

	private final ContadorDeAlertasAdvice advice = new ContadorDeAlertasAdvice(proveedor);

	private static PanelDeAlertas panelConDosActivas() {
		List<AlertaDetalle> lista = List.of(
				new AlertaDetalle(1, "AAA-111", "EQ-1", "SPS", TipoAlerta.MANTENIMIENTO_VENCIDO, Severidad.VENCIDA, "v", -1L, null),
				new AlertaDetalle(2, "BBB-222", "EQ-2", "SPS", TipoAlerta.MANTENIMIENTO_PROXIMO, Severidad.PROXIMA, "p", 10L, null),
				new AlertaDetalle(3, "CCC-333", "EQ-3", "SPS", TipoAlerta.SIN_MANTENIMIENTO, Severidad.INFORMATIVA, "i", null, null));
		return new PanelDeAlertas(lista, ResumenDeAlertas.de(lista), new ParametrosAlertas(1_000, 30, 7));
	}

	@Test
	void en_una_peticion_get_con_sesion_devuelve_las_alertas_vencidas_mas_proximas() {
		when(proveedor.getIfAvailable()).thenReturn(casoDeUso);
		when(casoDeUso.consultar()).thenReturn(panelConDosActivas());

		assertThat(advice.alertasActivas(new MockHttpServletRequest("GET", "/vehiculos"), USUARIO)).isEqualTo(2);
	}

	@Test
	void sin_sesion_no_calcula_nada() {
		when(proveedor.getIfAvailable()).thenReturn(casoDeUso);

		assertThat(advice.alertasActivas(new MockHttpServletRequest("GET", "/login"), null)).isNull();

		verify(casoDeUso, never()).consultar();
	}

	@Test
	void en_peticiones_que_no_son_get_no_calcula_nada() {
		when(proveedor.getIfAvailable()).thenReturn(casoDeUso);

		assertThat(advice.alertasActivas(new MockHttpServletRequest("POST", "/vehiculos"), USUARIO)).isNull();

		verify(casoDeUso, never()).consultar();
	}

	@Test
	void si_el_modulo_de_alertas_no_esta_disponible_no_hay_contador() {
		when(proveedor.getIfAvailable()).thenReturn(null);

		assertThat(advice.alertasActivas(new MockHttpServletRequest("GET", "/vehiculos"), USUARIO)).isNull();
	}

	@Test
	void si_calcular_las_alertas_falla_la_pagina_sigue_funcionando_sin_contador() {
		when(proveedor.getIfAvailable()).thenReturn(casoDeUso);
		when(casoDeUso.consultar()).thenThrow(new IllegalStateException("base de datos caída"));

		assertThatCode(() -> advice.alertasActivas(new MockHttpServletRequest("GET", "/vehiculos"), USUARIO)).doesNotThrowAnyException();
		assertThat(advice.alertasActivas(new MockHttpServletRequest("GET", "/vehiculos"), USUARIO)).isNull();
	}
}
