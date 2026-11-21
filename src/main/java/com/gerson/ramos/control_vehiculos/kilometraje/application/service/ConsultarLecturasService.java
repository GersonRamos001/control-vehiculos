package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarLecturasUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.LecturaDetalle;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.out.LecturaKilometrajeRepositoryPort;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

@Service
@Transactional(readOnly = true)
public class ConsultarLecturasService implements ConsultarLecturasUseCase {

	private final LecturaKilometrajeRepositoryPort lecturas;
	private final ConsultarVehiculosUseCase vehiculos;

	ConsultarLecturasService(LecturaKilometrajeRepositoryPort lecturas, ConsultarVehiculosUseCase vehiculos) {
		this.lecturas = lecturas;
		this.vehiculos = vehiculos;
	}

	@Override
	public List<LecturaDetalle> historialDe(long vehiculoId) {
		Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId);
		List<LecturaKilometraje> historial = lecturas.historialDe(vehiculo.id()); // de la más reciente a la más antigua

		List<LecturaDetalle> detalle = new ArrayList<>(historial.size());
		for (int i = 0; i < historial.size(); i++) {
			LecturaKilometraje actual = historial.get(i);
			if (i + 1 < historial.size()) {
				LecturaKilometraje anterior = historial.get(i + 1);
				detalle.add(new LecturaDetalle(actual.id().valor(), actual.fecha(), actual.kilometraje(),
						actual.kilometraje() - anterior.kilometraje(), ChronoUnit.DAYS.between(anterior.fecha(), actual.fecha())));
			}
			else {
				detalle.add(new LecturaDetalle(actual.id().valor(), actual.fecha(), actual.kilometraje(), null, null));
			}
		}
		return detalle;
	}
}
