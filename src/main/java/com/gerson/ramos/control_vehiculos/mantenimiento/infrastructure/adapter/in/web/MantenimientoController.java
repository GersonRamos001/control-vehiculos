package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.in.web;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarHistorialUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarProgramacionUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoComando;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

import jakarta.validation.Valid;

/** Adaptador de entrada web: historial de mantenimientos de un vehículo y registro de uno nuevo. */
@Controller
@RequestMapping("/vehiculos/{vehiculoId}/mantenimientos")
class MantenimientoController {

	private static final String VISTA_HISTORIAL = "mantenimientos/historial";
	private static final String VISTA_FORMULARIO = "mantenimientos/formulario";

	private final ConsultarVehiculosUseCase vehiculos;
	private final ConsultarHistorialUseCase historial;
	private final ConsultarProgramacionUseCase programacion;
	private final RegistrarMantenimientoUseCase registrar;
	private final ConsultarCatalogosUseCase catalogos;
	private final Clock reloj;

	MantenimientoController(ConsultarVehiculosUseCase vehiculos, ConsultarHistorialUseCase historial,
			ConsultarProgramacionUseCase programacion, RegistrarMantenimientoUseCase registrar,
			ConsultarCatalogosUseCase catalogos, Clock reloj) {
		this.vehiculos = vehiculos;
		this.historial = historial;
		this.programacion = programacion;
		this.registrar = registrar;
		this.catalogos = catalogos;
		this.reloj = reloj;
	}

	@GetMapping
	String historial(@PathVariable long vehiculoId, Model modelo) {
		Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId);
		modelo.addAttribute("vehiculoId", vehiculoId);
		modelo.addAttribute("placa", vehiculo.placa().valor());
		modelo.addAttribute("activo", vehiculo.activo());
		modelo.addAttribute("historial", historial.historialDe(vehiculoId));
		modelo.addAttribute("programacion", programacion.programacionDe(vehiculoId));
		return VISTA_HISTORIAL;
	}

	@GetMapping("/nuevo")
	String nuevo(@PathVariable long vehiculoId, Model modelo, RedirectAttributes flash) {
		Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId);
		if (!vehiculo.activo()) {
			flash.addFlashAttribute("error", "El vehículo " + vehiculo.placa().valor()
					+ " está inactivo: actívalo para registrarle mantenimientos");
			return "redirect:/vehiculos/" + vehiculoId + "/mantenimientos";
		}
		prepararFormulario(modelo, vehiculo, MantenimientoForm.para(LocalDate.now(reloj)));
		return VISTA_FORMULARIO;
	}

	@PostMapping
	String registrar(@PathVariable long vehiculoId, @Valid @ModelAttribute("form") MantenimientoForm form,
			BindingResult resultado, Model modelo, RedirectAttributes flash) {
		Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId);
		if (!resultado.hasErrors()) {
			try {
				registrar.registrar(new RegistrarMantenimientoComando(vehiculoId, form.getFecha(), form.getKilometraje(),
						form.getRutinaId(), form.getObservaciones()));
				flash.addFlashAttribute("exito", "Mantenimiento registrado para " + vehiculo.placa().valor());
				return "redirect:/vehiculos/" + vehiculoId + "/mantenimientos";
			}
			catch (ReglaDeNegocioException | DataIntegrityViolationException excepcion) {
				registrarError(resultado, excepcion);
			}
		}
		prepararFormulario(modelo, vehiculo, form);
		return VISTA_FORMULARIO;
	}

	/** Muestra el error junto al campo cuando el dominio lo indica; en otro caso, como error general. */
	private static void registrarError(BindingResult resultado, Exception excepcion) {
		if (excepcion instanceof ConflictoDeDatosException conflicto) {
			resultado.rejectValue(conflicto.campo(), "conflicto", conflicto.getMessage());
		}
		else if (excepcion instanceof DataIntegrityViolationException) {
			resultado.reject("integridad", "No se pudo guardar: el vehículo o la rutina ya no son válidos.");
		}
		else {
			resultado.reject("regla", Objects.requireNonNullElse(excepcion.getMessage(), "Datos inválidos"));
		}
	}

	private void prepararFormulario(Model modelo, Vehiculo vehiculo, MantenimientoForm form) {
		modelo.addAttribute("form", form);
		modelo.addAttribute("vehiculoId", vehiculo.id().valor());
		modelo.addAttribute("placa", vehiculo.placa().valor());
		modelo.addAttribute("rutinas", catalogos.listarRutinas(true));
		modelo.addAttribute("hoy", LocalDate.now(reloj));
	}
}
