package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in;

import java.util.List;

public interface ConsultarProgramacionUseCase {

	/**
	 * Programación de todos los vehículos activos, ordenada por días restantes (los vencidos primero); los que no
	 * tienen mantenimientos registrados van al final, ordenados por placa.
	 */
	List<FilaProgramacion> programacionDeVehiculosActivos();

	/** @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe */
	FilaProgramacion programacionDe(long vehiculoId);
}
