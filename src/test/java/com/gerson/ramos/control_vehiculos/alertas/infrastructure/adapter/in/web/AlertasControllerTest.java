package com.gerson.ramos.control_vehiculos.alertas.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.AlertaDetalle;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ConsultarAlertasUseCase;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.PanelDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ResumenDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.TipoAlerta;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

/** Pantalla de alertas con la plantilla real y el caso de uso simulado. */
@WebMvcTest(AlertasController.class)
@WithMockUser(roles = "ADMINISTRADOR")
classAlertasControllerTest {

	private static final ParametrosAlertas UMBRALES = new ParametrosAlertas(1_000, 30, 7);

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ConsultarAlertasUseCase alertas;

	private static AlertaDetalle vencida() {
		return new AlertaDetalle(5, "ABC-123", "EQ-001", "San Pedro Sula", TipoAlerta.MANTENIMIENTO_VENCIDO, Severidad.VENCIDA,
				"Vencido: se pasó por 1,200 km y venció hace 20 días", -1_200L, -20L);
	}

	private static AlertaDetalle proxima() {
		return new AlertaDetalle(6, "DEF-456", "EQ-002", "Tegucigalpa", TipoAlerta.MANTENIMIENTO_PROXIMO, Severidad.PROXIMA,
				"Próximo: faltan 800 km", 800L, 100L);
	}

	private static AlertaDetalle informativa() {
		return new AlertaDetalle(7, "GHI-789", "EQ-003", "La Ceiba", TipoAlerta.SIN_MANTENIMIENTO, Severidad.INFORMATIVA,
				"Sin mantenimientos registrados: no se puede programar el siguiente", null, null);
	}

	private static PanelDeAlertas panel(AlertaDetalle... detalles) {
		List<AlertaDetalle> lista = List.of(detalles);
		return new PanelDeAlertas(lista, ResumenDeAlertas.de(lista), UMBRALES);
	}

	@Test
	void muestra_las_alertas_agrupadas_por_severidad_con_accesos_directos() throws Exception {
		when(alertas.consultar()).thenReturn(panel(vencida(), proxima(), informativa()));

		String html = mvc.perform(get("/alertas"))
			.andExpect(status().isOk())
			.andExpect(view().name("alertas/lista"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Mantenimientos vencidos", "Mantenimientos próximos", "Para revisar", "ABC-123", "DEF-456", "GHI-789",
				"Vencido: se pasó por 1,200 km y venció hace 20 días", "Próximo: faltan 800 km", "Sin mantenimientos registrados",
				"/vehiculos/5/mantenimientos/nuevo", "/vehiculos/5/kilometraje", "/vehiculos/5/mantenimientos", "/configuracion/alertas");
	}

	@Test
	void la_barra_muestra_la_insignia_con_las_alertas_vencidas_y_proximas() throws Exception {
		when(alertas.consultar()).thenReturn(panel(vencida(), proxima(), informativa()));

		String html = mvc.perform(get("/alertas")).andReturn().getResponse().getContentAsString();

		assertThat(html).containsPattern("class=\"[^\"]*\\binsignia\\b[^\"]*\"[^>]*>2<");
	}

	@Test
	void sin_alertas_lo_dice_y_no_muestra_grupos_ni_insignia() throws Exception {
		when(alertas.consultar()).thenReturn(panel());

		String html = mvc.perform(get("/alertas")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		assertThat(html).contains("No hay alertas: todos los vehículos están al día.");
		assertThat(html).doesNotContain("Mantenimientos vencidos", "insignia");
	}

	@Test
	void muestra_los_umbrales_vigentes() throws Exception {
		when(alertas.consultar()).thenReturn(panel(proxima()));

		String html = mvc.perform(get("/alertas")).andReturn().getResponse().getContentAsString();

		assertThat(html).contains(">1,000</span>", ">30</span>", ">7</span>", "pendiente");
	}

	@Test
	void sin_sesion_redirige_al_login() throws Exception {
		mvc.perform(get("/alertas").with(anonymous()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("**/login"));
	}
}
