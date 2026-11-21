package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in;

import java.time.LocalDate;
import java.util.List;

public interface ConsultarMantenimientosRealizadosUseCase {

	/**
	 * Mantenimientos de toda la flota (incluidos los de vehículos hoy inactivos) con fecha entre {@code desde} y
	 * {@code hasta}, ambas inclusive, del más reciente al más antiguo.
	 *
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException si {@code desde} es posterior a {@code hasta}
	 */
	List<MantenimientoRealizado> entre(LocalDate desde, LocalDate hasta);
}
