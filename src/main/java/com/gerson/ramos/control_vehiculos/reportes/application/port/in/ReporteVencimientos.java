package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

import java.util.List;

/** Vehículos con vencimiento dentro del horizonte, ordenados por días restantes (vencidos primero). */
public record ReporteVencimientos(List<FilaVencimiento> filas, int vencidos, int porVencer) {
}
