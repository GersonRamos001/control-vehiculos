package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

import java.time.LocalDate;

/** Un vehículo con su siguiente mantenimiento; {@code vencido} si ya se pasó por km o por fecha (valores restantes negativos). */
public record FilaVencimiento(long vehiculoId, String placa, String codigoEquipo, String localidad, LocalDate ultimoMantenimientoFecha,
		int ultimoMantenimientoKm, long proximoKm, long kmRestantes, LocalDate fechaReferencia, long diasRestantes, boolean vencido) {
}
