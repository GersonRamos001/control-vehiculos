package com.gerson.ramos.control_vehiculos.vehiculo.application.port.in;

import org.jspecify.annotations.Nullable;

/** Datos para registrar un vehículo. {@code rutinaId} es opcional (vehículo aún sin mantenimiento registrado). */
public record RegistrarVehiculoComando(String placa, String codigoEquipo, long localidadId, long tipoUsoId,
		@Nullable Long rutinaId) {
}
