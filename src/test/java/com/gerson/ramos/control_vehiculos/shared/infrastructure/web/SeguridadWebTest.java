package com.gerson.ramos.control_vehiculos.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.gerson.ramos.control_vehiculos.shared.infrastructure.security.SecurityConfig;

/**
 * Reglas de acceso reales (SecurityConfig) por rol sobre las rutas de la aplicación (RutasDePrueba responde 200 en todas),
 * inicio de sesión con usuarios simulados y cierre de sesiones por parte de un administrador.
 */
@WebMvcTest(RutasDePrueba.class)
@Import({ SecurityConfig.class, SeguridadWebTest.UsuariosDePrueba.class })
class SeguridadWebTest {

	@TestConfiguration
	static class UsuariosDePrueba {

		@Bean
		UserDetailsService usuarios() {
			return new InMemoryUserDetailsManager(
					User.withUsername("jefa").password("{noop}clave-jefa").roles("ADMINISTRADOR").build(),
					User.withUsername("oper").password("{noop}clave-oper").roles("OPERADOR").build(),
					User.withUsername("cons").password("{noop}clave-cons").roles("CONSULTA").build(),
					User.withUsername("inactivo").password("{noop}clave-x").roles("OPERADOR").disabled(true).build(),
					User.withUsername("bloqueado").password("{noop}clave-x").roles("OPERADOR").accountLocked(true).build());
		}
	}

	@Autowired
	MockMvc mvc;

	@Autowired
	SessionRegistry registro;

	@Test
	void una_peticion_anonima_a_una_pagina_protegida_redirige_al_login() throws Exception {
		mvc.perform(get("/vehiculos")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	void la_pagina_de_login_es_publica() throws Exception {
		mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
	}

	/** Cada fila: rol, método, ruta y estado esperado (200 = permitido, 403 = rol sin permiso). */
	@ParameterizedTest(name = "{0} {1} {2} -> {3}")
	@CsvSource({
			// El administrador entra a todo.
			"ADMINISTRADOR, GET, /vehiculos, 200", "ADMINISTRADOR, POST, /vehiculos, 200", "ADMINISTRADOR, GET, /usuarios, 200",
			"ADMINISTRADOR, POST, /usuarios/2/clave, 200", "ADMINISTRADOR, GET, /configuracion/parametros, 200",
			"ADMINISTRADOR, GET, /kilometraje/semanal, 200", "ADMINISTRADOR, GET, /reportes/pendientes, 200",
			// El operador hace todo lo operativo, pero no administra usuarios ni configuración.
			"OPERADOR, GET, /vehiculos, 200", "OPERADOR, POST, /vehiculos, 200", "OPERADOR, GET, /vehiculos/nuevo, 200",
			"OPERADOR, GET, /vehiculos/5/mantenimientos, 200", "OPERADOR, GET, /vehiculos/5/kilometraje, 200",
			"OPERADOR, GET, /kilometraje/semanal, 200", "OPERADOR, POST, /kilometraje/semanal, 200", "OPERADOR, GET, /alertas, 200",
			"OPERADOR, GET, /reportes/pendientes, 200", "OPERADOR, GET, /reportes/pendientes/xlsx, 200",
			"OPERADOR, GET, /configuracion/parametros, 403", "OPERADOR, POST, /configuracion/localidades, 403",
			"OPERADOR, GET, /usuarios, 403", "OPERADOR, POST, /usuarios/2/clave, 403",
			// La consulta solo lee la lista de vehículos, la programación, las alertas y los reportes.
			"CONSULTA, GET, /vehiculos, 200", "CONSULTA, GET, /mantenimientos/programacion, 200", "CONSULTA, GET, /alertas, 200",
			"CONSULTA, GET, /reportes/pendientes, 200", "CONSULTA, GET, /reportes/pendientes/xlsx, 200",
			"CONSULTA, GET, /js/tablas-interactivas.js, 200", "CONSULTA, GET, /cuenta/clave, 200", "CONSULTA, POST, /cuenta/clave, 200",
			"CONSULTA, POST, /vehiculos, 403", "CONSULTA, GET, /vehiculos/nuevo, 403", "CONSULTA, GET, /vehiculos/5/mantenimientos, 403",
			"CONSULTA, GET, /vehiculos/5/kilometraje, 403", "CONSULTA, GET, /kilometraje/semanal, 403",
			"CONSULTA, POST, /kilometraje/semanal, 403", "CONSULTA, GET, /configuracion/parametros, 403", "CONSULTA, GET, /usuarios, 403" })
	void los_permisos_dependen_del_rol(String rol, String metodo, String ruta, int estado) throws Exception {
		var peticion = "POST".equals(metodo) ? post(ruta).with(csrf()) : get(ruta);

		mvc.perform(peticion.with(user("prueba").roles(rol))).andExpect(status().is(estado));
	}

	@Test
	void sin_csrf_un_post_se_rechaza_aunque_el_rol_lo_permita() throws Exception {
		mvc.perform(post("/vehiculos").with(user("oper").roles("OPERADOR"))).andExpect(status().isForbidden());
	}

	@Test
	void la_raiz_requiere_sesion_y_con_ella_no_da_403_a_ningun_rol() throws Exception {
		mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
		// "/" no tiene controlador en esta prueba: lo importante es que la regla de acceso no la bloquea.
		mvc.perform(get("/").with(user("cons").roles("CONSULTA"))).andExpect(status().isNotFound());
	}

	@Test
	void un_usuario_valido_inicia_sesion_con_su_rol() throws Exception {
		mvc.perform(formLogin("/login").user("oper").password("clave-oper"))
			.andExpect(authenticated().withUsername("oper").withRoles("OPERADOR"));
		mvc.perform(formLogin("/login").user("jefa").password("clave-jefa"))
			.andExpect(authenticated().withUsername("jefa").withRoles("ADMINISTRADOR"));
	}

	@Test
	void una_contrasena_incorrecta_no_inicia_sesion() throws Exception {
		mvc.perform(formLogin("/login").user("oper").password("incorrecta"))
			.andExpect(unauthenticated())
			.andExpect(redirectedUrl("/login?error"));
	}

	@Test
	void un_usuario_desactivado_o_bloqueado_no_entra_ni_con_la_contrasena_correcta() throws Exception {
		mvc.perform(formLogin("/login").user("inactivo").password("clave-x"))
			.andExpect(unauthenticated())
			.andExpect(redirectedUrl("/login?error"));
		mvc.perform(formLogin("/login").user("bloqueado").password("clave-x"))
			.andExpect(unauthenticated())
			.andExpect(redirectedUrl("/login?error"));
	}

	@Test
	@WithMockUser
	void cerrar_sesion_requiere_post_con_csrf_y_vuelve_al_login() throws Exception {
		mvc.perform(logout()).andExpect(redirectedUrl("/login?logout"));
		mvc.perform(get("/logout").with(csrf())).andExpect(status().is4xxClientError());
	}

	@Test
	void las_sesiones_expiradas_por_un_administrador_llevan_al_login() throws Exception {
		MvcResult acceso = mvc.perform(formLogin("/login").user("cons").password("clave-cons"))
			.andExpect(authenticated().withUsername("cons"))
			.andReturn();
		MockHttpSession sesion = (MockHttpSession) acceso.getRequest().getSession(false);
		mvc.perform(get("/alertas").session(sesion)).andExpect(status().isOk());

		// Lo que hace SesionesAdapter al desactivar o cambiar a un usuario.
		var principales = registro.getAllPrincipals();
		assertThat(principales).isNotEmpty();
		principales.forEach(principal -> registro.getAllSessions(principal, false).forEach(info -> info.expireNow()));

		mvc.perform(get("/alertas").session(sesion)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?expirada"));
	}
}
