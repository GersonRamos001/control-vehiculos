package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.EliminarUltimaLecturaUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.out.LecturaKilometrajeRepositoryPort;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

@Service
@Transactional
public class EliminarUltimaLecturaService implements EliminarUltimaLecturaUseCase {

	private final LecturaKilometrajeRepositoryPort lecturas;
	private final ConsultarVehiculosUseCase vehiculos;

	EliminarUltimaLecturaService(LecturaKilometrajeRepositoryPort lecturas, ConsultarVehiculosUseCase vehiculos) {
		this.lecturas = lecturas;
		this.vehiculos = vehiculos;
	}

	@Override
	public LecturaKilometraje eliminarUltima(long vehiculoId) {
		Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId);
		LecturaKilometraje ultima = lecturas.ultimaDe(vehiculo.id())
			.orElseThrow(() -> new RecursoNoEncontradoException("Lectura del vehículo " + vehiculo.placa().valor(), vehiculoId));
		lecturas.eliminar(ultima.id());
		return ultima;
	}
}
