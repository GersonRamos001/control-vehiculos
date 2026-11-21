package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import java.util.regex.Pattern;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/** Código funcional de un elemento de catálogo (p. ej. OPERATIVO, 10K). Se guarda normalizado en mayúsculas. */
public record CodigoCatalogo(String valor) {

	private static final Pattern PATRON = Pattern.compile("[A-Z0-9][A-Z0-9_-]{0,29}");

	public CodigoCatalogo {
		valor = Validaciones.requerirCodigo(valor, "El código", PATRON);
	}
}
