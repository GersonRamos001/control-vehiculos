package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/** Un mantenimiento del historial de la flota con los datos del vehículo y el código de la rutina ya resueltos. */
public record MantenimientoRealizado(long id, long vehiculoId, String placa, String codigoEquipo, long localidadId,
		String localidad, LocalDate fecha, int kilometraje, long rutinaId, String rutina, @Nullable String observaciones) {
}
