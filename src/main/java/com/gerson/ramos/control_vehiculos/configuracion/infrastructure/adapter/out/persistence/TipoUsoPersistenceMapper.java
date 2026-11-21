package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;

@Component
class TipoUsoPersistenceMapper {

	TipoUso aDominio(TipoUsoJpaEntity entidad) {
		return TipoUso.reconstruir(new TipoUsoId(entidad.getId()), new CodigoCatalogo(entidad.getCodigo()),
				entidad.getNombre(), entidad.isActivo());
	}

	TipoUsoJpaEntity aNuevaEntidad(TipoUso tipoUso) {
		return new TipoUsoJpaEntity(tipoUso.codigo().valor(), tipoUso.nombre(), tipoUso.activo());
	}

	void actualizar(TipoUso tipoUso, TipoUsoJpaEntity entidad) {
		entidad.actualizar(tipoUso.nombre(), tipoUso.activo());
	}
}
