package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ConteoPorClave;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaVencimiento;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroPendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientos;

/**
 * Convierte cada reporte a {@link TablaReporte}. Aquí se definen, una sola vez, las columnas de cada reporte y las notas
 * (filtros aplicados y totales) de la hoja "Resumen".
 */
final class ReporteTabular {

	private ReporteTabular() {
	}

	/** Nombres de los catálogos para describir los filtros (el reporte filtra por id, pero el usuario lee nombres). */
	record Nombres(Map<Long, String> localidades, Map<Long, String> tiposUso, Map<Long, String> rutinas) {

		String localidad(@Nullable Long id) {
			return nombre(localidades, id, "Todas");
		}

		String tipoUso(@Nullable Long id) {
			return nombre(tiposUso, id, "Todos");
		}

		String rutina(@Nullable Long id) {
			return nombre(rutinas, id, "Todas");
		}

		private static String nombre(Map<Long, String> nombres, @Nullable Long id, String porDefecto) {
			return id == null ? porDefecto : nombres.getOrDefault(id, "#" + id);
		}
	}

	static TablaReporte pendientes(ReportePendientes reporte, FiltroPendientes filtro, Nombres nombres, LocalDate emision) {
		List<List<Object>> filas = new ArrayList<>();
		for (FilaPendiente fila : reporte.filas()) {
			filas.add(celdas(fila.placa(), fila.codigoEquipo(), fila.localidad(), fila.tipoUso(), fila.vencido() ? "Vencido" : "Próximo",
					fila.motivo(), fila.ultimoMantenimientoFecha(), fila.ultimoMantenimientoKm(), fila.proximoKm(), fila.kmActual(),
					fila.kmRestantes(), fila.diasRestantes(), fila.fechaReferencia()));
		}
		List<String> notas = List.of("Reporte: Vehículos pendientes de mantenimiento", "Emitido: " + emision,
				"Estado: " + descripcion(filtro.estado()), "Localidad: " + nombres.localidad(filtro.localidadId()),
				"Tipo de uso: " + nombres.tipoUso(filtro.tipoUsoId()), "Vencidos: " + reporte.vencidos(),
				"Próximos: " + reporte.proximos(), "Total de vehículos: " + reporte.filas().size());
		return new TablaReporte("Pendientes",
				List.of("Placa", "Equipo", "Localidad", "Tipo de uso", "Estado", "Motivo", "Último mantenimiento",
						"Km del último mantenimiento", "Próximo km", "Km actual", "Km restantes", "Días restantes", "Fecha de referencia"),
				filas, notas);
	}

	static TablaReporte realizados(ReporteRealizados reporte, FiltroRealizados filtro, Nombres nombres, LocalDate emision) {
		List<List<Object>> filas = new ArrayList<>();
		for (MantenimientoRealizado fila : reporte.filas()) {
			filas.add(celdas(fila.fecha(), fila.placa(), fila.codigoEquipo(), fila.localidad(), fila.rutina(), fila.kilometraje(),
					fila.observaciones()));
		}
		List<String> notas = new ArrayList<>(List.of("Reporte: Mantenimientos realizados", "Emitido: " + emision,
				"Período: " + reporte.rango().desde() + " a " + reporte.rango().hasta(),
				"Localidad: " + nombres.localidad(filtro.localidadId()), "Rutina: " + nombres.rutina(filtro.rutinaId())));
		if (filtro.placa() != null && !filtro.placa().isBlank()) {
			notas.add("Placa contiene: " + filtro.placa().strip());
		}
		notas.add("Total de mantenimientos: " + reporte.total());
		notas.add("Por rutina: " + conteos(reporte.porRutina()));
		notas.add("Por localidad: " + conteos(reporte.porLocalidad()));
		return new TablaReporte("Realizados", List.of("Fecha", "Placa", "Equipo", "Localidad", "Rutina", "Kilometraje", "Observaciones"),
				filas, List.copyOf(notas));
	}

	static TablaReporte vencimientos(ReporteVencimientos reporte, FiltroVencimientos filtro, Nombres nombres, LocalDate emision) {
		List<List<Object>> filas = new ArrayList<>();
		for (FilaVencimiento fila : reporte.filas()) {
			filas.add(celdas(fila.placa(), fila.codigoEquipo(), fila.localidad(), fila.ultimoMantenimientoFecha(),
					fila.ultimoMantenimientoKm(), fila.proximoKm(), fila.kmRestantes(), fila.fechaReferencia(), fila.diasRestantes(),
					fila.vencido() ? "Vencido" : "Por vencer"));
		}
		List<String> notas = List.of("Reporte: Próximos vencimientos", "Emitido: " + emision,
				"Horizonte: " + filtro.horizonteDias() + " días o " + filtro.horizonteKm() + " km",
				"Ya vencidos: " + (filtro.incluirVencidos() ? "incluidos" : "omitidos"),
				"Localidad: " + nombres.localidad(filtro.localidadId()), "Ya vencidos (cantidad): " + reporte.vencidos(),
				"Por vencer: " + reporte.porVencer(), "Total de vehículos: " + reporte.filas().size());
		return new TablaReporte("Vencimientos",
				List.of("Placa", "Equipo", "Localidad", "Último mantenimiento", "Km del último mantenimiento", "Próximo km",
						"Km restantes", "Fecha de referencia", "Días restantes", "Estado"),
				filas, notas);
	}

	/** Lista de celdas que admite nulos (List.of no los admite). */
	private static List<Object> celdas(Object... valores) {
		return Arrays.asList(valores);
	}

	private static String descripcion(com.gerson.ramos.control_vehiculos.reportes.application.port.in.EstadoPendiente estado) {
		return switch (estado) {
			case TODOS -> "Vencidos y próximos";
			case VENCIDOS -> "Solo vencidos";
			case PROXIMOS -> "Solo próximos";
		};
	}

	private static String conteos(List<ConteoPorClave> conteos) {
		return conteos.isEmpty() ? "—"
				: conteos.stream().map(c -> c.clave() + " (" + c.cantidad() + ")").collect(Collectors.joining(", "));
	}
}
