package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/** Datos para registrar un mantenimiento preventivo realizado a un vehículo. */
public record RegistrarMantenimientoComando(long vehiculoId, LocalDate fecha, int kilometraje, long rutinaId,
		@Nullable String observaciones) {
}
