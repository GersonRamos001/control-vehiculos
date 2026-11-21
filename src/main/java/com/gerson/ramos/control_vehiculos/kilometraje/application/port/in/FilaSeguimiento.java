package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/**
 * Seguimiento de un vehículo: cuánto falta para su siguiente mantenimiento según su última lectura.
 *
 * <p>Los datos de mantenimiento ({@code proximoKm}, {@code diasRestantes}…) son nulos si el vehículo aún no tiene
 * mantenimientos registrados. {@code kmActual} es el mayor entre la última lectura y el km del último mantenimiento;
 * {@code kmRestantes = proximoKm - kmActual} (negativo si ya se pasó). {@code pendiente} indica que no tiene
 * lecturas o que la última tiene más días que los configurados en los umbrales de alerta (7 por defecto).
 */
public record FilaSeguimiento(long vehiculoId, String placa, String codigoEquipo, String localidad,
		@Nullable LocalDate ultimoMantenimientoFecha, @Nullable Integer ultimoMantenimientoKm, @Nullable Long proximoKm,
		@Nullable Long diasRestantes, @Nullable LocalDate ultimaLecturaFecha, @Nullable Integer ultimaLecturaKm,
		@Nullable Long diasSinLectura, @Nullable Long kmActual, @Nullable Long kmRestantes, boolean pendiente) {

	public boolean sinProgramacion() {
		return proximoKm == null;
	}

	public boolean sinLectura() {
		return ultimaLecturaFecha == null;
	}

	public boolean vencidaPorKm() {
		return kmRestantes != null && kmRestantes < 0;
	}

	public boolean vencidaPorFecha() {
		return diasRestantes != null && diasRestantes < 0;
	}
}
