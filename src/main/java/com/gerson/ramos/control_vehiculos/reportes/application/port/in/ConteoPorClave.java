package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

/** Cuántas veces aparece una clave (rutina, localidad…) en un reporte. */
public record ConteoPorClave(String clave, int cantidad) {
}
