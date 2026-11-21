package com.gerson.ramos.control_vehiculos.usuarios.application.port.in;

import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;

/** Lo mínimo que necesita el inicio de sesión de un usuario. {@code bloqueado} ya está evaluado con la hora actual. */
public record CredencialesDeAcceso(String username, String hashClave, Rol rol, boolean activo, boolean bloqueado) {
}
