package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import java.time.LocalDate;
import java.util.List;

public interface RegistrarLecturasSemanalesUseCase {

	/**
	 * Registra de una vez las lecturas de varios vehículos con una fecha común. Cada fila se procesa de forma
	 * independiente (como {@link RegistrarLecturaUseCase}); reenviar las mismas filas es seguro porque reemplaza.
	 */
	ResultadoLote registrar(LocalDate fecha, List<LecturaPorVehiculo> lecturas);
}
