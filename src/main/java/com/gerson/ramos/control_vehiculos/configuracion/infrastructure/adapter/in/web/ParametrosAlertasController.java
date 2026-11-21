package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ActualizarParametrosAlertasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosAlertasUseCase;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

import jakarta.validation.Valid;

/** Adaptador de entrada web: umbrales de las alertas (km, días y días para lectura pendiente). */
@Controller
@RequestMapping("/configuracion/alertas")
class ParametrosAlertasController {

	private static final String VISTA = "configuracion/alertas";
	private static final String REDIRIGIR = "redirect:/configuracion/alertas";

	private final ConsultarParametrosAlertasUseCase consultar;
	private final ActualizarParametrosAlertasUseCase actualizar;

	ParametrosAlertasController(ConsultarParametrosAlertasUseCase consultar, ActualizarParametrosAlertasUseCase actualizar) {
		this.consultar = consultar;
		this.actualizar = actualizar;
	}

	@GetMapping
	String ver(Model modelo) {
		modelo.addAttribute("form", ParametrosAlertasForm.desde(consultar.obtener()));
		return VISTA;
	}

	@PostMapping
	String guardar(@Valid @ModelAttribute("form") ParametrosAlertasForm form, BindingResult resultado, RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				actualizar.actualizar(form.getUmbralKm(), form.getUmbralDias(), form.getDiasLecturaPendiente());
				flash.addFlashAttribute("exito", "Umbrales de alerta actualizados");
				return REDIRIGIR;
			}
			catch (ReglaDeNegocioException excepcion) {
				ErroresDeFormulario.registrar(resultado, excepcion);
			}
		}
		return VISTA;
	}
}
