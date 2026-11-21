package com.gerson.ramos.control_vehiculos.shared.infrastructure.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class InicioController {

	@GetMapping("/")
	String inicio() {
		return "redirect:/vehiculos";
	}

	@GetMapping("/login")
	String login() {
		return "login";
	}
}
