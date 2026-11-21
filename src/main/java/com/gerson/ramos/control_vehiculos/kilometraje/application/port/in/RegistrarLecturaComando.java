package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import java.time.LocalDate;

/** Datos para registrar la lectura del odómetro de un vehículo en una fecha. */
public record RegistrarLecturaComando(long vehiculoId, LocalDate fecha, int kilometraje) {
}
