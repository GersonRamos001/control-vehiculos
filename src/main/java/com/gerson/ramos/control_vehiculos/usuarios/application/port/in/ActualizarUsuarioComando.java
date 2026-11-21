package com.gerson.ramos.control_vehiculos.usuarios.application.port.in;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;

/**
 * Corrección de nombre y rol. {@code version} es la que leyó el formulario (bloqueo optimista) y {@code actor} el
 * nombre de usuario de quien hace el cambio (para impedir que se quite el rol de administrador a sí mismo).
 */
public record ActualizarUsuarioComando(long id, @Nullable Long version, String nombreCompleto, Rol rol, String actor) {
}
