package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;

@Component
class RutinaMantenimientoPersistenceMapper {

	RutinaMantenimiento aDominio(RutinaMantenimientoJpaEntity entidad) {
		return RutinaMantenimiento.reconstruir(new RutinaMantenimientoId(entidad.getId()),
				new CodigoCatalogo(entidad.getCodigo()), entidad.getNombre(), entidad.isActivo());
	}

	RutinaMantenimientoJpaEntity aNuevaEntidad(RutinaMantenimiento rutina) {
		return new RutinaMantenimientoJpaEntity(rutina.codigo().valor(), rutina.nombre(), rutina.activo());
	}

	void actualizar(RutinaMantenimiento rutina, RutinaMantenimientoJpaEntity entidad) {
		entidad.actualizar(rutina.nombre(), rutina.activo());
	}
}
