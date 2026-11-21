package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;

/** Resultado de registrar una lectura: qué pasó con la lectura de esa fecha. */
public record ResultadoRegistro(LecturaKilometraje lectura, Tipo tipo) {

	public enum Tipo {
		/** No había lectura para ese vehículo y fecha: se creó una. */
		NUEVA,
		/** Ya había una lectura de esa fecha con otro kilometraje: se reemplazó. */
		REEMPLAZADA,
		/** Ya había una lectura de esa fecha con el mismo kilometraje: no se tocó. */
		SIN_CAMBIOS
	}
}
