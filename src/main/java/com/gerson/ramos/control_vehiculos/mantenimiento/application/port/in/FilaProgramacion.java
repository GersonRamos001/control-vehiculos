package com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/**
 * Programación del siguiente mantenimiento de un vehículo. Si el vehículo aún no tiene mantenimientos registrados,
 * todos los campos de programación son nulos ({@link #sinRegistro()}).
 *
 * @param diasRestantes días hasta la fecha de referencia; negativo si ya se venció
 */
public record FilaProgramacion(long vehiculoId, String placa, String codigoEquipo, String localidad,
		@Nullable LocalDate ultimaFecha, @Nullable Integer ultimoKilometraje, @Nullable String ultimaRutina,
		@Nullable Long proximoKm, @Nullable LocalDate fechaReferencia, @Nullable Long diasRestantes) {

	public boolean sinRegistro() {
		return ultimaFecha == null;
	}

	public boolean vencidaPorFecha() {
		return diasRestantes != null && diasRestantes < 0;
	}
}
