package com.gerson.ramos.control_vehiculos.configuracion.application.port.out;

import java.util.List;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;

/** Puerto de salida: persistencia de localidades. */
public interface LocalidadRepositoryPort {

	/** Inserta (id nulo) o actualiza la localidad y devuelve el estado persistido. */
	Localidad guardar(Localidad localidad);

	Optional<Localidad> buscarPorId(LocalidadId id);

	Optional<Localidad> buscarPorCodigo(CodigoCatalogo codigo);

	boolean existePorCodigo(CodigoCatalogo codigo);

	List<Localidad> listarActivas();

	List<Localidad> listarTodas();
}
