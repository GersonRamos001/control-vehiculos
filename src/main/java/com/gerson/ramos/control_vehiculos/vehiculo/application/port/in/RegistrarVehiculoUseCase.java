package com.gerson.ramos.control_vehiculos.vehiculo.application.port.in;

import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

public interface RegistrarVehiculoUseCase {

	/**
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException si la placa o el código
	 * de equipo ya existen, o si la localidad, el tipo de uso o la rutina no existen o están inactivos
	 */
	Vehiculo registrar(RegistrarVehiculoComando comando);
}
