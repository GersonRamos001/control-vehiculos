package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Puerto de salida: persistencia del historial de mantenimientos. */
public interface MantenimientoRepositoryPort {

	/** Inserta el mantenimiento (id nulo) y devuelve el estado persistido con su id. */
	Mantenimiento guardar(Mantenimiento mantenimiento);

	/** Historial del vehículo, del más reciente al más antiguo (fecha desc, id desc). */
	List<Mantenimiento> historialDe(VehiculoId vehiculoId);

	/** El mantenimiento más reciente del vehículo (mayor fecha; con igual fecha, mayor id). */
	Optional<Mantenimiento> ultimoDe(VehiculoId vehiculoId);

	/** El mantenimiento más reciente de cada vehículo que tenga alguno. */
	List<Mantenimiento> ultimosDeTodosLosVehiculos();

	/** Mantenimientos de toda la flota con fecha entre {@code desde} y {@code hasta} (ambas inclusive), del más reciente al más antiguo. */
	List<Mantenimiento> realizadosEntre(LocalDate desde, LocalDate hasta);
}
