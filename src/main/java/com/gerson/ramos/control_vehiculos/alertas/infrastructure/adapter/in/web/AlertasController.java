package com.gerson.ramos.control_vehiculos.alertas.infrastructure.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ConsultarAlertasUseCase;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.PanelDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;

/** Adaptador de entrada web: panel de alertas (mantenimientos vencidos y próximos, y avisos informativos). */
@Controller
@RequestMapping("/alertas")
class AlertasController {

	private final ConsultarAlertasUseCase alertas;

	AlertasController(ConsultarAlertasUseCase alertas) {
		this.alertas = alertas;
	}

	@GetMapping
	String ver(Model modelo) {
		PanelDeAlertas panel = alertas.consultar();
		modelo.addAttribute("resumen", panel.resumen());
		modelo.addAttribute("umbrales", panel.umbrales());
		modelo.addAttribute("vencidas", panel.deSeveridad(Severidad.VENCIDA));
		modelo.addAttribute("proximas", panel.deSeveridad(Severidad.PROXIMA));
		modelo.addAttribute("informativas", panel.deSeveridad(Severidad.INFORMATIVA));
		return "alertas/lista";
	}
}
