package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.web;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CambiarClavePropiaUseCase;

/** Adaptador de entrada web: cada usuario cambia su propia contraseña (disponible para cualquier rol). */
@Controller
@RequestMapping("/cuenta")
class CuentaController {

	private static final String VISTA = "cuenta/clave";

	private final CambiarClavePropiaUseCase cambiarClave;

	CuentaController(CambiarClavePropiaUseCase cambiarClave) {
		this.cambiarClave = cambiarClave;
	}

	@GetMapping("/clave")
	String formulario(Model modelo) {
		modelo.addAttribute("form", new ClaveForm());
		return VISTA;
	}

	@PostMapping("/clave")
	String cambiar(@ModelAttribute("form") ClaveForm form, BindingResult resultado, Principal principal, Model modelo,
			RedirectAttributes flash) {
		if (form.getClaveActual() == null || form.getClaveActual().isEmpty()) {
			resultado.rejectValue("claveActual", "obligatoria", "Escribe tu contraseña actual");
		}
		ErroresDeFormulario.validarClaveConfirmada(resultado, form.getClave(), form.getConfirmacion());
		if (!resultado.hasErrors()) {
			try {
				cambiarClave.cambiar(principal.getName(), form.getClaveActual(), form.getClave());
				flash.addFlashAttribute("exito", "Tu contraseña se actualizó");
				return "redirect:/cuenta/clave";
			}
			catch (ReglaDeNegocioException excepcion) {
				ErroresDeFormulario.registrar(resultado, excepcion);
			}
		}
		modelo.addAttribute("form", form);
		return VISTA;
	}
}
