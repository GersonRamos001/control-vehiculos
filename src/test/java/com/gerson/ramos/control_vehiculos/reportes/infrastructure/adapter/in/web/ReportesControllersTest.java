package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ConteoPorClave;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.EstadoPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaVencimiento;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroPendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientesUseCase;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizadosUseCase;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientosUseCase;
import com.gerson.ramos.control_vehiculos.reportes.domain.model.RangoDeFechas;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;

/** Pantallas y descargas CSV de reportes con las plantillas reales y los casos de uso simulados. Reloj fijo en 2026-11-21. */
@WebMvcTest({ ReportesController.class, ReportesDescargasController.class })
@WithMockUser(roles = "ADMINISTRADOR")
classReportesControllersTest {

	@TestConfiguration
	static class RelojFijo {

		@Bean
		Clock reloj() {
			return Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneOffset.UTC);
		}
	}

	private static final String BOM = "﻿";

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ReportePendientesUseCase pendientes;

	@MockitoBean
	ReporteRealizadosUseCase realizados;

	@MockitoBean
	ReporteVencimientosUseCase vencimientos;

	@MockitoBean
	ConsultarCatalogosUseCase catalogos;

	// ---- datos de ejemplo ----

	private static ReportePendientes reportePendientes() {
		FilaPendiente vencido = new FilaPendiente(1, "ABC-123", "EQ-001", "San Pedro Sula", "Operativo", Severidad.VENCIDA,
				"Vencido: se pasó por 100 km", LocalDate.of(2026, 5, 1), 100_000, 110_000L, 110_100L, -100L, -5L, LocalDate.of(2026, 11, 16));
		FilaPendiente proximo = new FilaPendiente(2, "DEF-456", "EQ-002", "Tegucigalpa", "Relevo", Severidad.PROXIMA,
				"Próximo: faltan 800 km", LocalDate.of(2026, 6, 1), 50_000, 60_000L, 59_200L, 800L, 20L, LocalDate.of(2026, 12, 11));
		return new ReportePendientes(List.of(vencido, proximo), 1, 1);
	}

	private static ReporteRealizados reporteRealizados(String observaciones) {
		MantenimientoRealizado fila = new MantenimientoRealizado(1, 1, "ABC-123", "EQ-001", 1, "San Pedro Sula", LocalDate.of(2026, 11, 10),
				100_000, 1, "10K", observaciones);
		return new ReporteRealizados(new RangoDeFechas(LocalDate.of(2026, 8, 23), LocalDate.of(2026, 11, 21)), List.of(fila),
				List.of(new ConteoPorClave("10K", 1)), List.of(new ConteoPorClave("San Pedro Sula", 1)));
	}

	private static ReporteVencimientos reporteVencimientos() {
		FilaVencimiento fila = new FilaVencimiento(1, "ABC-123", "EQ-001", "San Pedro Sula", LocalDate.of(2026, 5, 1), 100_000, 110_000L,
				300L, LocalDate.of(2026, 12, 1), 10L, false);
		return new ReporteVencimientos(List.of(fila), 0, 1);
	}

	private static String texto(MockHttpServletResponse respuesta) throws Exception {
		return respuesta.getContentAsString(StandardCharsets.UTF_8);
	}

	// ---- pantallas ----

	@Test
	void el_indice_enlaza_los_tres_reportes() throws Exception {
		String html = mvc.perform(get("/reportes"))
			.andExpect(status().isOk())
			.andExpect(view().name("reportes/indice"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("/reportes/pendientes", "/reportes/realizados", "/reportes/vencimientos");
	}

	@Test
	void pendientes_muestra_las_filas_los_totales_y_el_enlace_csv_con_los_filtros() throws Exception {
		when(pendientes.generar(any())).thenReturn(reportePendientes());

		String html = mvc.perform(get("/reportes/pendientes").param("estado", "VENCIDOS").param("localidadId", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("reportes/pendientes"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("ABC-123", "DEF-456", "Vencido: se pasó por 100 km", "Próximo: faltan 800 km", "110,000 km",
				"2026-11-16", "hace 5 días", "en 20 días", "/vehiculos/1/mantenimientos/nuevo",
				"/reportes/pendientes/csv?estado=VENCIDOS&amp;localidadId=1");
		ArgumentCaptor<FiltroPendientes> captor = ArgumentCaptor.forClass(FiltroPendientes.class);
		verify(pendientes).generar(captor.capture());
		assertThat(captor.getValue()).isEqualTo(new FiltroPendientes(EstadoPendiente.VENCIDOS, 1L, null));
	}

	@Test
	void pendientes_sin_parametros_incluye_todos_los_estados() throws Exception {
		when(pendientes.generar(any())).thenReturn(new ReportePendientes(List.of(), 0, 0));

		String html = mvc.perform(get("/reportes/pendientes")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Ningún vehículo cumple estos filtros.", "/reportes/pendientes/csv?estado=TODOS");
		verify(pendientes).generar(new FiltroPendientes(EstadoPendiente.TODOS, null, null));
	}

	@Test
	void pendientes_con_un_estado_invalido_muestra_el_error_sin_generar_el_reporte() throws Exception {
		mvc.perform(get("/reportes/pendientes").param("estado", "INEXISTENTE"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("filtro", "estado"));

		verify(pendientes, never()).generar(any());
	}

	@Test
	void realizados_muestra_el_periodo_efectivo_el_desglose_y_el_enlace_csv() throws Exception {
		when(realizados.generar(any())).thenReturn(reporteRealizados("cambio de aceite"));

		String html = mvc.perform(get("/reportes/realizados"))
			.andExpect(status().isOk())
			.andExpect(view().name("reportes/realizados"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("value=\"2026-08-23\"", "value=\"2026-11-21\"", "mantenimientos entre 2026-08-23 y 2026-11-21", "Por rutina",
				"Por localidad", "ABC-123", "cambio de aceite", "100,000 km",
				"/reportes/realizados/csv?desde=2026-08-23&amp;hasta=2026-11-21");
		verify(realizados).generar(new FiltroRealizados(null, null, null, null, null));
	}

	@Test
	void realizados_pasa_los_filtros_y_codifica_la_placa_en_el_enlace_csv() throws Exception {
		when(realizados.generar(any())).thenReturn(reporteRealizados(null));

		String html = mvc.perform(get("/reportes/realizados").param("desde", "2026-08-01").param("hasta", "2026-09-30")
				.param("localidadId", "1").param("rutinaId", "2").param("placa", "AB 1"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		verify(realizados).generar(new FiltroRealizados(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 30), 1L, 2L, "AB 1"));
		assertThat(html).contains("localidadId=1", "rutinaId=2", "placa=AB%201");
	}

	@Test
	void realizados_con_rango_invalido_muestra_el_error_junto_al_campo_y_no_ofrece_csv() throws Exception {
		when(realizados.generar(any())).thenThrow(new ConflictoDeDatosException("desde", "La fecha inicial no puede ser posterior a la final"));

		String html = mvc.perform(get("/reportes/realizados").param("desde", "2026-11-01").param("hasta", "2026-10-01"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("filtro", "desde"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("La fecha inicial no puede ser posterior a la final");
		assertThat(html).doesNotContain("Descargar CSV");
	}

	@Test
	void realizados_con_fecha_ilegible_no_llama_al_caso_de_uso() throws Exception {
		mvc.perform(get("/reportes/realizados").param("desde", "ayer"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("filtro", "desde"));

		verify(realizados, never()).generar(any());
	}

	@Test
	void vencimientos_usa_los_horizontes_por_defecto_y_arma_el_enlace_csv() throws Exception {
		when(vencimientos.generar(any())).thenReturn(reporteVencimientos());

		String html = mvc.perform(get("/reportes/vencimientos"))
			.andExpect(status().isOk())
			.andExpect(view().name("reportes/vencimientos"))
			.andReturn().getResponse().getContentAsString();

		verify(vencimientos).generar(new FiltroVencimientos(60, 2_000, true, null));
		assertThat(html).contains("value=\"60\"", "value=\"2000\"", "ABC-123", "2026-12-01", "10 días", "300 km",
				"/reportes/vencimientos/csv?horizonteDias=60&amp;horizonteKm=2000&amp;vencidos=INCLUIR");
	}

	@Test
	void vencimientos_puede_omitir_los_vencidos_y_filtrar_por_localidad() throws Exception {
		when(vencimientos.generar(any())).thenReturn(new ReporteVencimientos(List.of(), 0, 0));

		mvc.perform(get("/reportes/vencimientos").param("horizonteDias", "30").param("horizonteKm", "500").param("vencidos", "OMITIR")
				.param("localidadId", "2"))
			.andExpect(status().isOk());

		verify(vencimientos).generar(new FiltroVencimientos(30, 500, false, 2L));
	}

	@Test
	void vencimientos_con_horizonte_no_positivo_muestra_el_error_sin_generar_el_reporte() throws Exception {
		mvc.perform(get("/reportes/vencimientos").param("horizonteDias", "0"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("filtro", "horizonteDias"));

		verify(vencimientos, never()).generar(any());
	}

	@Test
	void vencimientos_con_un_horizonte_por_encima_del_tope_muestra_el_error_sin_generar_el_reporte() throws Exception {
		mvc.perform(get("/reportes/vencimientos").param("horizonteDias", "3651").param("horizonteKm", "1000001"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("filtro", "horizonteDias", "horizonteKm"));

		verify(vencimientos, never()).generar(any());
	}

	// ---- descargas CSV ----

	@Test
	void csv_de_pendientes_lleva_bom_encabezados_separador_y_nombre_con_la_fecha() throws Exception {
		when(pendientes.generar(any())).thenReturn(reportePendientes());

		MockHttpServletResponse respuesta = mvc.perform(get("/reportes/pendientes/csv").param("estado", "TODOS"))
			.andExpect(status().isOk())
			.andReturn().getResponse();

		assertThat(respuesta.getContentType()).startsWith("text/csv");
		assertThat(respuesta.getHeader("Content-Disposition")).contains("attachment").contains("reporte-pendientes-2026-11-21.csv");
		assertThat(respuesta.getHeader("Cache-Control")).contains("no-store");
		byte[] bytes = respuesta.getContentAsByteArray();
		assertThat(bytes[0]).isEqualTo((byte) 0xEF);
		assertThat(bytes[1]).isEqualTo((byte) 0xBB);
		assertThat(bytes[2]).isEqualTo((byte) 0xBF);
		assertThat(texto(respuesta)).isEqualTo(BOM
				+ "Placa;Equipo;Localidad;Tipo de uso;Estado;Motivo;Último mantenimiento;Km del último mantenimiento;Próximo km;Km actual;"
				+ "Km restantes;Días restantes;Fecha de referencia\r\n"
				+ "ABC-123;EQ-001;San Pedro Sula;Operativo;Vencido;Vencido: se pasó por 100 km;2026-05-01;100000;110000;110100;-100;-5;2026-11-16\r\n"
				+ "DEF-456;EQ-002;Tegucigalpa;Relevo;Próximo;Próximo: faltan 800 km;2026-06-01;50000;60000;59200;800;20;2026-12-11\r\n");
	}

	@Test
	void csv_de_pendientes_respeta_los_filtros() throws Exception {
		when(pendientes.generar(any())).thenReturn(new ReportePendientes(List.of(), 0, 0));

		mvc.perform(get("/reportes/pendientes/csv").param("estado", "PROXIMOS").param("tipoUsoId", "2")).andExpect(status().isOk());

		verify(pendientes).generar(new FiltroPendientes(EstadoPendiente.PROXIMOS, null, 2L));
	}

	@Test
	void csv_de_realizados_neutraliza_las_formulas_en_las_observaciones() throws Exception {
		when(realizados.generar(any())).thenReturn(reporteRealizados("=HYPERLINK(\"http://malo\")"));

		MockHttpServletResponse respuesta = mvc.perform(get("/reportes/realizados/csv")).andExpect(status().isOk()).andReturn().getResponse();

		assertThat(respuesta.getHeader("Content-Disposition")).contains("reporte-realizados-2026-11-21.csv");
		assertThat(texto(respuesta)).isEqualTo(BOM + "Fecha;Placa;Equipo;Localidad;Rutina;Kilometraje;Observaciones\r\n"
				+ "2026-11-10;ABC-123;EQ-001;San Pedro Sula;10K;100000;\"'=HYPERLINK(\"\"http://malo\"\")\"\r\n");
	}

	@Test
	void csv_de_realizados_deja_vacia_la_celda_sin_observaciones() throws Exception {
		when(realizados.generar(any())).thenReturn(reporteRealizados(null));

		MockHttpServletResponse respuesta = mvc.perform(get("/reportes/realizados/csv")).andReturn().getResponse();

		assertThat(texto(respuesta)).endsWith("2026-11-10;ABC-123;EQ-001;San Pedro Sula;10K;100000;\r\n");
	}

	@Test
	void csv_de_vencimientos_lleva_las_columnas_esperadas() throws Exception {
		when(vencimientos.generar(any())).thenReturn(reporteVencimientos());

		MockHttpServletResponse respuesta = mvc.perform(get("/reportes/vencimientos/csv")).andExpect(status().isOk()).andReturn().getResponse();

		assertThat(respuesta.getHeader("Content-Disposition")).contains("reporte-vencimientos-2026-11-21.csv");
		assertThat(texto(respuesta)).isEqualTo(BOM
				+ "Placa;Equipo;Localidad;Último mantenimiento;Km del último mantenimiento;Próximo km;Km restantes;Fecha de referencia;"
				+ "Días restantes;Estado\r\n"
				+ "ABC-123;EQ-001;San Pedro Sula;2026-05-01;100000;110000;300;2026-12-01;10;Por vencer\r\n");
	}

	@Test
	void una_descarga_con_filtros_invalidos_responde_400_en_lugar_de_un_csv() throws Exception {
		mvc.perform(get("/reportes/vencimientos/csv").param("horizonteDias", "abc")).andExpect(status().isBadRequest());
		mvc.perform(get("/reportes/realizados/csv").param("desde", "ayer")).andExpect(status().isBadRequest());

		verify(vencimientos, never()).generar(any());
		verify(realizados, never()).generar(any());
	}

	// ---- Excel (.xlsx) y tablas interactivas ----

	private static XSSFWorkbook libro(MockHttpServletResponse respuesta) throws Exception {
		return new XSSFWorkbook(new ByteArrayInputStream(respuesta.getContentAsByteArray()));
	}

	@Test
	void las_pantallas_ofrecen_excel_y_csv_y_una_tabla_ordenable_con_buscador() throws Exception {
		when(pendientes.generar(any())).thenReturn(reportePendientes());

		String html = mvc.perform(get("/reportes/pendientes").param("estado", "VENCIDOS").param("localidadId", "1"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Descargar Excel", "/reportes/pendientes/xlsx?estado=VENCIDOS&amp;localidadId=1", "Descargar CSV",
				"id=\"tabla-pendientes\"", "tabla-interactiva", "data-tipo=\"numero\"", "data-valor=\"110000\"",
				"data-valor=\"ABC-123\"", "data-contador-total", "id=\"buscar-pendientes\"", "/js/tablas-interactivas.js");
	}

	@Test
	void sin_reporte_no_se_ofrece_ninguna_descarga() throws Exception {
		when(realizados.generar(any())).thenThrow(new ConflictoDeDatosException("desde", "La fecha inicial no puede ser posterior a la final"));

		String html = mvc.perform(get("/reportes/realizados").param("desde", "2026-11-01").param("hasta", "2026-10-01"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).doesNotContain("Descargar Excel", "Descargar CSV");
	}

	@Test
	void los_dias_se_escriben_en_singular_cuando_es_uno_y_hoy_cuando_es_cero() throws Exception {
		FilaPendiente manana = new FilaPendiente(1, "AAA-111", "EQ-1", "SPS", "Operativo", Severidad.PROXIMA, "p", null, null, null, null, 500L, 1L,
				LocalDate.of(2026, 11, 22));
		FilaPendiente ayer = new FilaPendiente(2, "BBB-222", "EQ-2", "SPS", "Operativo", Severidad.VENCIDA, "v", null, null, null, null, -5L, -1L,
				LocalDate.of(2026, 11, 20));
		FilaPendiente hoy = new FilaPendiente(3, "CCC-333", "EQ-3", "SPS", "Operativo", Severidad.PROXIMA, "p", null, null, null, null, 5L, 0L,
				LocalDate.of(2026, 11, 21));
		when(pendientes.generar(any())).thenReturn(new ReportePendientes(List.of(manana, ayer, hoy), 1, 2));
		FilaVencimiento venceManana = new FilaVencimiento(1, "AAA-111", "EQ-1", "SPS", LocalDate.of(2026, 5, 1), 1, 2L, 1L, LocalDate.of(2026, 11, 22), 1L,
				false);
		FilaVencimiento vencioAyer = new FilaVencimiento(2, "BBB-222", "EQ-2", "SPS", LocalDate.of(2026, 5, 1), 1, 2L, -1L, LocalDate.of(2026, 11, 20),
				-1L, true);
		FilaVencimiento venceHoy = new FilaVencimiento(3, "CCC-333", "EQ-3", "SPS", LocalDate.of(2026, 5, 1), 1, 2L, 1L, LocalDate.of(2026, 11, 21), 0L,
				false);
		when(vencimientos.generar(any())).thenReturn(new ReporteVencimientos(List.of(vencioAyer, venceHoy, venceManana), 1, 2));

		String htmlPendientes = mvc.perform(get("/reportes/pendientes")).andReturn().getResponse().getContentAsString();
		String htmlVencimientos = mvc.perform(get("/reportes/vencimientos")).andReturn().getResponse().getContentAsString();

		assertThat(htmlPendientes).contains("en 1 día<", "hace 1 día<", ">hoy<").doesNotContain("1 días");
		assertThat(htmlVencimientos).contains(">1 día<", "Vencido hace 1 día<", ">Hoy<").doesNotContain("1 días");
	}

	@Test
	void excel_de_pendientes_trae_datos_tipados_filtros_con_nombres_y_nombre_de_archivo() throws Exception {
		when(catalogos.listarLocalidades(false))
			.thenReturn(List.of(Localidad.reconstruir(new LocalidadId(1), new CodigoCatalogo("SPS"), "San Pedro Sula", true)));
		when(pendientes.generar(any())).thenReturn(reportePendientes());

		MockHttpServletResponse respuesta = mvc.perform(get("/reportes/pendientes/xlsx").param("estado", "VENCIDOS").param("localidadId", "1"))
			.andExpect(status().isOk())
			.andReturn().getResponse();

		assertThat(respuesta.getContentType()).startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(respuesta.getHeader("Content-Disposition")).contains("attachment").contains("reporte-pendientes-2026-11-21.xlsx");
		assertThat(respuesta.getHeader("Cache-Control")).contains("no-store");
		verify(pendientes).generar(new FiltroPendientes(EstadoPendiente.VENCIDOS, 1L, null));
		try (XSSFWorkbook libro = libro(respuesta)) {
			Sheet datos = libro.getSheet("Pendientes");
			assertThat(datos.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Placa");
			assertThat(datos.getRow(1).getCell(0).getStringCellValue()).isEqualTo("ABC-123");
			assertThat(datos.getRow(1).getCell(6).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 1));
			assertThat(datos.getRow(1).getCell(10).getNumericCellValue()).isEqualTo(-100d);
			Sheet resumen = libro.getSheet("Resumen");
			assertThat(resumen.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Estado: Solo vencidos");
			assertThat(resumen.getRow(3).getCell(0).getStringCellValue()).isEqualTo("Localidad: San Pedro Sula");
		}
	}

	@Test
	void excel_de_realizados_guarda_las_formulas_como_texto() throws Exception {
		when(realizados.generar(any())).thenReturn(reporteRealizados("=HYPERLINK(\"http://malo\")"));

		MockHttpServletResponse respuesta = mvc.perform(get("/reportes/realizados/xlsx")).andExpect(status().isOk()).andReturn().getResponse();

		assertThat(respuesta.getHeader("Content-Disposition")).contains("reporte-realizados-2026-11-21.xlsx");
		try (XSSFWorkbook libro = libro(respuesta)) {
			Sheet datos = libro.getSheet("Realizados");
			assertThat(datos.getRow(1).getCell(6).getCellType()).isEqualTo(CellType.STRING);
			assertThat(datos.getRow(1).getCell(6).getStringCellValue()).isEqualTo("=HYPERLINK(\"http://malo\")");
			assertThat(datos.getRow(1).getCell(5).getNumericCellValue()).isEqualTo(100_000d);
			assertThat(libro.getSheet("Resumen").getRow(2).getCell(0).getStringCellValue()).isEqualTo("Período: 2026-08-23 a 2026-11-21");
		}
	}

	@Test
	void excel_de_vencimientos_describe_el_horizonte_en_el_resumen() throws Exception {
		when(vencimientos.generar(any())).thenReturn(reporteVencimientos());

		MockHttpServletResponse respuesta = mvc.perform(get("/reportes/vencimientos/xlsx")).andExpect(status().isOk()).andReturn().getResponse();

		assertThat(respuesta.getHeader("Content-Disposition")).contains("reporte-vencimientos-2026-11-21.xlsx");
		try (XSSFWorkbook libro = libro(respuesta)) {
			assertThat(libro.getSheet("Vencimientos").getRow(1).getCell(0).getStringCellValue()).isEqualTo("ABC-123");
			assertThat(libro.getSheet("Resumen").getRow(2).getCell(0).getStringCellValue()).isEqualTo("Horizonte: 60 días o 2000 km");
		}
	}

	@Test
	void el_excel_con_filtros_invalidos_responde_400() throws Exception {
		mvc.perform(get("/reportes/vencimientos/xlsx").param("horizonteDias", "abc")).andExpect(status().isBadRequest());
		mvc.perform(get("/reportes/pendientes/xlsx").param("estado", "INEXISTENTE")).andExpect(status().isBadRequest());

		verify(vencimientos, never()).generar(any());
		verify(pendientes, never()).generar(any());
	}

	@Test
	void una_descarga_con_rango_invalido_responde_400() throws Exception {
		when(realizados.generar(any())).thenThrow(new ConflictoDeDatosException("desde", "La fecha inicial no puede ser posterior a la final"));

		mvc.perform(get("/reportes/realizados/csv").param("desde", "2026-11-01").param("hasta", "2026-10-01"))
			.andExpect(status().isBadRequest());
	}
}
