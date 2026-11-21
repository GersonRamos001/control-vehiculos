package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/**
 * Lectura del historial con su diferencia respecto a la lectura anterior. La primera lectura del vehículo no tiene
 * diferencia (los dos campos son nulos).
 */
public record LecturaDetalle(long id, LocalDate fecha, int kilometraje, @Nullable Integer kmRecorridos,
		@Nullable Long diasDesdeAnterior) {
}
