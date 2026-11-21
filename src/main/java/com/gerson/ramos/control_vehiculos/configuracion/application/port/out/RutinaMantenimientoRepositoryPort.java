package com.gerson.ramos.control_vehiculos.configuracion.application.port.out;

import java.util.List;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;

/** Puerto de salida: persistencia de rutinas de mantenimiento preventivo. */
public interface RutinaMantenimientoRepositoryPort {

	/** Inserta (id nulo) o actualiza la rutina y devuelve el estado persistido. */
	RutinaMantenimiento guardar(RutinaMantenimiento rutina);

	Optional<RutinaMantenimiento> buscarPorId(RutinaMantenimientoId id);

	Optional<RutinaMantenimiento> buscarPorCodigo(CodigoCatalogo codigo);

	boolean existePorCodigo(CodigoCatalogo codigo);

	List<RutinaMantenimiento> listarActivas();

	List<RutinaMantenimiento> listarTodas();
}
