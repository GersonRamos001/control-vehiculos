package com.gerson.ramos.control_vehiculos.alertas.domain.model;

/**
 * Una alerta evaluada para un vehículo. En las alertas de mantenimiento, {@code porKm} y {@code porFecha} indican
 * por qué criterio(s) se disparó (pueden ser ambos); en las informativas son falsos.
 */
public record Alerta(TipoAlerta tipo, boolean porKm, boolean porFecha) {

	public Severidad severidad() {
		return tipo.severidad();
	}
}
