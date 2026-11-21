package com.gerson.ramos.control_vehiculos.vehiculo.domain.model;

import java.util.regex.Pattern;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/** Placa del vehículo. Se normaliza (trim + mayúsculas); formato permisivo: letras, dígitos y guion, 4 a 10 caracteres. */
public record Placa(String valor) {

	public static final int LONGITUD_MAXIMA = 10;

	private static final Pattern PATRON = Pattern.compile("[A-Z0-9-]{4," + LONGITUD_MAXIMA + "}");

	public Placa {
		valor = Validaciones.requerirCodigo(valor, "La placa", PATRON);
	}
}
