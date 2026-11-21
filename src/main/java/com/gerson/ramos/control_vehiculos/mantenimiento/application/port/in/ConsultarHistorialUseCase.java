package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in;

import java.util.List;

public interface ConsultarHistorialUseCase {

	/**
	 * Historial del vehículo, del más reciente al más antiguo.
	 *
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe
	 */
	List<MantenimientoDetalle> historialDe(long vehiculoId);
}
