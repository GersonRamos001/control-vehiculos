package com.gerson.ramos.control_vehiculos.vehiculo.application.port.in;

import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

/** Actualiza la última rutina de mantenimiento realizada de un vehículo (lo usa el módulo de mantenimiento). */
public interface RegistrarRutinaRealizadaUseCase {

	/**
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException si la rutina no existe o está inactiva
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException si el vehículo está inactivo
	 */
	Vehiculo registrarRutinaRealizada(long vehiculoId, long rutinaId);
}
