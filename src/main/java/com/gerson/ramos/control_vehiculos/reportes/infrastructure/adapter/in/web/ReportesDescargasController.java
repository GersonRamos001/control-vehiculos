package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroPendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientesUseCase;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizadosUseCase;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientosUseCase;
import com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web.ReporteTabular.Nombres;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

import jakarta.validation.Valid;

/**
 * Adaptador de entrada web: descargas de los reportes en CSV y en Excel (.xlsx), con los mismos filtros que las
 * pantallas. El separador del CSV es configurable con {@code app.reportes.separador-csv} (por defecto {@code ;}, el de
 * Excel en configuración regional hispana; use {@code ,} para configuración inglesa).
 */
@Controller
@RequestMapping("/reportes")
class ReportesDescargasController {

	private static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv;charset=UTF-8");
	private static final MediaType XLSX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	private final ReportePendientesUseCase pendientes;
	private final ReporteRealizadosUseCase realizados;
	private final ReporteVencimientosUseCase vencimientos;
	private final ConsultarCatalogosUseCase catalogos;
	private final Clock reloj;
	private final char separador;

	ReportesDescargasController(ReportePendientesUseCase pendientes, ReporteRealizadosUseCase realizados,
			ReporteVencimientosUseCase vencimientos, ConsultarCatalogosUseCase catalogos, Clock reloj,
			@Value("${app.reportes.separador-csv:;}") String separador) {
		if (separador.length() != 1) {
			throw new IllegalStateException("app.reportes.separador-csv debe ser un solo carácter (por ejemplo ';' o ','), pero es '"
					+ separador + "'");
		}
		this.pendientes = pendientes;
		this.realizados = realizados;
		this.vencimientos = vencimientos;
		this.catalogos = catalogos;
		this.reloj = reloj;
		this.separador = separador.charAt(0);
	}

	// ---- vehículos pendientes ----

	@GetMapping("/pendientes/csv")
	ResponseEntity<byte[]> pendientesCsv(@Valid @ModelAttribute FiltroPendientesForm filtro, BindingResult resultado) {
		return csv("pendientes", tablaPendientes(filtro, resultado));
	}

	@GetMapping("/pendientes/xlsx")
	ResponseEntity<byte[]> pendientesXlsx(@Valid @ModelAttribute FiltroPendientesForm filtro, BindingResult resultado) {
		return xlsx("pendientes", tablaPendientes(filtro, resultado));
	}

	private TablaReporte tablaPendientes(FiltroPendientesForm filtro, BindingResult resultado) {
		exigirFiltroValido(resultado);
		FiltroPendientes consulta = filtro.aFiltro();
		return ReporteTabular.pendientes(pendientes.generar(consulta), consulta, nombres(), LocalDate.now(reloj));
	}

	// ---- mantenimientos realizados ----

	@GetMapping("/realizados/csv")
	ResponseEntity<byte[]> realizadosCsv(@Valid @ModelAttribute FiltroRealizadosForm filtro, BindingResult resultado) {
		return csv("realizados", tablaRealizados(filtro, resultado));
	}

	@GetMapping("/realizados/xlsx")
	ResponseEntity<byte[]> realizadosXlsx(@Valid @ModelAttribute FiltroRealizadosForm filtro, BindingResult resultado) {
		return xlsx("realizados", tablaRealizados(filtro, resultado));
	}

	private TablaReporte tablaRealizados(FiltroRealizadosForm filtro, BindingResult resultado) {
		exigirFiltroValido(resultado);
		FiltroRealizados consulta = filtro.aFiltro();
		return ReporteTabular.realizados(realizados.generar(consulta), consulta, nombres(), LocalDate.now(reloj));
	}

	// ---- próximos vencimientos ----

	@GetMapping("/vencimientos/csv")
	ResponseEntity<byte[]> vencimientosCsv(@Valid @ModelAttribute FiltroVencimientosForm filtro, BindingResult resultado) {
		return csv("vencimientos", tablaVencimientos(filtro, resultado));
	}

	@GetMapping("/vencimientos/xlsx")
	ResponseEntity<byte[]> vencimientosXlsx(@Valid @ModelAttribute FiltroVencimientosForm filtro, BindingResult resultado) {
		return xlsx("vencimientos", tablaVencimientos(filtro, resultado));
	}

	private TablaReporte tablaVencimientos(FiltroVencimientosForm filtro, BindingResult resultado) {
		exigirFiltroValido(resultado);
		FiltroVencimientos consulta = filtro.aFiltro();
		return ReporteTabular.vencimientos(vencimientos.generar(consulta), consulta, nombres(), LocalDate.now(reloj));
	}

	// ---- comunes ----

	/** Una descarga no puede mostrar errores junto a campos: si el filtro es inválido responde con la página de error. */
	private static void exigirFiltroValido(BindingResult resultado) {
		if (resultado.hasErrors()) {
			throw new ReglaDeNegocioException("Los filtros del reporte no son válidos: revisa las fechas y los números.");
		}
	}

	private Nombres nombres() {
		Map<Long, String> localidades = new HashMap<>();
		for (Localidad localidad : catalogos.listarLocalidades(false)) {
			localidades.put(localidad.id().valor(), localidad.nombre());
		}
		Map<Long, String> tiposUso = new HashMap<>();
		for (TipoUso tipoUso : catalogos.listarTiposUso(false)) {
			tiposUso.put(tipoUso.id().valor(), tipoUso.nombre());
		}
		Map<Long, String> rutinas = new HashMap<>();
		for (RutinaMantenimiento rutina : catalogos.listarRutinas(false)) {
			rutinas.put(rutina.id().valor(), rutina.codigo().valor());
		}
		return new Nombres(localidades, tiposUso, rutinas);
	}

	private ResponseEntity<byte[]> csv(String reporte, TablaReporte tabla) {
		return descarga(reporte, "csv", TEXT_CSV, ExportadorCsv.csv(tabla, separador));
	}

	private ResponseEntity<byte[]> xlsx(String reporte, TablaReporte tabla) {
		return descarga(reporte, "xlsx", XLSX, LibroExcel.generar(tabla));
	}

	private ResponseEntity<byte[]> descarga(String reporte, String extension, MediaType tipo, byte[] contenido) {
		String archivo = "reporte-" + reporte + "-" + LocalDate.now(reloj) + "." + extension;
		return ResponseEntity.ok()
			.contentType(tipo)
			.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(archivo).build().toString())
			.cacheControl(CacheControl.noStore())
			.body(contenido);
	}
}
