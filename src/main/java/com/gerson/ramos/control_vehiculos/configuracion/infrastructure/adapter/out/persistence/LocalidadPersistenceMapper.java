package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;

@Component
class LocalidadPersistenceMapper {

	Localidad aDominio(LocalidadJpaEntity entidad) {
		return Localidad.reconstruir(new LocalidadId(entidad.getId()), new CodigoCatalogo(entidad.getCodigo()),
				entidad.getNombre(), entidad.isActivo());
	}

	LocalidadJpaEntity aNuevaEntidad(Localidad localidad) {
		return new LocalidadJpaEntity(localidad.codigo().valor(), localidad.nombre(), localidad.activo());
	}

	void actualizar(Localidad localidad, LocalidadJpaEntity entidad) {
		entidad.actualizar(localidad.nombre(), localidad.activo());
	}
}
