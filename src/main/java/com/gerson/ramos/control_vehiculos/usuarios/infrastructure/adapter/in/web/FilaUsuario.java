package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.web;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

/** Una fila de la lista de usuarios, con los textos ya listos para mostrar. */
record FilaUsuario(long id, String username, String nombreCompleto, String rol, boolean activo, boolean bloqueado,
		String ultimoAcceso, boolean esActual) {

	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	static FilaUsuario de(Usuario usuario, Instant ahora, ZoneId zona, String actor) {
		Instant ultimo = usuario.ultimoAcceso();
		return new FilaUsuario(usuario.id().valor(), usuario.username(), usuario.nombreCompleto(), usuario.rol().etiqueta(),
				usuario.activo(), usuario.estaBloqueado(ahora), ultimo == null ? "Nunca" : FORMATO.withZone(zona).format(ultimo),
				usuario.username().equalsIgnoreCase(actor));
	}
}
