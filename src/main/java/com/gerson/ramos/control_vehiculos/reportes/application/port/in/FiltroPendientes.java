package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

import org.jspecify.annotations.Nullable;

/** Filtros del reporte de vehículos pendientes; {@code localidadId} y {@code tipoUsoId} nulos significan "todos". */
public record FiltroPendientes(EstadoPendiente estado, @Nullable Long localidadId, @Nullable Long tipoUsoId) {
}
