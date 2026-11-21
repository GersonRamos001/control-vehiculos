package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

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

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarRutinasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

import jakarta.validation.Valid;

/** Adaptador de entrada web: administración de rutinas de mantenimiento (usa las plantillas de catálogo simple). */
@Controller
@RequestMapping(RutinaController.BASE)
class RutinaController {

	static final String BASE = "/configuracion/rutinas";
	private static final String VISTA_LISTA = "configuracion/catalogo-lista";
	private static final String VISTA_FORMULARIO = "configuracion/catalogo-formulario";
	private static final String REDIRIGIR_A_LISTA = "redirect:" + BASE;

	private final GestionarRutinasUseCase rutinas;

	RutinaController(GestionarRutinasUseCase rutinas) {
		this.rutinas = rutinas;
	}

	@GetMapping
	String listar(Model modelo) {
		modelo.addAttribute("filas", rutinas.listar().stream()
			.map(r -> FilaCatalogo.de(BASE, r.id().valor(), r.codigo().valor(), r.nombre(), r.activo()))
			.toList());
		modelo.addAttribute("titulo", "Rutinas de mantenimiento");
		modelo.addAttribute("seccion", "rutinas");
		modelo.addAttribute("urlNuevo", BASE + "/nuevo");
		return VISTA_LISTA;
	}

	@GetMapping("/nuevo")
	String nueva(Model modelo) {
		prepararFormulario(modelo, CatalogoForm.vacio(), null);
		return VISTA_FORMULARIO;
	}

	@PostMapping
	String crear(@Valid @ModelAttribute("form") CatalogoForm form, BindingResult resultado, Model modelo,
			RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				RutinaMantenimiento creada = rutinas.crear(form.getCodigo(), form.getNombre());
				flash.addFlashAttribute("exito", "Rutina " + creada.codigo().valor() + " creada");
				return REDIRIGIR_A_LISTA;
			}
			catch (ReglaDeNegocioException | DataIntegrityViolationException excepcion) {
				ErroresDeFormulario.registrar(resultado, excepcion);
			}
		}
		prepararFormulario(modelo, form, null);
		return VISTA_FORMULARIO;
	}

	@GetMapping("/{id}/editar")
	String editar(@PathVariable long id, Model modelo) {
		RutinaMantenimiento rutina = rutinas.buscar(id);
		prepararFormulario(modelo, CatalogoForm.de(rutina.codigo().valor(), rutina.nombre()), id);
		return VISTA_FORMULARIO;
	}

	@PostMapping("/{id}")
	String actualizar(@PathVariable long id, @Valid @ModelAttribute("form") CatalogoForm form, BindingResult resultado,
			Model modelo, RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				RutinaMantenimiento actualizada = rutinas.renombrar(id, form.getNombre());
				flash.addFlashAttribute("exito", "Rutina " + actualizada.codigo().valor() + " actualizada");
				return REDIRIGIR_A_LISTA;
			}
			catch (ReglaDeNegocioException excepcion) {
				ErroresDeFormulario.registrar(resultado, excepcion);
			}
		}
		prepararFormulario(modelo, form, id);
		return VISTA_FORMULARIO;
	}

	@PostMapping("/{id}/activar")
	String activar(@PathVariable long id, RedirectAttributes flash) {
		RutinaMantenimiento rutina = rutinas.cambiarEstado(id, true);
		flash.addFlashAttribute("exito", "Rutina " + rutina.codigo().valor() + " activada");
		return REDIRIGIR_A_LISTA;
	}

	@PostMapping("/{id}/desactivar")
	String desactivar(@PathVariable long id, RedirectAttributes flash) {
		RutinaMantenimiento rutina = rutinas.cambiarEstado(id, false);
		flash.addFlashAttribute("exito", "Rutina " + rutina.codigo().valor() + " desactivada");
		return REDIRIGIR_A_LISTA;
	}

	private void prepararFormulario(Model modelo, CatalogoForm form, Long idEnEdicion) {
		modelo.addAttribute("form", form);
		modelo.addAttribute("edicion", idEnEdicion != null);
		modelo.addAttribute("titulo", idEnEdicion == null ? "Nueva rutina" : "Editar rutina");
		modelo.addAttribute("seccion", "rutinas");
		modelo.addAttribute("accion", idEnEdicion == null ? BASE : BASE + "/" + idEnEdicion);
		modelo.addAttribute("urlLista", BASE);
	}
}
