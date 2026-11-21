package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroVencimientos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Parámetros (GET) del reporte de próximos vencimientos. "Vencidos" es una lista (incluir/omitir) y no una casilla
 * porque una casilla sin marcar no se envía y no se distinguiría de "primera visita" (que debe incluirlos).
 */
@Getter
@Setter
public class FiltroVencimientosForm {

	public enum Vencidos {
		INCLUIR, OMITIR
	}

	@NotNull(message = "El horizonte en días es obligatorio")
	@Positive(message = "El horizonte en días debe ser mayor que cero")
	@Max(value = FiltroVencimientos.HORIZONTE_DIAS_MAXIMO, message = "El horizonte en días no puede superar 3,650 (10 años)")
	private Integer horizonteDias = FiltroVencimientos.HORIZONTE_DIAS_POR_DEFECTO;

	@NotNull(message = "El horizonte en kilómetros es obligatorio")
	@Positive(message = "El horizonte en kilómetros debe ser mayor que cero")
	@Max(value = FiltroVencimientos.HORIZONTE_KM_MAXIMO, message = "El horizonte en kilómetros no puede superar 1,000,000")
	private Integer horizonteKm = FiltroVencimientos.HORIZONTE_KM_POR_DEFECTO;

	private Vencidos vencidos = Vencidos.INCLUIR;

	private Long localidadId;

	FiltroVencimientos aFiltro() {
		return new FiltroVencimientos(horizonteDias, horizonteKm, vencidos != Vencidos.OMITIR, localidadId);
	}
}
