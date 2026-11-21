package com.gerson.ramos.control_vehiculos.vehiculo.application.port.in;

import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

/** Baja y alta lógica de vehículos. Ambas operaciones son idempotentes. */
public interface CambiarEstadoVehiculoUseCase {

	Vehiculo desactivar(long vehiculoId);

	Vehiculo activar(long vehiculoId);
}
