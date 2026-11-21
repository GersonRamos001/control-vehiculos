package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import java.util.List;

/**
 * Un reporte como tabla: la única representación de la que salen el CSV y el Excel, de modo que ambos tengan siempre
 * las mismas columnas.
 *
 * @param nombre nombre corto del reporte (también el de la hoja de datos de Excel)
 * @param encabezados títulos de las columnas
 * @param filas una lista de celdas por fila; cada celda es nula, un número, una fecha o un texto
 * @param notas líneas de la hoja "Resumen" de Excel (título, emisión, filtros aplicados, totales); el CSV las ignora
 */
record TablaReporte(String nombre, List<String> encabezados, List<List<Object>> filas, List<String> notas) {
}
