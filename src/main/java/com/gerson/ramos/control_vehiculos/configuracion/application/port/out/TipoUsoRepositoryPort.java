package com.gerson.ramos.control_vehiculos.configuracion.application.port.out;

import java.util.List;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;

/** Puerto de salida: persistencia de tipos de uso. */
public interface TipoUsoRepositoryPort {

	/** Inserta (id nulo) o actualiza el tipo de uso y devuelve el estado persistido. */
	TipoUso guardar(TipoUso tipoUso);

	Optional<TipoUso> buscarPorId(TipoUsoId id);

	Optional<TipoUso> buscarPorCodigo(CodigoCatalogo codigo);

	boolean existePorCodigo(CodigoCatalogo codigo);

	List<TipoUso> listarActivos();

	List<TipoUso> listarTodos();
}
