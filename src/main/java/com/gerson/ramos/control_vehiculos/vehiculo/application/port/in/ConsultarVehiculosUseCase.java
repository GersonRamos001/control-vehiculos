package com.gerson.ramos.control_vehiculos.vehiculo.application.port.in;

import java.util.List;

import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

public interface ConsultarVehiculosUseCase {

	/** Vehículos ordenados por placa. */
	List<VehiculoDetalle> listar(FiltroEstado filtro);

	/** @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si no existe */
	Vehiculo buscarPorId(long vehiculoId);
}
