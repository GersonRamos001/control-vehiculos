package com.gerson.ramos.control_vehiculos.vehiculo.application.port.in;

import org.jspecify.annotations.Nullable;

/** Vista de lectura de un vehículo con los nombres de sus catálogos ya resueltos. */
public record VehiculoDetalle(long id, String placa, String codigoEquipo, long localidadId, String localidad,
		long tipoUsoId, String tipoUso, @Nullable Long rutinaId, @Nullable String rutina, boolean activo, long version) {
}
