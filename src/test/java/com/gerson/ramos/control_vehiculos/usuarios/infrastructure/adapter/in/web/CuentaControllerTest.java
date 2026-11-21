package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CambiarClavePropiaUseCase;

/** Pantalla "Cambiar mi contraseña" con la plantilla real y el caso de uso simulado. Disponible para cualquier rol (aquí CONSULTA). */
@WebMvcTest(CuentaController.class)
@WithMockUser(username = "ana", roles = "CONSULTA")
class CuentaControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	CambiarClavePropiaUseCase cambiarClave;

	@Test
	void muestra_el_formulario() throws Exception {
		String html = mvc.perform(get("/cuenta/clave")).andExpect(status().isOk()).andExpect(view().name("cuenta/clave"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Cambiar mi contraseña", "name=\"claveActual\"", "name=\"clave\"", "name=\"confirmacion\"");
	}

	@Test
	void cambia_la_clave_del_usuario_de_la_sesion() throws Exception {
		mvc.perform(post("/cuenta/clave").with(csrf()).param("claveActual", "clave-actual-1").param("clave", "clave-nueva-2")
				.param("confirmacion", "clave-nueva-2"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/cuenta/clave"))
			.andExpect(flash().attribute("exito", "Tu contraseña se actualizó"));

		verify(cambiarClave).cambiar("ana", "clave-actual-1", "clave-nueva-2");
	}

	@Test
	void faltan_datos_o_la_confirmacion_no_coincide() throws Exception {
		String html = mvc.perform(post("/cuenta/clave").with(csrf()).param("claveActual", "").param("clave", "clave-nueva-2")
				.param("confirmacion", "otra"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Escribe tu contraseña actual", "La confirmación no coincide con la contraseña");
		verify(cambiarClave, never()).cambiar(any(), any(), any());
	}

	@Test
	void una_clave_actual_incorrecta_se_muestra_junto_a_su_campo() throws Exception {
		doThrow(new ConflictoDeDatosException("claveActual", "La contraseña actual no es correcta")).when(cambiarClave)
			.cambiar("ana", "mala-clave-1", "clave-nueva-2");

		String html = mvc.perform(post("/cuenta/clave").with(csrf()).param("claveActual", "mala-clave-1").param("clave", "clave-nueva-2")
				.param("confirmacion", "clave-nueva-2"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("La contraseña actual no es correcta");
	}

	@Test
	void una_clave_que_incumple_la_politica_se_muestra_como_error_general() throws Exception {
		doThrow(new ReglaDeNegocioException("La contraseña debe tener al menos 8 caracteres")).when(cambiarClave)
			.cambiar("ana", "clave-actual-1", "corta");

		String html = mvc.perform(post("/cuenta/clave").with(csrf()).param("claveActual", "clave-actual-1").param("clave", "corta")
				.param("confirmacion", "corta"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("La contraseña debe tener al menos 8 caracteres");
	}

	@Test
	void el_menu_de_un_usuario_de_consulta_no_muestra_configuracion_ni_kilometraje() throws Exception {
		String html = mvc.perform(get("/cuenta/clave")).andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Cambiar mi contraseña", "CONSULTA", "/alertas", "/reportes");
		assertThat(html).doesNotContain("/configuracion/", "/usuarios", "/kilometraje/semanal");
	}
}
