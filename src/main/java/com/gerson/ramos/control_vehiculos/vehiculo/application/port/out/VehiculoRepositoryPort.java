package com.gerson.ramos.control_vehiculos.vehiculo.application.port.out;

import java.util.List;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Puerto de salida: persistencia de vehículos. */
public interface VehiculoRepositoryPort {

	/**
	 * Inserta (id nulo) o actualiza el vehículo y devuelve el estado persistido (con id y versión vigentes).
	 * Lanza una excepción de concurrencia si la versión del vehículo ya no es la vigente.
	 */
	Vehiculo guardar(Vehiculo vehiculo);

	Optional<Vehiculo> buscarPorId(VehiculoId id);

	Optional<Vehiculo> buscarPorPlaca(Placa placa);

	boolean existePorPlaca(Placa placa);

	boolean existePorCodigoEquipo(CodigoEquipo codigoEquipo);

	List<Vehiculo> listarActivos();

	List<Vehiculo> listarTodos();
}
