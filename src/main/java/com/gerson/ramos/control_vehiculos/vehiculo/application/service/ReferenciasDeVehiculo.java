package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;

/**
 * Valida que las referencias de un vehículo a catálogos existan y estén activas. Si el valor no cambia respecto
 * al actual (vehículo existente), no se exige que siga activo: así se puede seguir editando un vehículo cuya
 * localidad fue desactivada después.
 */
@Component
class ReferenciasDeVehiculo {

	private final ConsultarCatalogosUseCase catalogos;

	ReferenciasDeVehiculo(ConsultarCatalogosUseCase catalogos) {
		this.catalogos = catalogos;
	}

	LocalidadId localidad(long id, @Nullable LocalidadId actual) {
		LocalidadId nueva = new LocalidadId(id);
		if (!nueva.equals(actual) && !catalogos.buscarLocalidad(nueva).map(Localidad::activo).orElse(false)) {
			throw new ConflictoDeDatosException("localidadId", "La localidad seleccionada no existe o está inactiva");
		}
		return nueva;
	}

	TipoUsoId tipoUso(long id, @Nullable TipoUsoId actual) {
		TipoUsoId nuevo = new TipoUsoId(id);
		if (!nuevo.equals(actual) && !catalogos.buscarTipoUso(nuevo).map(TipoUso::activo).orElse(false)) {
			throw new ConflictoDeDatosException("tipoUsoId", "El tipo de uso seleccionado no existe o está inactivo");
		}
		return nuevo;
	}

	@Nullable
	RutinaMantenimientoId rutina(@Nullable Long id, @Nullable RutinaMantenimientoId actual) {
		if (id == null) {
			return null;
		}
		RutinaMantenimientoId nueva = new RutinaMantenimientoId(id);
		if (!nueva.equals(actual) && !catalogos.buscarRutina(nueva).map(RutinaMantenimiento::activo).orElse(false)) {
			throw new ConflictoDeDatosException("rutinaId", "La rutina seleccionada no existe o está inactiva");
		}
		return nueva;
	}
}
