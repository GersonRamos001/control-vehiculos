package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroRealizados;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Parámetros (GET) del reporte de mantenimientos realizados. Sin fechas se usan los valores por defecto del reporte
 * (últimos 90 días hasta hoy); el controlador copia aquí el período efectivo para mostrarlo.
 */
@Getter
@Setter
public class FiltroRealizadosForm {

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate desde;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate hasta;

	private Long localidadId;

	private Long rutinaId;

	@Size(max = 10, message = "La placa no puede exceder 10 caracteres")
	private String placa;

	FiltroRealizados aFiltro() {
		return new FiltroRealizados(desde, hasta, localidadId, rutinaId, placa);
	}
}
