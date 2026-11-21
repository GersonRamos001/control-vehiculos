package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.in.web;

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

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarLecturasUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarSeguimientoUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.EliminarUltimaLecturaUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaComando;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoRegistro;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

import jakarta.validation.Valid;

/** Adaptador de entrada web: historial de lecturas de un vehículo, alta/corrección individual y borrado de la última. */
@Controller
@RequestMapping("/vehiculos/{vehiculoId}/kilometraje")
class KilometrajeController {

	private static final String VISTA = "kilometraje/historial";

	private final ConsultarVehiculosUseCase vehiculos;
	private final ConsultarLecturasUseCase historial;
	private final ConsultarSeguimientoUseCase seguimiento;
	private final RegistrarLecturaUseCase registrar;
	private final EliminarUltimaLecturaUseCase eliminarUltima;
	private final Clock reloj;

	KilometrajeController(ConsultarVehiculosUseCase vehiculos, ConsultarLecturasUseCase historial,
			ConsultarSeguimientoUseCase seguimiento, RegistrarLecturaUseCase registrar,
			EliminarUltimaLecturaUseCase eliminarUltima, Clock reloj) {
		this.vehiculos = vehiculos;
		this.historial = historial;
		this.seguimiento = seguimiento;
		this.registrar = registrar;
		this.eliminarUltima = eliminarUltima;
		this.reloj = reloj;
	}

	@GetMapping
	String ver(@PathVariable long vehiculoId, Model modelo) {
		prepararVista(modelo, vehiculoId, LecturaForm.para(LocalDate.now(reloj)));
		return VISTA;
	}

	@PostMapping
	String registrar(@PathVariable long vehiculoId, @Valid @ModelAttribute("form") LecturaForm form, BindingResult resultado,
			Model modelo, RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				ResultadoRegistro resultadoRegistro = registrar
					.registrar(new RegistrarLecturaComando(vehiculoId, form.getFecha(), form.getKilometraje()));
				flash.addFlashAttribute("exito", mensaje(resultadoRegistro));
				return "redirect:/vehiculos/" + vehiculoId + "/kilometraje";
			}
			catch (ReglaDeNegocioException | DataIntegrityViolationException excepcion) {
				registrarError(resultado, excepcion);
			}
		}
		prepararVista(modelo, vehiculoId, form);
		return VISTA;
	}

	@PostMapping("/ultima/eliminar")
	String eliminarUltima(@PathVariable long vehiculoId, RedirectAttributes flash) {
		LecturaKilometraje eliminada = eliminarUltima.eliminarUltima(vehiculoId);
		flash.addFlashAttribute("exito", "Lectura del " + eliminada.fecha() + " (" + eliminada.kilometraje() + " km) eliminada");
		return "redirect:/vehiculos/" + vehiculoId + "/kilometraje";
	}

	private static String mensaje(ResultadoRegistro resultado) {
		LecturaKilometraje lectura = resultado.lectura();
		return switch (resultado.tipo()) {
			case NUEVA -> "Lectura del " + lectura.fecha() + " registrada";
			case REEMPLAZADA -> "Lectura del " + lectura.fecha() + " corregida a " + lectura.kilometraje() + " km";
			case SIN_CAMBIOS -> "La lectura del " + lectura.fecha() + " ya estaba registrada con ese kilometraje";
		};
	}

	/** Muestra el error junto al campo cuando el dominio lo indica; en otro caso, como error general. */
	private static void registrarError(BindingResult resultado, Exception excepcion) {
		if (excepcion instanceof ConflictoDeDatosException conflicto) {
			resultado.rejectValue(conflicto.campo(), "conflicto", conflicto.getMessage());
		}
		else if (excepcion instanceof DataIntegrityViolationException) {
			resultado.reject("integridad", "No se pudo guardar la lectura; vuelve a intentarlo.");
		}
		else {
			resultado.reject("regla", Objects.requireNonNullElse(excepcion.getMessage(), "Datos inválidos"));
		}
	}

	private void prepararVista(Model modelo, long vehiculoId, LecturaForm form) {
		Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId);
		modelo.addAttribute("form", form);
		modelo.addAttribute("vehiculoId", vehiculoId);
		modelo.addAttribute("placa", vehiculo.placa().valor());
		modelo.addAttribute("activo", vehiculo.activo());
		modelo.addAttribute("seguimiento", seguimiento.seguimientoDe(vehiculoId));
		modelo.addAttribute("historial", historial.historialDe(vehiculoId));
		modelo.addAttribute("hoy", LocalDate.now(reloj));
	}
}
