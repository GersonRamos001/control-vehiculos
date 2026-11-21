package com.gerson.ramos.control_vehiculos.alertas.domain.model;

/** Tipos de alerta y su severidad. */
public enum TipoAlerta {

	MANTENIMIENTO_VENCIDO(Severidad.VENCIDA),
	MANTENIMIENTO_PROXIMO(Severidad.PROXIMA),
	/** El vehículo no tiene mantenimientos registrados, así que no se puede programar el siguiente. */
	SIN_MANTENIMIENTO(Severidad.INFORMATIVA),
	/** Sin lecturas de kilometraje o con la última demasiado antigua. */
	LECTURA_PENDIENTE(Severidad.INFORMATIVA);

	private final Severidad severidad;

	TipoAlerta(Severidad severidad) {
		this.severidad = severidad;
	}

	public Severidad severidad() {
		return severidad;
	}
}
