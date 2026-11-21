package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

public interface RegistrarLecturaUseCase {

	/**
	 * Registra la lectura; si ya existía una para ese vehículo y fecha, la reemplaza (así se corrige una lectura mal
	 * digitada).
	 *
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException si el vehículo no existe
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException si el vehículo está inactivo
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException fecha futura o kilometraje
	 * incoherente con las lecturas anteriores/posteriores o con el último mantenimiento
	 */
	ResultadoRegistro registrar(RegistrarLecturaComando comando);
}
