package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.ActualizarUsuarioComando;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CrearUsuarioComando;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.GestionarUsuariosUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.UsuarioId;

/** Pantallas de administración de usuarios con las plantillas reales y el caso de uso simulado. Reloj fijo; usuario de sesión "user". */
@WebMvcTest(UsuarioController.class)
@WithMockUser(roles = "ADMINISTRADOR")
class UsuarioControllerTest {

	private static final Instant AHORA = Instant.parse("2026-11-21T12:00:00Z");

	@TestConfiguration
	static class RelojFijo {

		@Bean
		Clock reloj() {
			return Clock.fixed(AHORA, ZoneOffset.UTC);
		}
	}

	@Autowired
	MockMvc mvc;

	@MockitoBean
	GestionarUsuariosUseCase usuarios;

	private static Usuario usuario(long id, String username, Rol rol, boolean activo) {
		return Usuario.reconstruir(new UsuarioId(id), username, "Nombre de " + username, rol, activo, "{bcrypt}x", 0, null, null, 3L);
	}

	private static Usuario bloqueado(long id, String username) {
		Usuario usuario = usuario(id, username, Rol.OPERADOR, true);
		for (int i = 0; i < 5; i++) {
			usuario.registrarIntentoFallido(AHORA, 5, Duration.ofMinutes(15));
		}
		return usuario;
	}

	@Test
	void la_lista_muestra_usuarios_estados_y_acciones() throws Exception {
		when(usuarios.listar()).thenReturn(List.of(usuario(1, "user", Rol.ADMINISTRADOR, true), bloqueado(2, "ana"),
				usuario(3, "inactivo", Rol.CONSULTA, false)));

		String html = mvc.perform(get("/usuarios")).andExpect(status().isOk()).andExpect(view().name("usuarios/lista"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("user", "ana", "inactivo", "Administrador", "Operador", "Consulta", "Bloqueado", "Inactivo", "Nunca",
				"/usuarios/2/desbloquear", "/usuarios/3/activar", "/usuarios/2/desactivar", "/usuarios/2/clave", "/usuarios/1/editar");
		// Sobre la propia cuenta no se ofrece desactivar ni restablecer la contraseña.
		assertThat(html).doesNotContain("/usuarios/1/desactivar", "/usuarios/1/clave");
	}

	@Test
	void el_formulario_nuevo_ofrece_los_tres_roles() throws Exception {
		String html = mvc.perform(get("/usuarios/nuevo")).andExpect(status().isOk()).andExpect(view().name("usuarios/formulario"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Nuevo usuario", "Administrador", "Operador", "Consulta", "name=\"clave\"", "name=\"confirmacion\"");
	}

	@Test
	void crea_el_usuario_y_vuelve_a_la_lista() throws Exception {
		when(usuarios.crear(any())).thenReturn(usuario(5, "nuevo.user", Rol.CONSULTA, true));

		mvc.perform(post("/usuarios").with(csrf()).param("username", "nuevo.user").param("nombreCompleto", "Nuevo Usuario")
				.param("rol", "CONSULTA").param("clave", "clave-segura-1").param("confirmacion", "clave-segura-1"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/usuarios"))
			.andExpect(flash().attribute("exito", "Usuario nuevo.user creado"));

		verify(usuarios).crear(new CrearUsuarioComando("nuevo.user", "Nuevo Usuario", Rol.CONSULTA, "clave-segura-1"));
	}

	@Test
	void una_confirmacion_distinta_no_crea_nada_y_lo_dice() throws Exception {
		String html = mvc.perform(post("/usuarios").with(csrf()).param("username", "nuevo.user").param("nombreCompleto", "Nuevo")
				.param("rol", "OPERADOR").param("clave", "clave-segura-1").param("confirmacion", "otra-cosa"))
			.andExpect(status().isOk())
			.andExpect(view().name("usuarios/formulario"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("La confirmación no coincide con la contraseña");
		verify(usuarios, never()).crear(any());
	}

	@Test
	void faltan_datos_obligatorios() throws Exception {
		String html = mvc.perform(post("/usuarios").with(csrf()).param("username", " ").param("nombreCompleto", " ")
				.param("rol", "OPERADOR").param("clave", "").param("confirmacion", ""))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("El nombre de usuario es obligatorio", "El nombre es obligatorio", "La contraseña es obligatoria");
		verify(usuarios, never()).crear(any());
	}

	@Test
	void un_nombre_repetido_se_muestra_junto_al_campo() throws Exception {
		when(usuarios.crear(any())).thenThrow(new ConflictoDeDatosException("username", "Ya existe un usuario con el nombre ana"));

		String html = mvc.perform(post("/usuarios").with(csrf()).param("username", "ana").param("nombreCompleto", "Ana")
				.param("rol", "OPERADOR").param("clave", "clave-segura-1").param("confirmacion", "clave-segura-1"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Ya existe un usuario con el nombre ana");
	}

	@Test
	void una_clave_que_incumple_la_politica_se_muestra_como_error_general() throws Exception {
		when(usuarios.crear(any())).thenThrow(new ReglaDeNegocioException("La contraseña debe tener al menos 8 caracteres"));

		String html = mvc.perform(post("/usuarios").with(csrf()).param("username", "ana").param("nombreCompleto", "Ana")
				.param("rol", "OPERADOR").param("clave", "corta").param("confirmacion", "corta"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("La contraseña debe tener al menos 8 caracteres");
	}

	@Test
	void el_formulario_de_edicion_trae_los_datos_y_el_nombre_de_usuario_no_se_cambia() throws Exception {
		when(usuarios.buscar(2)).thenReturn(usuario(2, "ana", Rol.OPERADOR, true));

		String html = mvc.perform(get("/usuarios/2/editar")).andExpect(status().isOk()).andExpect(view().name("usuarios/formulario"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Editar usuario", "value=\"ana\"", "readonly", "value=\"Nombre de ana\"", "name=\"version\"", "value=\"3\"");
		assertThat(html).doesNotContain("name=\"clave\"");
	}

	@Test
	void actualiza_con_el_actor_de_la_sesion_y_la_version_leida() throws Exception {
		when(usuarios.actualizar(any())).thenReturn(usuario(2, "ana", Rol.CONSULTA, true));

		mvc.perform(post("/usuarios/2").with(csrf()).param("nombreCompleto", "Ana María").param("rol", "CONSULTA").param("version", "3"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/usuarios"))
			.andExpect(flash().attribute("exito", "Usuario ana actualizado"));

		verify(usuarios).actualizar(new ActualizarUsuarioComando(2, 3L, "Ana María", Rol.CONSULTA, "user"));
	}

	@Test
	void una_regla_de_negocio_al_actualizar_vuelve_al_formulario() throws Exception {
		when(usuarios.actualizar(any())).thenThrow(new ReglaDeNegocioException("Debe quedar al menos un administrador activo"));
		when(usuarios.buscar(2)).thenReturn(usuario(2, "ana", Rol.ADMINISTRADOR, true));

		String html = mvc.perform(post("/usuarios/2").with(csrf()).param("nombreCompleto", "Ana").param("rol", "OPERADOR").param("version", "3"))
			.andExpect(status().isOk())
			.andExpect(view().name("usuarios/formulario"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Debe quedar al menos un administrador activo", "value=\"ana\"");
	}

	@Test
	void actualizar_sin_version_no_llama_al_caso_de_uso() throws Exception {
		when(usuarios.buscar(2)).thenReturn(usuario(2, "ana", Rol.OPERADOR, true));

		mvc.perform(post("/usuarios/2").with(csrf()).param("nombreCompleto", "Ana").param("rol", "OPERADOR"))
			.andExpect(status().isOk())
			.andExpect(view().name("usuarios/formulario"));

		verify(usuarios, never()).actualizar(any());
	}

	@Test
	void desactiva_y_activa_con_mensaje() throws Exception {
		when(usuarios.cambiarEstado(2, false, "user")).thenReturn(usuario(2, "ana", Rol.OPERADOR, false));
		when(usuarios.cambiarEstado(2, true, "user")).thenReturn(usuario(2, "ana", Rol.OPERADOR, true));

		mvc.perform(post("/usuarios/2/desactivar").with(csrf()))
			.andExpect(redirectedUrl("/usuarios"))
			.andExpect(flash().attribute("exito", "Usuario ana desactivado"));
		mvc.perform(post("/usuarios/2/activar").with(csrf()))
			.andExpect(redirectedUrl("/usuarios"))
			.andExpect(flash().attribute("exito", "Usuario ana activado"));
	}

	@Test
	void si_el_caso_de_uso_lo_rechaza_el_mensaje_va_en_la_lista() throws Exception {
		when(usuarios.cambiarEstado(1, false, "user")).thenThrow(new ReglaDeNegocioException("No puedes desactivar tu propia cuenta"));

		mvc.perform(post("/usuarios/1/desactivar").with(csrf()))
			.andExpect(redirectedUrl("/usuarios"))
			.andExpect(flash().attribute("error", "No puedes desactivar tu propia cuenta"));
	}

	@Test
	void desbloquea_un_usuario() throws Exception {
		when(usuarios.desbloquear(2)).thenReturn(usuario(2, "ana", Rol.OPERADOR, true));

		mvc.perform(post("/usuarios/2/desbloquear").with(csrf()))
			.andExpect(redirectedUrl("/usuarios"))
			.andExpect(flash().attribute("exito", "Usuario ana desbloqueado"));
	}

	@Test
	void el_formulario_de_restablecer_muestra_el_usuario() throws Exception {
		when(usuarios.buscar(2)).thenReturn(usuario(2, "ana", Rol.OPERADOR, true));

		String html = mvc.perform(get("/usuarios/2/clave")).andExpect(status().isOk()).andExpect(view().name("usuarios/clave"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Restablecer contraseña de", "ana", "name=\"clave\"", "name=\"confirmacion\"");
	}

	@Test
	void restablece_la_clave_con_el_actor_de_la_sesion() throws Exception {
		when(usuarios.restablecerClave(2, "clave-nueva-2", "user")).thenReturn(usuario(2, "ana", Rol.OPERADOR, true));

		mvc.perform(post("/usuarios/2/clave").with(csrf()).param("clave", "clave-nueva-2").param("confirmacion", "clave-nueva-2"))
			.andExpect(redirectedUrl("/usuarios"))
			.andExpect(flash().attributeExists("exito"));
	}

	@Test
	void restablecer_con_confirmacion_distinta_vuelve_al_formulario_sin_llamar_al_caso_de_uso() throws Exception {
		when(usuarios.buscar(2)).thenReturn(usuario(2, "ana", Rol.OPERADOR, true));

		String html = mvc.perform(post("/usuarios/2/clave").with(csrf()).param("clave", "clave-nueva-2").param("confirmacion", "distinta"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("La confirmación no coincide con la contraseña");
		verify(usuarios, never()).restablecerClave(anyLong(), any(), any());
	}

	@Test
	void restablecer_una_clave_debil_se_muestra_como_error() throws Exception {
		when(usuarios.restablecerClave(2, "corta", "user")).thenThrow(new ReglaDeNegocioException("La contraseña debe tener al menos 8 caracteres"));
		when(usuarios.buscar(2)).thenReturn(usuario(2, "ana", Rol.OPERADOR, true));

		String html = mvc.perform(post("/usuarios/2/clave").with(csrf()).param("clave", "corta").param("confirmacion", "corta"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("La contraseña debe tener al menos 8 caracteres");
	}
}
