package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.EstadoPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientesUseCase;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizadosUseCase;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientosUseCase;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

import jakarta.validation.Valid;

/**
 * Adaptador de entrada web: pantallas de reportes. Los filtros viajan por GET (se pueden guardar en favoritos) y cada
 * pantalla ofrece los enlaces de descarga (Excel y CSV) con los mismos filtros; solo se ofrecen si el reporte se generó.
 */
@Controller
@RequestMapping("/reportes")
class ReportesController {

	private final ReportePendientesUseCase pendientes;
	private final ReporteRealizadosUseCase realizados;
	private final ReporteVencimientosUseCase vencimientos;
	private final ConsultarCatalogosUseCase catalogos;

	ReportesController(ReportePendientesUseCase pendientes, ReporteRealizadosUseCase realizados,
			ReporteVencimientosUseCase vencimientos, ConsultarCatalogosUseCase catalogos) {
		this.pendientes = pendientes;
		this.realizados = realizados;
		this.vencimientos = vencimientos;
		this.catalogos = catalogos;
	}

	@GetMapping
	String indice() {
		return "reportes/indice";
	}

	@GetMapping("/pendientes")
	String pendientes(@Valid @ModelAttribute("filtro") FiltroPendientesForm filtro, BindingResult resultado, Model modelo) {
		if (!resultado.hasErrors()) {
			ReportePendientes reporte = pendientes.generar(filtro.aFiltro());
			modelo.addAttribute("reporte", reporte);
			modelo.addAttribute("csvUrl", EnlacesDeReporte.pendientes("csv", filtro));
			modelo.addAttribute("xlsxUrl", EnlacesDeReporte.pendientes("xlsx", filtro));
		}
		modelo.addAttribute("estados", EstadoPendiente.values());
		modelo.addAttribute("localidades", catalogos.listarLocalidades(false));
		modelo.addAttribute("tiposUso", catalogos.listarTiposUso(false));
		return "reportes/pendientes";
	}

	@GetMapping("/realizados")
	String realizados(@Valid @ModelAttribute("filtro") FiltroRealizadosForm filtro, BindingResult resultado, Model modelo) {
		if (!resultado.hasErrors()) {
			try {
				ReporteRealizados reporte = realizados.generar(filtro.aFiltro());
				// El formulario muestra el período efectivo (con los valores por defecto ya aplicados).
				filtro.setDesde(reporte.rango().desde());
				filtro.setHasta(reporte.rango().hasta());
				modelo.addAttribute("reporte", reporte);
				modelo.addAttribute("csvUrl", EnlacesDeReporte.realizados("csv", filtro, reporte.rango()));
				modelo.addAttribute("xlsxUrl", EnlacesDeReporte.realizados("xlsx", filtro, reporte.rango()));
			}
			catch (ConflictoDeDatosException conflicto) {
				resultado.rejectValue(conflicto.campo(), "conflicto", conflicto.getMessage());
			}
			catch (ReglaDeNegocioException regla) {
				resultado.reject("regla", regla.getMessage());
			}
		}
		modelo.addAttribute("localidades", catalogos.listarLocalidades(false));
		modelo.addAttribute("rutinas", catalogos.listarRutinas(false));
		return "reportes/realizados";
	}

	@GetMapping("/vencimientos")
	String vencimientos(@Valid @ModelAttribute("filtro") FiltroVencimientosForm filtro, BindingResult resultado, Model modelo) {
		if (!resultado.hasErrors()) {
			try {
				ReporteVencimientos reporte = vencimientos.generar(filtro.aFiltro());
				modelo.addAttribute("reporte", reporte);
				modelo.addAttribute("csvUrl", EnlacesDeReporte.vencimientos("csv", filtro));
				modelo.addAttribute("xlsxUrl", EnlacesDeReporte.vencimientos("xlsx", filtro));
			}
			catch (ConflictoDeDatosException conflicto) {
				resultado.rejectValue(conflicto.campo(), "conflicto", conflicto.getMessage());
			}
		}
		modelo.addAttribute("opcionesVencidos", FiltroVencimientosForm.Vencidos.values());
		modelo.addAttribute("localidades", catalogos.listarLocalidades(false));
		return "reportes/vencimientos";
	}
}
