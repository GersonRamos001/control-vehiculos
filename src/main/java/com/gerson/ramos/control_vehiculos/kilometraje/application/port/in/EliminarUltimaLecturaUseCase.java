package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;

public interface EliminarUltimaLecturaUseCase {

	/**
	 * Elimina la lectura más reciente del vehículo (única forma de borrar lecturas) y la devuelve.
	 *
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe o
	 * no tiene lecturas
	 */
	LecturaKilometraje eliminarUltima(long vehiculoId);
}
