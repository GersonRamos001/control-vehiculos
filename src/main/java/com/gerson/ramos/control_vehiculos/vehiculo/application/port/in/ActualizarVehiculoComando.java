package com.gerson.ramos.control_vehiculos.vehiculo.application.port.in;

import org.jspecify.annotations.Nullable;

/**
 * Datos para actualizar un vehículo. {@code version} es la versión que el usuario leyó (bloqueo optimista).
 * Si {@code rutinaId} es nulo la última rutina registrada se conserva sin cambios.
 */
public record ActualizarVehiculoComando(long id, long version, String placa, String codigoEquipo, long localidadId,
		long tipoUsoId, @Nullable Long rutinaId) {
}
