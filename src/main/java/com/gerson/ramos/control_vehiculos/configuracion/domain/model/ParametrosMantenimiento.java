package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/**
 * Parámetros globales del mantenimiento preventivo: cada cuántos kilómetros y meses corresponde el siguiente
 * mantenimiento, sin importar la rutina (10K/20K/30K) realizada. Hoy: 10,000 km y 6 meses.
 */
public record ParametrosMantenimiento(int intervaloKm, int intervaloMeses) {

	public ParametrosMantenimiento {
		Validaciones.requerirPositivo(intervaloKm, "El intervalo en kilómetros");
		Validaciones.requerirPositivo(intervaloMeses, "El intervalo en meses");
	}
}
