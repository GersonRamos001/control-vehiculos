package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.in.web;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarSeguimientoUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.LecturaPorVehiculo;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturasSemanalesUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoLote;

import jakarta.validation.Valid;

/**
 * Adaptador de entrada web: captura semanal de lecturas de odómetro de toda la flota, con el seguimiento de cuánto
 * falta para el siguiente mantenimiento. El recálculo en vivo mientras se escribe lo hace el JS de la página.
 */
@Controller
@RequestMapping("/kilometraje/semanal")
class SeguimientoSemanalController {

	private static final String VISTA = "kilometraje/semanal";

	private final ConsultarSeguimientoUseCase seguimiento;
	private final RegistrarLecturasSemanalesUseCase lote;
	private final Clock reloj;

	SeguimientoSemanalController(ConsultarSeguimientoUseCase seguimiento, RegistrarLecturasSemanalesUseCase lote, Clock reloj) {
		this.seguimiento = seguimiento;
		this.lote = lote;
		this.reloj = reloj;
	}

	@GetMapping
	String ver(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, Model modelo) {
		prepararVista(modelo, SeguimientoSemanalForm.para(fecha != null ? fecha : LocalDate.now(reloj)), Map.of());
		return VISTA;
	}

	@PostMapping
	String guardar(@Valid @ModelAttribute("form") SeguimientoSemanalForm form, BindingResult resultado, Model modelo,
			RedirectAttributes flash) {
		Map<Long, String> errores = Map.of();
		if (!resultado.hasErrors()) {
			List<LecturaPorVehiculo> filas = form.getKilometrajes().entrySet().stream()
				.filter(entrada -> entrada.getValue() != null)
				.map(entrada -> new LecturaPorVehiculo(entrada.getKey(), entrada.getValue()))
				.toList();
			if (filas.isEmpty()) {
				resultado.reject("sinLecturas", "Escribe al menos una lectura para guardar.");
			}
			else {
				ResultadoLote resultadoLote = lote.registrar(form.getFecha(), filas);
				if (!resultadoLote.huboErrores()) {
					flash.addFlashAttribute("exito", resumen(resultadoLote));
					return "redirect:/kilometraje/semanal";
				}
				errores = resultadoLote.errores();
				modelo.addAttribute("resumenParcial", resumen(resultadoLote));
			}
		}
		prepararVista(modelo, form, errores);
		return VISTA;
	}

	private static String resumen(ResultadoLote resultado) {
		StringBuilder texto = new StringBuilder();
		texto.append(resultado.nuevas()).append(resultado.nuevas() == 1 ? " lectura registrada" : " lecturas registradas");
		if (resultado.reemplazadas() > 0) {
			texto.append(", ").append(resultado.reemplazadas()).append(resultado.reemplazadas() == 1 ? " corregida" : " corregidas");
		}
		if (resultado.sinCambios() > 0) {
			texto.append(", ").append(resultado.sinCambios()).append(" sin cambios");
		}
		return texto.toString();
	}

	private void prepararVista(Model modelo, SeguimientoSemanalForm form, Map<Long, String> errores) {
		var filas = seguimiento.seguimientoSemanal();
		modelo.addAttribute("form", form);
		modelo.addAttribute("filas", filas);
		modelo.addAttribute("errores", errores);
		modelo.addAttribute("pendientes", filas.stream().filter(fila -> fila.pendiente()).count());
		modelo.addAttribute("hoy", LocalDate.now(reloj));
	}
}
