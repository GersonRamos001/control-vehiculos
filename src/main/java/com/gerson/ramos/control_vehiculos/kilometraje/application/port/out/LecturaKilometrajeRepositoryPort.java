package com.gerson.ramos.control_vehiculos.kilometraje.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaId;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Puerto de salida: persistencia de las lecturas de odómetro. */
public interface LecturaKilometrajeRepositoryPort {

	/** Inserta la lectura (id nulo) o actualiza el kilometraje de la existente (id no nulo); devuelve el estado persistido. */
	LecturaKilometraje guardar(LecturaKilometraje lectura);

	Optional<LecturaKilometraje> buscarPorVehiculoYFecha(VehiculoId vehiculoId, LocalDate fecha);

	/** Lecturas del vehículo, de la más reciente a la más antigua. */
	List<LecturaKilometraje> historialDe(VehiculoId vehiculoId);

	/** La lectura más reciente (mayor fecha) del vehículo. */
	Optional<LecturaKilometraje> ultimaDe(VehiculoId vehiculoId);

	/** La lectura más reciente de cada vehículo que tenga alguna. */
	List<LecturaKilometraje> ultimasDeTodosLosVehiculos();

	void eliminar(LecturaId id);
}
