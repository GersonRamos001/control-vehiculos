package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/**
 * Filtros del reporte de mantenimientos realizados. Sin {@code hasta} se usa hoy; sin {@code desde}, 90 días antes de
 * {@code hasta}. {@code placa} filtra por coincidencia parcial sin distinguir mayúsculas.
 */
public record FiltroRealizados(@Nullable LocalDate desde, @Nullable LocalDate hasta, @Nullable Long localidadId,
		@Nullable Long rutinaId, @Nullable String placa) {
}
