package com.gerson.ramos.control_vehiculos.shared.infrastructure.web;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone a todas las vistas el usuario autenticado y lo que su rol le permite mostrar:
 * {@code usuarioActual} (nombre de usuario), {@code rolActual} (ADMINISTRADOR, OPERADOR, CONSULTA o nulo),
 * {@code esAdministrador} y {@code puedeEscribir}. Es solo para ocultar controles; el acceso real lo decide SecurityConfig.
 */
@ControllerAdvice
class UsuarioActualAdvice {

	private static final String ROL_ADMINISTRADOR = "ROLE_ADMINISTRADOR";
	private static final String ROL_OPERADOR = "ROLE_OPERADOR";
	private static final String ROL_CONSULTA = "ROLE_CONSULTA";

	@ModelAttribute
	void datosDelUsuarioActual(@Nullable Authentication autenticacion, Model modelo) {
		boolean administrador = tiene(autenticacion, ROL_ADMINISTRADOR);
		boolean operador = tiene(autenticacion, ROL_OPERADOR);
		boolean consulta = tiene(autenticacion, ROL_CONSULTA);
		modelo.addAttribute("usuarioActual", autenticacion == null ? null : autenticacion.getName());
		modelo.addAttribute("rolActual", administrador ? "ADMINISTRADOR" : operador ? "OPERADOR" : consulta ? "CONSULTA" : null);
		modelo.addAttribute("esAdministrador", administrador);
		modelo.addAttribute("puedeEscribir", administrador || operador);
	}

	private static boolean tiene(@Nullable Authentication autenticacion, String autoridad) {
		if (autenticacion == null) {
			return false;
		}
		for (GrantedAuthority concedida : autenticacion.getAuthorities()) {
			if (autoridad.equals(concedida.getAuthority())) {
				return true;
			}
		}
		return false;
	}
}
