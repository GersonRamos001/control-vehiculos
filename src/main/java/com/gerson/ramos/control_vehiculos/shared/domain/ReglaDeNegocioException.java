package com.gerson.ramos.control_vehiculos.shared.domain;

/**
 * Se lanza cuando se viola una regla del dominio (dato inválido, transición de estado no permitida, etc.).
 * Es una excepción no verificada; la capa de entrada (web) decide cómo traducirla a una respuesta.
 */
public class ReglaDeNegocioException extends RuntimeException {

	public ReglaDeNegocioException(String mensaje) {
		super(mensaje);
	}
}
