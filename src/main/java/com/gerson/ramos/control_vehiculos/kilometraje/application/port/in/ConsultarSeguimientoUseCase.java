package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import java.util.List;

public interface ConsultarSeguimientoUseCase {

	/** Seguimiento de los vehículos activos: primero los pendientes de lectura, luego por placa. */
	List<FilaSeguimiento> seguimientoSemanal();

	/** @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe */
	FilaSeguimiento seguimientoDe(long vehiculoId);
}
