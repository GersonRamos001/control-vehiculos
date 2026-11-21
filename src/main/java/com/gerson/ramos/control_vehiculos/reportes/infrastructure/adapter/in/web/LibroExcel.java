package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Genera el archivo de Excel (.xlsx) de una {@link TablaReporte}: una hoja con los datos (encabezado en negrita, fila
 * congelada, autofiltro, fechas y números reales con formato) y una hoja "Resumen" con las notas.
 *
 * <p>Los textos se escriben siempre como celdas de texto (nunca como fórmulas), así que un valor como {@code =1+1}
 * no se ejecuta al abrir el archivo.
 */
final class LibroExcel {

	static final String HOJA_RESUMEN = "Resumen";
	static final String FORMATO_FECHA = "yyyy-mm-dd";
	static final String FORMATO_ENTERO = "#,##0";

	private static final int ANCHO_MINIMO = 10;
	private static final int ANCHO_MAXIMO = 60;
	private static final int ANCHO_RESUMEN = 90;
	/** Excel limita el nombre de una hoja a 31 caracteres y prohíbe estos símbolos. */
	private static final int LONGITUD_MAXIMA_NOMBRE_DE_HOJA = 31;
	private static final String SIMBOLOS_PROHIBIDOS_EN_HOJA = "[]:*?/\\";

	private LibroExcel() {
	}

	static byte[] generar(TablaReporte tabla) {
		try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
			Estilos estilos = new Estilos(libro);
			hojaDeDatos(libro, tabla, estilos);
			hojaDeResumen(libro, tabla.notas(), estilos);
			libro.write(salida);
			return salida.toByteArray();
		}
		catch (IOException excepcion) {
			throw new UncheckedIOException("No se pudo generar el archivo de Excel", excepcion);
		}
	}

	private static void hojaDeDatos(XSSFWorkbook libro, TablaReporte tabla, Estilos estilos) {
		Sheet hoja = libro.createSheet(nombreDeHoja(tabla.nombre()));
		List<String> encabezados = tabla.encabezados();
		int[] caracteres = new int[encabezados.size()];

		Row filaDeEncabezado = hoja.createRow(0);
		for (int columna = 0; columna < encabezados.size(); columna++) {
			Cell celda = filaDeEncabezado.createCell(columna);
			celda.setCellValue(encabezados.get(columna));
			celda.setCellStyle(estilos.encabezado);
			caracteres[columna] = encabezados.get(columna).length();
		}

		int numeroDeFila = 1;
		for (List<Object> valores : tabla.filas()) {
			Row fila = hoja.createRow(numeroDeFila++);
			for (int columna = 0; columna < valores.size() && columna < caracteres.length; columna++) {
				caracteres[columna] = Math.max(caracteres[columna], escribir(fila.createCell(columna), valores.get(columna), estilos));
			}
		}

		for (int columna = 0; columna < caracteres.length; columna++) {
			int ancho = Math.min(ANCHO_MAXIMO, Math.max(ANCHO_MINIMO, caracteres[columna] + 2));
			hoja.setColumnWidth(columna, ancho * 256);
		}
		hoja.createFreezePane(0, 1);
		if (!encabezados.isEmpty()) {
			hoja.setAutoFilter(new CellRangeAddress(0, Math.max(0, tabla.filas().size()), 0, encabezados.size() - 1));
		}
	}

	/** Escribe una celda con su tipo real y devuelve cuántos caracteres ocupa (para calcular el ancho de la columna). */
	private static int escribir(Cell celda, Object valor, Estilos estilos) {
		if (valor == null) {
			celda.setBlank();
			return 0;
		}
		if (valor instanceof LocalDate fecha) {
			celda.setCellValue(fecha);
			celda.setCellStyle(estilos.fecha);
			return FORMATO_FECHA.length();
		}
		if (valor instanceof Number numero) {
			celda.setCellValue(numero.doubleValue());
			celda.setCellStyle(estilos.entero);
			return String.format("%,d", numero.longValue()).length();
		}
		if (valor instanceof Boolean booleano) {
			celda.setCellValue(booleano);
			return 5;
		}
		String texto = valor.toString();
		celda.setCellValue(texto);
		return texto.length();
	}

	private static void hojaDeResumen(XSSFWorkbook libro, List<String> notas, Estilos estilos) {
		Sheet hoja = libro.createSheet(HOJA_RESUMEN);
		for (int i = 0; i < notas.size(); i++) {
			Cell celda = hoja.createRow(i).createCell(0);
			celda.setCellValue(notas.get(i));
			if (i == 0) {
				celda.setCellStyle(estilos.titulo);
			}
		}
		hoja.setColumnWidth(0, ANCHO_RESUMEN * 256);
	}

	static String nombreDeHoja(String nombre) {
		StringBuilder limpio = new StringBuilder();
		for (char c : nombre.toCharArray()) {
			limpio.append(SIMBOLOS_PROHIBIDOS_EN_HOJA.indexOf(c) >= 0 ? ' ' : c);
		}
		String resultado = limpio.toString().strip();
		if (resultado.isEmpty()) {
			resultado = "Reporte";
		}
		return resultado.length() > LONGITUD_MAXIMA_NOMBRE_DE_HOJA ? resultado.substring(0, LONGITUD_MAXIMA_NOMBRE_DE_HOJA) : resultado;
	}

	/** Los estilos se crean una sola vez por libro (Excel limita su cantidad). */
	private static final class Estilos {

		final CellStyle encabezado;
		final CellStyle fecha;
		final CellStyle entero;
		final CellStyle titulo;

		Estilos(XSSFWorkbook libro) {
			DataFormat formatos = libro.createDataFormat();

			Font negrita = libro.createFont();
			negrita.setBold(true);
			encabezado = libro.createCellStyle();
			encabezado.setFont(negrita);
			encabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			encabezado.setBorderBottom(BorderStyle.THIN);
			encabezado.setVerticalAlignment(VerticalAlignment.CENTER);

			fecha = libro.createCellStyle();
			fecha.setDataFormat(formatos.getFormat(FORMATO_FECHA));

			entero = libro.createCellStyle();
			entero.setDataFormat(formatos.getFormat(FORMATO_ENTERO));

			Font grande = libro.createFont();
			grande.setBold(true);
			grande.setFontHeightInPoints((short) 14);
			titulo = libro.createCellStyle();
			titulo.setFont(grande);
		}
	}
}
