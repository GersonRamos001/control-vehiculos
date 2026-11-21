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

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarLocalidadesUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

import jakarta.validation.Valid;

/** Adaptador de entrada web: administración de localidades. */
@Controller
@RequestMapping(LocalidadController.BASE)
class LocalidadController {

	static final String BASE = "/configuracion/localidades";
	private static final String VISTA_LISTA = "configuracion/catalogo-lista";
	private static final String VISTA_FORMULARIO = "configuracion/catalogo-formulario";
	private static final String REDIRIGIR_A_LISTA = "redirect:" + BASE;

	private final GestionarLocalidadesUseCase localidades;

	LocalidadController(GestionarLocalidadesUseCase localidades) {
		this.localidades = localidades;
	}

	@GetMapping
	String listar(Model modelo) {
		modelo.addAttribute("filas", localidades.listar().stream()
			.map(l -> FilaCatalogo.de(BASE, l.id().valor(), l.codigo().valor(), l.nombre(), l.activo()))
			.toList());
		modelo.addAttribute("titulo", "Localidades");
		modelo.addAttribute("seccion", "localidades");
		modelo.addAttribute("urlNuevo", BASE + "/nuevo");
		return VISTA_LISTA;
	}

	@GetMapping("/nuevo")
	String nuevo(Model modelo) {
		prepararFormulario(modelo, CatalogoForm.vacio(), null);
		return VISTA_FORMULARIO;
	}

	@PostMapping
	String crear(@Valid @ModelAttribute("form") CatalogoForm form, BindingResult resultado, Model modelo,
			RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				Localidad creada = localidades.crear(form.getCodigo(), form.getNombre());
				flash.addFlashAttribute("exito", "Localidad " + creada.codigo().valor() + " creada");
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
		Localidad localidad = localidades.buscar(id);
		prepararFormulario(modelo, CatalogoForm.de(localidad.codigo().valor(), localidad.nombre()), id);
		return VISTA_FORMULARIO;
	}

	@PostMapping("/{id}")
	String actualizar(@PathVariable long id, @Valid @ModelAttribute("form") CatalogoForm form, BindingResult resultado,
			Model modelo, RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				Localidad actualizada = localidades.renombrar(id, form.getNombre());
				flash.addFlashAttribute("exito", "Localidad " + actualizada.codigo().valor() + " actualizada");
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
		Localidad localidad = localidades.cambiarEstado(id, true);
		flash.addFlashAttribute("exito", "Localidad " + localidad.codigo().valor() + " activada");
		return REDIRIGIR_A_LISTA;
	}

	@PostMapping("/{id}/desactivar")
	String desactivar(@PathVariable long id, RedirectAttributes flash) {
		Localidad localidad = localidades.cambiarEstado(id, false);
		flash.addFlashAttribute("exito", "Localidad " + localidad.codigo().valor() + " desactivada");
		return REDIRIGIR_A_LISTA;
	}

	private void prepararFormulario(Model modelo, CatalogoForm form, Long idEnEdicion) {
		modelo.addAttribute("form", form);
		modelo.addAttribute("edicion", idEnEdicion != null);
		modelo.addAttribute("titulo", idEnEdicion == null ? "Nueva localidad" : "Editar localidad");
		modelo.addAttribute("seccion", "localidades");
		modelo.addAttribute("accion", idEnEdicion == null ? BASE : BASE + "/" + idEnEdicion);
		modelo.addAttribute("urlLista", BASE);
	}
}
