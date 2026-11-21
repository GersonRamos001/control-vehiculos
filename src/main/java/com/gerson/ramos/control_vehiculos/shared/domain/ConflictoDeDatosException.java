package com.gerson.ramos.control_vehiculos.shared.domain;

/**
 * Conflicto con datos existentes o inválidos asociado a un campo concreto del formulario
 * (p. ej. placa duplicada, localidad inactiva). La capa web lo muestra junto a ese campo.
 */
public class ConflictoDeDatosException extends ReglaDeNegocioException {

	private final String campo;

	public ConflictoDeDatosException(String campo, String mensaje) {
		super(mensaje);
		this.campo = campo;
	}

	public String campo() {
		return campo;
	}
}
