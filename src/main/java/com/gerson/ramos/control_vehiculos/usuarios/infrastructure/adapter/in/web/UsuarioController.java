package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.web;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.ActualizarUsuarioComando;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CrearUsuarioComando;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.GestionarUsuariosUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

import jakarta.validation.Valid;

/** Adaptador de entrada web: administración de usuarios (solo el rol ADMINISTRADOR llega aquí; lo exige SecurityConfig). */
@Controller
@RequestMapping(UsuarioController.BASE)
class UsuarioController {

	static final String BASE = "/usuarios";
	private static final String VISTA_LISTA = "usuarios/lista";
	private static final String VISTA_FORMULARIO = "usuarios/formulario";
	private static final String VISTA_CLAVE = "usuarios/clave";
	private static final String REDIRIGIR_A_LISTA = "redirect:" + BASE;

	private final GestionarUsuariosUseCase usuarios;
	private final Clock reloj;

	UsuarioController(GestionarUsuariosUseCase usuarios, Clock reloj) {
		this.usuarios = usuarios;
		this.reloj = reloj;
	}

	@GetMapping
	String listar(Principal principal, Model modelo) {
		Instant ahora = reloj.instant();
		ZoneId zona = reloj.getZone();
		List<FilaUsuario> filas = usuarios.listar().stream().map(u -> FilaUsuario.de(u, ahora, zona, principal.getName())).toList();
		modelo.addAttribute("filas", filas);
		return VISTA_LISTA;
	}

	@GetMapping("/nuevo")
	String nuevo(Model modelo) {
		prepararFormulario(modelo, UsuarioForm.vacio(), null);
		return VISTA_FORMULARIO;
	}

	@PostMapping
	String crear(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult resultado, Model modelo, RedirectAttributes flash) {
		if (form.getUsername() == null || form.getUsername().isBlank()) {
			resultado.rejectValue("username", "obligatorio", "El nombre de usuario es obligatorio");
		}
		ErroresDeFormulario.validarClaveConfirmada(resultado, form.getClave(), form.getConfirmacion());
		if (!resultado.hasErrors()) {
			try {
				Usuario creado = usuarios.crear(new CrearUsuarioComando(form.getUsername(), form.getNombreCompleto(), form.getRol(),
						form.getClave()));
				flash.addFlashAttribute("exito", "Usuario " + creado.username() + " creado");
				return REDIRIGIR_A_LISTA;
			}
			catch (ReglaDeNegocioException | DataIntegrityViolationException excepcion) {
				ErroresDeFormulario.registrar(resultado, excepcion);
			}
		}
		prepararFormulario(modelo, form, null);
		return VISTA_FORMULARIO;
	}

	@GetMapping("/{id}/editar")
	String editar(@PathVariable long id, Model modelo) {
		prepararFormulario(modelo, UsuarioForm.desde(usuarios.buscar(id)), id);
		return VISTA_FORMULARIO;
	}

	@PostMapping("/{id}")
	String actualizar(@PathVariable long id, @Valid @ModelAttribute("form") UsuarioForm form, BindingResult resultado, Model modelo,
			Principal principal, RedirectAttributes flash) {
		if (form.getVersion() == null) {
			resultado.reject("version", "Falta la versión del usuario; recarga la página");
		}
		if (!resultado.hasErrors()) {
			try {
				Usuario actualizado = usuarios.actualizar(new ActualizarUsuarioComando(id, form.getVersion(), form.getNombreCompleto(),
						form.getRol(), principal.getName()));
				flash.addFlashAttribute("exito", "Usuario " + actualizado.username() + " actualizado");
				return REDIRIGIR_A_LISTA;
			}
			catch (ReglaDeNegocioException | DataIntegrityViolationException | ObjectOptimisticLockingFailureException excepcion) {
				ErroresDeFormulario.registrar(resultado, excepcion);
			}
		}
		// El nombre de usuario no viaja en la edición (es de solo lectura): se recupera para mostrarlo.
		form.setUsername(usuarios.buscar(id).username());
		prepararFormulario(modelo, form, id);
		return VISTA_FORMULARIO;
	}

	@PostMapping("/{id}/activar")
	String activar(@PathVariable long id, Principal principal, RedirectAttributes flash) {
		return cambiarEstado(id, true, principal, flash, "activado");
	}

	@PostMapping("/{id}/desactivar")
	String desactivar(@PathVariable long id, Principal principal, RedirectAttributes flash) {
		return cambiarEstado(id, false, principal, flash, "desactivado");
	}

	@PostMapping("/{id}/desbloquear")
	String desbloquear(@PathVariable long id, RedirectAttributes flash) {
		Usuario usuario = usuarios.desbloquear(id);
		flash.addFlashAttribute("exito", "Usuario " + usuario.username() + " desbloqueado");
		return REDIRIGIR_A_LISTA;
	}

	@GetMapping("/{id}/clave")
	String formularioDeClave(@PathVariable long id, Model modelo) {
		prepararClave(modelo, new ClaveForm(), usuarios.buscar(id), id);
		return VISTA_CLAVE;
	}

	@PostMapping("/{id}/clave")
	String restablecerClave(@PathVariable long id, @ModelAttribute("form") ClaveForm form, BindingResult resultado, Model modelo,
			Principal principal, RedirectAttributes flash) {
		ErroresDeFormulario.validarClaveConfirmada(resultado, form.getClave(), form.getConfirmacion());
		if (!resultado.hasErrors()) {
			try {
				Usuario usuario = usuarios.restablecerClave(id, form.getClave(), principal.getName());
				flash.addFlashAttribute("exito", "Contraseña de " + usuario.username() + " restablecida; sus sesiones abiertas se cerraron");
				return REDIRIGIR_A_LISTA;
			}
			catch (ReglaDeNegocioException excepcion) {
				ErroresDeFormulario.registrar(resultado, excepcion);
			}
		}
		prepararClave(modelo, form, usuarios.buscar(id), id);
		return VISTA_CLAVE;
	}

	private String cambiarEstado(long id, boolean activo, Principal principal, RedirectAttributes flash, String accion) {
		try {
			Usuario usuario = usuarios.cambiarEstado(id, activo, principal.getName());
			flash.addFlashAttribute("exito", "Usuario " + usuario.username() + " " + accion);
		}
		catch (ReglaDeNegocioException excepcion) {
			flash.addFlashAttribute("error", excepcion.getMessage());
		}
		return REDIRIGIR_A_LISTA;
	}

	private void prepararFormulario(Model modelo, UsuarioForm form, Long idEnEdicion) {
		modelo.addAttribute("form", form);
		modelo.addAttribute("edicion", idEnEdicion != null);
		modelo.addAttribute("titulo", idEnEdicion == null ? "Nuevo usuario" : "Editar usuario");
		modelo.addAttribute("accion", idEnEdicion == null ? BASE : BASE + "/" + idEnEdicion);
		modelo.addAttribute("roles", Rol.values());
	}

	private void prepararClave(Model modelo, ClaveForm form, Usuario usuario, long id) {
		modelo.addAttribute("form", form);
		modelo.addAttribute("username", usuario.username());
		modelo.addAttribute("accion", BASE + "/" + id + "/clave");
	}
}
