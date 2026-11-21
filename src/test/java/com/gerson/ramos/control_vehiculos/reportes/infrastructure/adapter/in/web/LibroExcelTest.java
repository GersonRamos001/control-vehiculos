package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/** Se genera el .xlsx y se vuelve a leer con POI para comprobar lo que Excel verá. */
class LibroExcelTest {

	private static TablaReporte tabla(List<List<Object>> filas, String... notas) {
		return new TablaReporte("Pendientes", List.of("Placa", "Fecha", "Kilometraje"), filas, List.of(notas));
	}

	private static List<Object> fila(Object... celdas) {
		return Arrays.asList(celdas);
	}

	private static XSSFWorkbook leer(TablaReporte tabla) throws IOException {
		return new XSSFWorkbook(new ByteArrayInputStream(LibroExcel.generar(tabla)));
	}

	@Test
	void tiene_una_hoja_de_datos_con_el_nombre_del_reporte_y_una_hoja_resumen() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("ABC-123", LocalDate.of(2026, 11, 21), 107_500)), "Reporte: X"))) {
			assertThat(libro.getNumberOfSheets()).isEqualTo(2);
			assertThat(libro.getSheetName(0)).isEqualTo("Pendientes");
			assertThat(libro.getSheetName(1)).isEqualTo(LibroExcel.HOJA_RESUMEN);
		}
	}

	@Test
	void el_encabezado_va_en_negrita_con_relleno() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("ABC-123", LocalDate.of(2026, 11, 21), 107_500))))) {
			Sheet hoja = libro.getSheetAt(0);

			assertThat(hoja.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Placa");
			assertThat(hoja.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Fecha");
			assertThat(hoja.getRow(0).getCell(2).getStringCellValue()).isEqualTo("Kilometraje");
			XSSFCellStyle estilo = (XSSFCellStyle) hoja.getRow(0).getCell(0).getCellStyle();
			assertThat(estilo.getFont().getBold()).isTrue();
			assertThat(estilo.getFillPattern().name()).isEqualTo("SOLID_FOREGROUND");
		}
	}

	@Test
	void congela_la_fila_de_encabezado_y_activa_el_autofiltro_sobre_todos_los_datos() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("A", null, 1), fila("B", null, 2))))) {
			XSSFSheet hoja = libro.getSheetAt(0);

			assertThat(hoja.getPaneInformation()).isNotNull();
			assertThat(hoja.getPaneInformation().isFreezePane()).isTrue();
			assertThat(hoja.getPaneInformation().getHorizontalSplitPosition()).isEqualTo((short) 1);
			assertThat(hoja.getCTWorksheet().getAutoFilter().getRef()).isEqualTo("A1:C3");
		}
	}

	@Test
	void las_fechas_son_fechas_reales_con_formato_iso() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("ABC-123", LocalDate.of(2026, 11, 21), 1))))) {
			Cell celda = libro.getSheetAt(0).getRow(1).getCell(1);

			assertThat(celda.getCellType()).isEqualTo(CellType.NUMERIC);
			assertThat(DateUtil.isCellDateFormatted(celda)).isTrue();
			assertThat(celda.getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2026, 11, 21));
			assertThat(celda.getCellStyle().getDataFormatString()).isEqualTo(LibroExcel.FORMATO_FECHA);
		}
	}

	@Test
	void los_kilometros_y_dias_son_numeros_reales_con_separador_de_miles() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("ABC-123", null, 107_500), fila("DEF-456", null, -1_200L))))) {
			Cell positivo = libro.getSheetAt(0).getRow(1).getCell(2);
			Cell negativo = libro.getSheetAt(0).getRow(2).getCell(2);

			assertThat(positivo.getCellType()).isEqualTo(CellType.NUMERIC);
			assertThat(positivo.getNumericCellValue()).isEqualTo(107_500d);
			assertThat(positivo.getCellStyle().getDataFormatString()).isEqualTo(LibroExcel.FORMATO_ENTERO);
			assertThat(negativo.getNumericCellValue()).isEqualTo(-1_200d);
		}
	}

	@Test
	void los_valores_nulos_quedan_como_celdas_vacias() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("ABC-123", null, null))))) {
			assertThat(libro.getSheetAt(0).getRow(1).getCell(1).getCellType()).isEqualTo(CellType.BLANK);
			assertThat(libro.getSheetAt(0).getRow(1).getCell(2).getCellType()).isEqualTo(CellType.BLANK);
		}
	}

	@Test
	void un_texto_que_parece_formula_se_guarda_como_texto_y_no_se_ejecuta() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("=HYPERLINK(\"http://malo\")", null, null), fila("+1+1", null, null),
				fila("@SUMA(A1)", null, null), fila("-2+3", null, null))))) {
			for (int i = 1; i <= 4; i++) {
				Cell celda = libro.getSheetAt(0).getRow(i).getCell(0);
				assertThat(celda.getCellType()).isEqualTo(CellType.STRING);
			}
			assertThat(libro.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()).isEqualTo("=HYPERLINK(\"http://malo\")");
			assertThat(libro.getSheetAt(0).getRow(2).getCell(0).getStringCellValue()).isEqualTo("+1+1");
		}
	}

	@Test
	void conserva_acentos_y_caracteres_especiales() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("Próximo: niño, ñandú; “comillas” & <etiquetas>", null, null))))) {
			assertThat(libro.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()).isEqualTo("Próximo: niño, ñandú; “comillas” & <etiquetas>");
		}
	}

	@Test
	void el_ancho_de_las_columnas_se_ajusta_al_contenido_dentro_de_un_minimo_y_un_maximo() throws IOException {
		String larguisimo = "x".repeat(300);
		try (XSSFWorkbook libro = leer(tabla(List.of(fila("AB", null, 5), fila(larguisimo, null, 5))))) {
			Sheet hoja = libro.getSheetAt(0);

			assertThat(hoja.getColumnWidth(0)).isEqualTo(60 * 256); // tope
			assertThat(hoja.getColumnWidth(2)).isEqualTo(Math.max(10, "Kilometraje".length() + 2) * 256); // el encabezado manda
			assertThat(hoja.getColumnWidth(1)).isGreaterThanOrEqualTo(10 * 256);
		}
	}

	@Test
	void la_hoja_resumen_lleva_las_notas_y_el_titulo_en_negrita() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(), "Reporte: Vehículos pendientes", "Emitido: 2026-11-21", "Estado: Solo vencidos"))) {
			Sheet resumen = libro.getSheet(LibroExcel.HOJA_RESUMEN);

			assertThat(resumen.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Reporte: Vehículos pendientes");
			assertThat(resumen.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Emitido: 2026-11-21");
			assertThat(resumen.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Estado: Solo vencidos");
			assertThat(((XSSFCellStyle) resumen.getRow(0).getCell(0).getCellStyle()).getFont().getBold()).isTrue();
		}
	}

	@Test
	void una_tabla_sin_filas_genera_solo_el_encabezado_con_autofiltro() throws IOException {
		try (XSSFWorkbook libro = leer(tabla(List.of(), "Reporte: vacío"))) {
			XSSFSheet hoja = libro.getSheetAt(0);

			assertThat(hoja.getLastRowNum()).isZero();
			assertThat(hoja.getCTWorksheet().getAutoFilter().getRef()).isEqualTo("A1:C1");
		}
	}

	@Test
	void soporta_miles_de_filas() throws IOException {
		List<List<Object>> filas = new ArrayList<>();
		for (int i = 0; i < 5_000; i++) {
			filas.add(fila("P-" + i, LocalDate.of(2026, 1, 1).plusDays(i % 300), i));
		}

		try (XSSFWorkbook libro = leer(tabla(filas))) {
			assertThat(libro.getSheetAt(0).getLastRowNum()).isEqualTo(5_000);
			assertThat(libro.getSheetAt(0).getRow(5_000).getCell(0).getStringCellValue()).isEqualTo("P-4999");
		}
	}

	@Test
	void el_nombre_de_la_hoja_respeta_los_limites_de_excel() {
		assertThat(LibroExcel.nombreDeHoja("Pendientes")).isEqualTo("Pendientes");
		assertThat(LibroExcel.nombreDeHoja("Datos: [2026]/x*?\\y")).doesNotContain(":", "[", "]", "/", "*", "?", "\\");
		assertThat(LibroExcel.nombreDeHoja("a".repeat(50))).hasSize(31);
		assertThat(LibroExcel.nombreDeHoja("   ")).isEqualTo("Reporte");
		assertThat(LibroExcel.nombreDeHoja("[]")).isEqualTo("Reporte");
	}
}
