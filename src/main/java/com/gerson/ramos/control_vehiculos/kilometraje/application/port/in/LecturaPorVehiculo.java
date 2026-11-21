package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

/** Una fila de la captura semanal: la lectura del odómetro de un vehículo. */
public record LecturaPorVehiculo(long vehiculoId, int kilometraje) {
}
