package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarProgramacionUseCase;

/** Adaptador de entrada web: programación del siguiente mantenimiento de todos los vehículos activos. */
@Controller
@RequestMapping("/mantenimientos/programacion")
class ProgramacionController {

	private final ConsultarProgramacionUseCase programacion;

	ProgramacionController(ConsultarProgramacionUseCase programacion) {
		this.programacion = programacion;
	}

	@GetMapping
	String ver(Model modelo) {
		modelo.addAttribute("filas", programacion.programacionDeVehiculosActivos());
		return "mantenimientos/programacion";
	}
}
