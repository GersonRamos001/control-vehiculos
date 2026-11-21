package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in;

import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;

public interface RegistrarMantenimientoUseCase {

	/**
	 * Registra el mantenimiento y, si es el más reciente del vehículo, actualiza su última rutina realizada.
	 *
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException si el vehículo está inactivo
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException fecha futura, kilometraje
	 * incoherente con el historial, o rutina inexistente o inactiva
	 */
	Mantenimiento registrar(RegistrarMantenimientoComando comando);
}
