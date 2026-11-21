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

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarTiposUsoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

import jakarta.validation.Valid;

/** Adaptador de entrada web: administración de tipos de uso. */
@Controller
@RequestMapping(TipoUsoController.BASE)
class TipoUsoController {

	static final String BASE = "/configuracion/tipos-uso";
	private static final String VISTA_LISTA = "configuracion/catalogo-lista";
	private static final String VISTA_FORMULARIO = "configuracion/catalogo-formulario";
	private static final String REDIRIGIR_A_LISTA = "redirect:" + BASE;

	private final GestionarTiposUsoUseCase tiposUso;

	TipoUsoController(GestionarTiposUsoUseCase tiposUso) {
		this.tiposUso = tiposUso;
	}

	@GetMapping
	String listar(Model modelo) {
		modelo.addAttribute("filas", tiposUso.listar().stream()
			.map(t -> FilaCatalogo.de(BASE, t.id().valor(), t.codigo().valor(), t.nombre(), t.activo()))
			.toList());
		modelo.addAttribute("titulo", "Tipos de uso");
		modelo.addAttribute("seccion", "tipos-uso");
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
				TipoUso creado = tiposUso.crear(form.getCodigo(), form.getNombre());
				flash.addFlashAttribute("exito", "Tipo de uso " + creado.codigo().valor() + " creado");
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
		TipoUso tipoUso = tiposUso.buscar(id);
		prepararFormulario(modelo, CatalogoForm.de(tipoUso.codigo().valor(), tipoUso.nombre()), id);
		return VISTA_FORMULARIO;
	}

	@PostMapping("/{id}")
	String actualizar(@PathVariable long id, @Valid @ModelAttribute("form") CatalogoForm form, BindingResult resultado,
			Model modelo, RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				TipoUso actualizado = tiposUso.renombrar(id, form.getNombre());
				flash.addFlashAttribute("exito", "Tipo de uso " + actualizado.codigo().valor() + " actualizado");
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
		TipoUso tipoUso = tiposUso.cambiarEstado(id, true);
		flash.addFlashAttribute("exito", "Tipo de uso " + tipoUso.codigo().valor() + " activado");
		return REDIRIGIR_A_LISTA;
	}

	@PostMapping("/{id}/desactivar")
	String desactivar(@PathVariable long id, RedirectAttributes flash) {
		TipoUso tipoUso = tiposUso.cambiarEstado(id, false);
		flash.addFlashAttribute("exito", "Tipo de uso " + tipoUso.codigo().valor() + " desactivado");
		return REDIRIGIR_A_LISTA;
	}

	private void prepararFormulario(Model modelo, CatalogoForm form, Long idEnEdicion) {
		modelo.addAttribute("form", form);
		modelo.addAttribute("edicion", idEnEdicion != null);
		modelo.addAttribute("titulo", idEnEdicion == null ? "Nuevo tipo de uso" : "Editar tipo de uso");
		modelo.addAttribute("seccion", "tipos-uso");
		modelo.addAttribute("accion", idEnEdicion == null ? BASE : BASE + "/" + idEnEdicion);
		modelo.addAttribute("urlLista", BASE);
	}
}
