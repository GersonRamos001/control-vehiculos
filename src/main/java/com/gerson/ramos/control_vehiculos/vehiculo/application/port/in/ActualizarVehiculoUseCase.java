package com.gerson.ramos.control_vehiculos.vehiculo.application.port.in;

import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

public interface ActualizarVehiculoUseCase {

	/**
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeConcurrenciaException si la versión no es la vigente
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException por duplicados o catálogos inactivos
	 */
	Vehiculo actualizar(ActualizarVehiculoComando comando);
}
