package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.UsuarioId;

@Component
class UsuarioPersistenceMapper {

	Usuario aDominio(UsuarioJpaEntity entidad) {
		return Usuario.reconstruir(new UsuarioId(entidad.getId()), entidad.getUsername(), entidad.getNombreCompleto(),
				Rol.valueOf(entidad.getRol()), entidad.isActivo(), entidad.getClaveHash(), entidad.getIntentosFallidos(),
				entidad.getBloqueadoHasta(), entidad.getUltimoAcceso(), entidad.getVersion());
	}

	UsuarioJpaEntity aNuevaEntidad(Usuario usuario) {
		UsuarioJpaEntity entidad = new UsuarioJpaEntity(usuario.username());
		actualizar(usuario, entidad);
		return entidad;
	}

	void actualizar(Usuario usuario, UsuarioJpaEntity entidad) {
		entidad.actualizar(usuario.nombreCompleto(), usuario.rol().name(), usuario.hashClave(), usuario.activo(),
				usuario.intentosFallidos(), usuario.bloqueadoHasta(), usuario.ultimoAcceso());
	}
}
