package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ActualizarParametrosMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

import jakarta.validation.Valid;

/** Adaptador de entrada web: parámetros globales del mantenimiento preventivo (intervalo de km y de meses). */
@Controller
@RequestMapping("/configuracion/parametros")
class ParametrosMantenimientoController {

	private static final String VISTA = "configuracion/parametros";
	private static final String REDIRIGIR = "redirect:/configuracion/parametros";

	private final ConsultarParametrosMantenimientoUseCase consultar;
	private final ActualizarParametrosMantenimientoUseCase actualizar;

	ParametrosMantenimientoController(ConsultarParametrosMantenimientoUseCase consultar,
			ActualizarParametrosMantenimientoUseCase actualizar) {
		this.consultar = consultar;
		this.actualizar = actualizar;
	}

	@GetMapping
	String ver(Model modelo) {
		modelo.addAttribute("form", ParametrosForm.desde(consultar.obtener()));
		return VISTA;
	}

	@PostMapping
	String guardar(@Valid @ModelAttribute("form") ParametrosForm form, BindingResult resultado, RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				actualizar.actualizar(form.getIntervaloKm(), form.getIntervaloMeses());
				flash.addFlashAttribute("exito", "Parámetros de mantenimiento actualizados");
				return REDIRIGIR;
			}
			catch (ReglaDeNegocioException excepcion) {
				ErroresDeFormulario.registrar(resultado, excepcion);
			}
		}
		return VISTA;
	}
}
