package com.gerson.ramos.control_vehiculos.vehiculo.domain.model;

import java.util.regex.Pattern;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/** Código de equipo del vehículo. Se normaliza (trim + mayúsculas); hasta 30 caracteres sin espacios. */
public record CodigoEquipo(String valor) {

	public static final int LONGITUD_MAXIMA = 30;

	private static final Pattern PATRON = Pattern.compile("[A-Z0-9][A-Z0-9_./-]{0," + (LONGITUD_MAXIMA - 1) + "}");

	public CodigoEquipo {
		valor = Validaciones.requerirCodigo(valor, "El código de equipo", PATRON);
	}
}
