package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ConteoPorClave;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.EstadoPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaVencimiento;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroPendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.domain.model.RangoDeFechas;
import com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web.ReporteTabular.Nombres;

class ReporteTabularTest {

	private static final LocalDate EMISION = LocalDate.of(2026, 11, 21);
	private static final Nombres NOMBRES = new Nombres(Map.of(1L, "San Pedro Sula", 2L, "Tegucigalpa"), Map.of(1L, "Operativo"),
			Map.of(1L, "10K"));

	@Test
	void pendientes_define_las_columnas_una_vez_y_cada_fila_coincide_con_ellas() {
		FilaPendiente completa = new FilaPendiente(1, "ABC-123", "EQ-001", "San Pedro Sula", "Operativo", Severidad.VENCIDA,
				"Vencido: se pasó por 100 km", LocalDate.of(2026, 5, 1), 100_000, 110_000L, 110_100L, -100L, -5L, LocalDate.of(2026, 11, 16));
		FilaPendiente sinDatos = new FilaPendiente(2, "DEF-456", "EQ-002", "Tegucigalpa", "Relevo", Severidad.PROXIMA, "Próximo", null, null,
				null, null, null, null, null);

		TablaReporte tabla = ReporteTabular.pendientes(new ReportePendientes(List.of(completa, sinDatos), 1, 1),
				new FiltroPendientes(EstadoPendiente.VENCIDOS, 1L, null), NOMBRES, EMISION);

		assertThat(tabla.nombre()).isEqualTo("Pendientes");
		assertThat(tabla.encabezados()).hasSize(13);
		assertThat(tabla.filas()).hasSize(2).allSatisfy(fila -> assertThat(fila).hasSameSizeAs(tabla.encabezados()));
		assertThat(tabla.filas().get(0)).containsExactly("ABC-123", "EQ-001", "San Pedro Sula", "Operativo", "Vencido",
				"Vencido: se pasó por 100 km", LocalDate.of(2026, 5, 1), 100_000, 110_000L, 110_100L, -100L, -5L, LocalDate.of(2026, 11, 16));
		assertThat(tabla.filas().get(1)).containsSequence("DEF-456", "EQ-002", "Tegucigalpa", "Relevo", "Próximo", "Próximo");
		assertThat(tabla.filas().get(1).subList(6, 13)).containsOnlyNulls();
	}

	@Test
	void pendientes_describe_los_filtros_con_nombres_y_trae_los_totales() {
		TablaReporte tabla = ReporteTabular.pendientes(new ReportePendientes(List.of(), 3, 4),
				new FiltroPendientes(EstadoPendiente.PROXIMOS, 2L, 1L), NOMBRES, EMISION);

		assertThat(tabla.notas()).containsExactly("Reporte: Vehículos pendientes de mantenimiento", "Emitido: 2026-11-21",
				"Estado: Solo próximos", "Localidad: Tegucigalpa", "Tipo de uso: Operativo", "Vencidos: 3", "Próximos: 4",
				"Total de vehículos: 0");
	}

	@Test
	void sin_filtros_las_notas_dicen_todas_o_todos() {
		TablaReporte tabla = ReporteTabular.pendientes(new ReportePendientes(List.of(), 0, 0),
				new FiltroPendientes(EstadoPendiente.TODOS, null, null), NOMBRES, EMISION);

		assertThat(tabla.notas()).contains("Estado: Vencidos y próximos", "Localidad: Todas", "Tipo de uso: Todos");
	}

	@Test
	void un_id_de_catalogo_desconocido_se_muestra_como_numeral_en_lugar_de_fallar() {
		TablaReporte tabla = ReporteTabular.pendientes(new ReportePendientes(List.of(), 0, 0),
				new FiltroPendientes(EstadoPendiente.TODOS, 99L, null), NOMBRES, EMISION);

		assertThat(tabla.notas()).contains("Localidad: #99");
	}

	@Test
	void realizados_lleva_periodo_filtros_totales_y_desgloses() {
		MantenimientoRealizado fila = new MantenimientoRealizado(1, 1, "ABC-123", "EQ-001", 1, "San Pedro Sula", LocalDate.of(2026, 11, 10), 100_000,
				1, "10K", "cambio de aceite");
		ReporteRealizados reporte = new ReporteRealizados(new RangoDeFechas(LocalDate.of(2026, 8, 23), LocalDate.of(2026, 11, 21)), List.of(fila),
				List.of(new ConteoPorClave("10K", 3), new ConteoPorClave("20K", 1)), List.of(new ConteoPorClave("San Pedro Sula", 4)));

		TablaReporte tabla = ReporteTabular.realizados(reporte, new FiltroRealizados(null, null, 1L, 1L, "  abc "), NOMBRES, EMISION);

		assertThat(tabla.nombre()).isEqualTo("Realizados");
		assertThat(tabla.encabezados()).containsExactly("Fecha", "Placa", "Equipo", "Localidad", "Rutina", "Kilometraje", "Observaciones");
		assertThat(tabla.filas().get(0)).containsExactly(LocalDate.of(2026, 11, 10), "ABC-123", "EQ-001", "San Pedro Sula", "10K", 100_000,
				"cambio de aceite");
		assertThat(tabla.notas()).containsExactly("Reporte: Mantenimientos realizados", "Emitido: 2026-11-21", "Período: 2026-08-23 a 2026-11-21",
				"Localidad: San Pedro Sula", "Rutina: 10K", "Placa contiene: abc", "Total de mantenimientos: 1",
				"Por rutina: 10K (3), 20K (1)", "Por localidad: San Pedro Sula (4)");
	}

	@Test
	void realizados_sin_placa_ni_resultados_no_menciona_la_placa_y_marca_los_desgloses_vacios() {
		ReporteRealizados reporte = new ReporteRealizados(new RangoDeFechas(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)), List.of(), List.of(),
				List.of());

		TablaReporte tabla = ReporteTabular.realizados(reporte, new FiltroRealizados(null, null, null, null, "   "), NOMBRES, EMISION);

		assertThat(tabla.notas()).noneMatch(nota -> nota.startsWith("Placa contiene"));
		assertThat(tabla.notas()).contains("Por rutina: —", "Por localidad: —", "Total de mantenimientos: 0");
		assertThat(tabla.filas()).isEmpty();
	}

	@Test
	void vencimientos_define_columnas_filas_y_notas() {
		FilaVencimiento fila = new FilaVencimiento(1, "ABC-123", "EQ-001", "San Pedro Sula", LocalDate.of(2026, 5, 1), 100_000, 110_000L, 300L,
				LocalDate.of(2026, 12, 1), 10L, false);

		TablaReporte tabla = ReporteTabular.vencimientos(new ReporteVencimientos(List.of(fila), 2, 1), new FiltroVencimientos(60, 2_000, false, 1L),
				NOMBRES, EMISION);

		assertThat(tabla.nombre()).isEqualTo("Vencimientos");
		assertThat(tabla.encabezados()).hasSize(10);
		assertThat(tabla.filas().get(0)).containsExactly("ABC-123", "EQ-001", "San Pedro Sula", LocalDate.of(2026, 5, 1), 100_000, 110_000L, 300L,
				LocalDate.of(2026, 12, 1), 10L, "Por vencer");
		assertThat(tabla.notas()).containsExactly("Reporte: Próximos vencimientos", "Emitido: 2026-11-21", "Horizonte: 60 días o 2000 km",
				"Ya vencidos: omitidos", "Localidad: San Pedro Sula", "Ya vencidos (cantidad): 2", "Por vencer: 1", "Total de vehículos: 1");
	}

	@Test
	void el_csv_sale_de_la_misma_tabla_con_los_mismos_encabezados_y_sin_las_notas() {
		TablaReporte tabla = new TablaReporte("X", List.of("A", "B"), List.of(java.util.Arrays.asList("uno", 2), java.util.Arrays.asList(null, "=x")),
				List.of("nota que no va al CSV"));

		String csv = new String(ExportadorCsv.csv(tabla, ';'), StandardCharsets.UTF_8);

		assertThat(csv).isEqualTo("﻿A;B\r\nuno;2\r\n;'=x\r\n");
	}
}
