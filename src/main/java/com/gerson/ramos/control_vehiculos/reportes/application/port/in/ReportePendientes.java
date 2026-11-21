package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

import java.util.List;

/** Vehículos pendientes (vencidos primero) y sus totales ya filtrados. */
public record ReportePendientes(List<FilaPendiente> filas, int vencidos, int proximos) {
}
