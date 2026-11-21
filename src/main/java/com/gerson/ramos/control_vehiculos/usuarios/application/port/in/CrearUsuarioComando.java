package com.gerson.ramos.control_vehiculos.usuarios.application.port.in;

import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;

/** Alta de un usuario por un administrador. {@code clave} viaja en claro solo hasta el servicio, que la codifica. */
public record CrearUsuarioComando(String username, String nombreCompleto, Rol rol, String clave) {
}
