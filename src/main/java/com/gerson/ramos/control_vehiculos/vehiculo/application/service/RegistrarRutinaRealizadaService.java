package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarRutinaRealizadaUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Service
@Transactional
public class RegistrarRutinaRealizadaService implements RegistrarRutinaRealizadaUseCase {

	private final VehiculoRepositoryPort vehiculos;
	private final ReferenciasDeVehiculo referencias;

	RegistrarRutinaRealizadaService(VehiculoRepositoryPort vehiculos, ReferenciasDeVehiculo referencias) {
		this.vehiculos = vehiculos;
		this.referencias = referencias;
	}

	@Override
	public Vehiculo registrarRutinaRealizada(long vehiculoId, long rutinaId) {
		Vehiculo vehiculo = vehiculos.buscarPorId(new VehiculoId(vehiculoId))
			.orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", vehiculoId));
		RutinaMantenimientoId rutina = referencias.rutina(rutinaId, vehiculo.ultimaRutinaId());
		if (rutina == null) {
			return vehiculo;
		}
		if (rutina.equals(vehiculo.ultimaRutinaId())) {
			return vehiculo;
		}
		vehiculo.registrarRutinaRealizada(rutina);
		return vehiculos.guardar(vehiculo);
	}
}
