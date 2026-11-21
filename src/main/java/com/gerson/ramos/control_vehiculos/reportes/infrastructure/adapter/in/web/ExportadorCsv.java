package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import java.util.List;

/** Convierte una {@link TablaReporte} a CSV (una fila de encabezados y una por registro). Las notas del resumen no se incluyen. */
final class ExportadorCsv {

	private ExportadorCsv() {
	}

	static byte[] csv(TablaReporte tabla, char separador) {
		CsvWriter csv = new CsvWriter(separador).fila(tabla.encabezados().toArray());
		for (List<Object> fila : tabla.filas()) {
			csv.fila(fila.toArray());
		}
		return csv.bytes();
	}
}
