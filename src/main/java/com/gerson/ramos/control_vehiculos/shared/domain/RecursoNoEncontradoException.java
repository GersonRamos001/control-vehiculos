package com.gerson.ramos.control_vehiculos.shared.domain;

/** Se lanza cuando una entidad referenciada por id no existe. */
public class RecursoNoEncontradoException extends ReglaDeNegocioException {

	public RecursoNoEncontradoException(String recurso, Object id) {
		super(recurso + " no encontrado: " + id);
	}
}
