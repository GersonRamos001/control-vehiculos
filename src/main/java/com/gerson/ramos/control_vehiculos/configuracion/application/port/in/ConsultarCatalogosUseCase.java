package com.gerson.ramos.control_vehiculos.configuracion.application.port.in;

import java.util.List;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;

/** Consulta de catálogos de solo lectura. Es la puerta que otros módulos (p. ej. vehículos) usan para leer catálogos. */
public interface ConsultarCatalogosUseCase {

	List<Localidad> listarLocalidades(boolean soloActivas);

	List<TipoUso> listarTiposUso(boolean soloActivos);

	List<RutinaMantenimiento> listarRutinas(boolean soloActivas);

	Optional<Localidad> buscarLocalidad(LocalidadId id);

	Optional<TipoUso> buscarTipoUso(TipoUsoId id);

	Optional<RutinaMantenimiento> buscarRutina(RutinaMantenimientoId id);
}
