package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.CambiarEstadoVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Service
@Transactional
public class CambiarEstadoVehiculoService implements CambiarEstadoVehiculoUseCase {

	private final VehiculoRepositoryPort vehiculos;

	CambiarEstadoVehiculoService(VehiculoRepositoryPort vehiculos) {
		this.vehiculos = vehiculos;
	}

	@Override
	public Vehiculo desactivar(long vehiculoId) {
		Vehiculo vehiculo = cargar(vehiculoId);
		if (!vehiculo.activo()) {
			return vehiculo;
		}
		vehiculo.desactivar();
		return vehiculos.guardar(vehiculo);
	}

	@Override
	public Vehiculo activar(long vehiculoId) {
		Vehiculo vehiculo = cargar(vehiculoId);
		if (vehiculo.activo()) {
			return vehiculo;
		}
		vehiculo.activar();
		return vehiculos.guardar(vehiculo);
	}

	private Vehiculo cargar(long vehiculoId) {
		return vehiculos.buscarPorId(new VehiculoId(vehiculoId))
			.orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", vehiculoId));
	}
}
