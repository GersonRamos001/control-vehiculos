package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

import org.jspecify.annotations.Nullable;

/**
 * Filtros del reporte de próximos vencimientos: un vehículo aparece si le faltan {@code horizonteDias} días o menos
 * <b>o</b> {@code horizonteKm} km o menos. Con {@code incluirVencidos} falso se omiten los que ya se pasaron.
 */
public record FiltroVencimientos(int horizonteDias, int horizonteKm, boolean incluirVencidos, @Nullable Long localidadId) {

	public static final int HORIZONTE_DIAS_POR_DEFECTO = 60;
	public static final int HORIZONTE_KM_POR_DEFECTO = 2_000;

	/** Topes razonables: 10 años y un millón de kilómetros. */
	public static final int HORIZONTE_DIAS_MAXIMO = 3_650;
	public static final int HORIZONTE_KM_MAXIMO = 1_000_000;
}
