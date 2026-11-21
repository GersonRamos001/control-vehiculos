package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/** Vista de lectura de un mantenimiento del historial, con el código de la rutina ya resuelto. */
public record MantenimientoDetalle(long id, LocalDate fecha, int kilometraje, long rutinaId, String rutina,
		@Nullable String observaciones) {
}
