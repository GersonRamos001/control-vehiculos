package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import java.util.List;

public interface ConsultarLecturasUseCase {

	/**
	 * Historial del vehículo, de la lectura más reciente a la más antigua, con km recorridos y días desde la anterior.
	 *
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe
	 */
	List<LecturaDetalle> historialDe(long vehiculoId);
}
