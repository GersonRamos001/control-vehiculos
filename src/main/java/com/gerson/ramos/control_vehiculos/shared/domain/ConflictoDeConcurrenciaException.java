package com.gerson.ramos.control_vehiculos.shared.domain;

/** El registro fue modificado por otra operación desde que el usuario lo leyó (bloqueo optimista). */
public class ConflictoDeConcurrenciaException extends ReglaDeNegocioException {

	public ConflictoDeConcurrenciaException(String recurso) {
		super(recurso + " fue modificado por otro usuario. Recarga la página y vuelve a intentarlo.");
	}
}
