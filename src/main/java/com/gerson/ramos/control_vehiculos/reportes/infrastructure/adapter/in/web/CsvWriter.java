package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import java.nio.charset.StandardCharsets;

/**
 * Escritor de CSV pensado para abrirse en Excel: UTF-8 con BOM (acentos correctos), saltos de línea CRLF, comillas
 * dobles solo cuando hacen falta y protección contra inyección de fórmulas.
 *
 * <p>Los textos que empiezan con {@code =}, {@code +}, {@code -}, {@code @}, tabulador o retorno de carro se prefijan
 * con una comilla simple para que Excel no los interprete como fórmulas (las observaciones las escribe el usuario).
 * Los números, booleanos y fechas se escriben tal cual (sin separadores de miles; fechas ISO {@code yyyy-MM-dd}), por
 * lo que un número negativo sigue siendo un número.
 */
final class CsvWriter {

	private static final char BOM = '﻿';
	private static final String FIN_DE_LINEA = "\r\n";

	private final char separador;
	private final StringBuilder salida = new StringBuilder().append(BOM);

	CsvWriter(char separador) {
		if (separador == '"' || separador == '\n' || separador == '\r') {
			throw new IllegalArgumentException("Separador de CSV no válido: " + separador);
		}
		this.separador = separador;
	}

	/** Agrega una fila. Cada celda puede ser nula (vacía), un número, un booleano, una fecha o un texto. */
	CsvWriter fila(Object... celdas) {
		for (int i = 0; i < celdas.length; i++) {
			if (i > 0) {
				salida.append(separador);
			}
			salida.append(celda(celdas[i]));
		}
		salida.append(FIN_DE_LINEA);
		return this;
	}

	byte[] bytes() {
		return salida.toString().getBytes(StandardCharsets.UTF_8);
	}

	private String celda(Object valor) {
		if (valor == null) {
			return "";
		}
		if (valor instanceof Number || valor instanceof Boolean || valor instanceof java.time.temporal.TemporalAccessor) {
			return valor.toString();
		}
		return texto(valor.toString());
	}

	private String texto(String original) {
		String texto = original;
		if (!texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0) {
			texto = "'" + texto;
		}
		boolean necesitaComillas = texto.indexOf(separador) >= 0 || texto.indexOf('"') >= 0 || texto.indexOf('\n') >= 0
				|| texto.indexOf('\r') >= 0;
		return necesitaComillas ? "\"" + texto.replace("\"", "\"\"") + "\"" : texto;
	}
}
