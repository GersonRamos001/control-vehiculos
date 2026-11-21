package com.gerson.ramos.control_vehiculos.alertas.domain.model;

/** Qué tan urgente es una alerta. El orden de declaración es el orden de aparición (la más urgente primero). */
public enum Severidad {
	/** El mantenimiento ya se pasó (por km o por fecha). */
	VENCIDA,
	/** El mantenimiento está cerca (dentro de los umbrales). */
	PROXIMA,
	/** No es urgente, pero conviene atenderla para que el seguimiento sea confiable. */
	INFORMATIVA
}
