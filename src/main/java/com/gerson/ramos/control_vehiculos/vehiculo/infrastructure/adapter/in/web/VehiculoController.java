package com.gerson.ramos.control_vehiculos.vehiculo.infrastructure.adapter.in.web;

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeConcurrenciaException;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ActualizarVehiculoComando;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ActualizarVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.CambiarEstadoVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarVehiculoComando;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

import jakarta.validation.Valid;

/** Adaptador de entrada web: pantallas de listado, alta, edición y baja/alta lógica de vehículos. */
@Controller
@RequestMapping("/vehiculos")
class VehiculoController {

	private static final String VISTA_LISTA = "vehiculos/lista";
	private static final String VISTA_FORMULARIO = "vehiculos/formulario";
	private static final String REDIRIGIR_A_LISTA = "redirect:/vehiculos";

	private final ConsultarVehiculosUseCase consultar;
	private final RegistrarVehiculoUseCase registrar;
	private final ActualizarVehiculoUseCase actualizar;
	private final CambiarEstadoVehiculoUseCase cambiarEstado;
	private final ConsultarCatalogosUseCase catalogos;

	VehiculoController(ConsultarVehiculosUseCase consultar, RegistrarVehiculoUseCase registrar,
			ActualizarVehiculoUseCase actualizar, CambiarEstadoVehiculoUseCase cambiarEstado,
			ConsultarCatalogosUseCase catalogos) {
		this.consultar = consultar;
		this.registrar = registrar;
		this.actualizar = actualizar;
		this.cambiarEstado = cambiarEstado;
		this.catalogos = catalogos;
	}

	@GetMapping
	String listar(@RequestParam(defaultValue = "ACTIVOS") FiltroEstado filtro, Model modelo) {
		modelo.addAttribute("vehiculos", consultar.listar(filtro));
		modelo.addAttribute("filtro", filtro);
		return VISTA_LISTA;
	}

	@GetMapping("/nuevo")
	String nuevo(Model modelo) {
		VehiculoForm form = VehiculoForm.vacio();
		prepararFormulario(modelo, form, null);
		return VISTA_FORMULARIO;
	}

	@PostMapping
	String crear(@Valid @ModelAttribute("form") VehiculoForm form, BindingResult resultado, Model modelo,
			RedirectAttributes flash) {
		if (!resultado.hasErrors()) {
			try {
				Vehiculo vehiculo = registrar.registrar(new RegistrarVehiculoComando(form.getPlaca(),
						form.getCodigoEquipo(), form.getLocalidadId(), form.getTipoUsoId(), form.getRutinaId()));
				flash.addFlashAttribute("exito", "Vehículo " + vehiculo.placa().valor() + " registrado");
				return REDIRIGIR_A_LISTA;
			}
			catch (ReglaDeNegocioException | DataIntegrityViolationException excepcion) {
				registrarError(resultado, excepcion);
			}
		}
		prepararFormulario(modelo, form, null);
		return VISTA_FORMULARIO;
	}

	@GetMapping("/{id}/editar")
	String editar(@PathVariable long id, Model modelo, RedirectAttributes flash) {
		Vehiculo vehiculo = consultar.buscarPorId(id);
		if (!vehiculo.activo()) {
			flash.addFlashAttribute("error", "El vehículo " + vehiculo.placa().valor() + " está inactivo: actívalo antes de editarlo");
			return REDIRIGIR_A_LISTA + "?filtro=TODOS";
		}
		prepararFormulario(modelo, VehiculoForm.desde(vehiculo), id);
		return VISTA_FORMULARIO;
	}

	@PostMapping("/{id}")
	String actualizar(@PathVariable long id, @Valid @ModelAttribute("form") VehiculoForm form, BindingResult resultado,
			Model modelo, RedirectAttributes flash) {
		if (form.getVersion() == null) {
			resultado.reject("version", "Falta la versión del vehículo; recarga la página");
		}
		if (!resultado.hasErrors()) {
			try {
				Vehiculo vehiculo = actualizar.actualizar(new ActualizarVehiculoComando(id, form.getVersion(),
						form.getPlaca(), form.getCodigoEquipo(), form.getLocalidadId(), form.getTipoUsoId(),
						form.getRutinaId()));
				flash.addFlashAttribute("exito", "Vehículo " + vehiculo.placa().valor() + " actualizado");
				return REDIRIGIR_A_LISTA;
			}
			catch (ReglaDeNegocioException | DataIntegrityViolationException | ObjectOptimisticLockingFailureException excepcion) {
				registrarError(resultado, excepcion);
			}
		}
		prepararFormulario(modelo, form, id);
		return VISTA_FORMULARIO;
	}

	@PostMapping("/{id}/desactivar")
	String desactivar(@PathVariable long id, RedirectAttributes flash) {
		Vehiculo vehiculo = cambiarEstado.desactivar(id);
		flash.addFlashAttribute("exito", "Vehículo " + vehiculo.placa().valor() + " desactivado");
		return REDIRIGIR_A_LISTA;
	}

	@PostMapping("/{id}/activar")
	String activar(@PathVariable long id, RedirectAttributes flash) {
		Vehiculo vehiculo = cambiarEstado.activar(id);
		flash.addFlashAttribute("exito", "Vehículo " + vehiculo.placa().valor() + " activado");
		return REDIRIGIR_A_LISTA + "?filtro=TODOS";
	}

	/** Muestra el error junto al campo cuando el dominio lo indica; en otro caso, como error general del formulario. */
	private static void registrarError(BindingResult resultado, Exception excepcion) {
		if (excepcion instanceof ConflictoDeDatosException conflicto) {
			resultado.rejectValue(conflicto.campo(), "conflicto", conflicto.getMessage());
		}
		else if (excepcion instanceof ConflictoDeConcurrenciaException || excepcion instanceof ObjectOptimisticLockingFailureException) {
			resultado.reject("concurrencia", "Otro usuario modificó este vehículo. Recarga la página y vuelve a intentarlo.");
		}
		else if (excepcion instanceof DataIntegrityViolationException) {
			resultado.reject("duplicado", "La placa o el código de equipo ya existe.");
		}
		else {
			resultado.reject("regla", Objects.requireNonNullElse(excepcion.getMessage(), "Datos inválidos"));
		}
	}

	/** Carga el formulario y las listas de catálogos (activos más el valor actual si ya está inactivo). */
	private void prepararFormulario(Model modelo, VehiculoForm form, Long idEnEdicion) {
		Long localidadActual = form.getLocalidadId();
		Long tipoUsoActual = form.getTipoUsoId();
		Long rutinaActual = form.getRutinaId();
		modelo.addAttribute("form", form);
		modelo.addAttribute("edicion", idEnEdicion != null);
		modelo.addAttribute("accion", idEnEdicion == null ? "/vehiculos" : "/vehiculos/" + idEnEdicion);
		modelo.addAttribute("localidades", catalogos.listarLocalidades(false).stream()
			.filter(l -> l.activo() || Objects.equals(l.id().valor(), localidadActual)).toList());
		modelo.addAttribute("tiposUso", catalogos.listarTiposUso(false).stream()
			.filter(t -> t.activo() || Objects.equals(t.id().valor(), tipoUsoActual)).toList());
		modelo.addAttribute("rutinas", catalogos.listarRutinas(false).stream()
			.filter(r -> r.activo() || Objects.equals(r.id().valor(), rutinaActual)).toList());
	}
}
