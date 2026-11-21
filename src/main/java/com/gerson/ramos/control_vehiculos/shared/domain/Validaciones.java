package com.gerson.ramos.control_vehiculos.shared.domain;

import java.util.Locale;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/** Validaciones reutilizables del dominio. Todas lanzan {@link ReglaDeNegocioException}. */
public final class Validaciones {

	private static final int LONGITUD_MAXIMA_CODIGO = 100;

	private Validaciones() {
	}

	public static <T> T requerir(@Nullable T valor, String campo) {
		if (valor == null) {
			throw new ReglaDeNegocioException(campo + " es obligatorio");
		}
		return valor;
	}

	/** Texto obligatorio, sin espacios en los extremos y con longitud máxima. */
	public static String requerirTexto(@Nullable String valor, String campo, int longitudMaxima) {
		if (valor == null || valor.isBlank()) {
			throw new ReglaDeNegocioException(campo + " es obligatorio");
		}
		String texto = valor.strip();
		if (texto.length() > longitudMaxima) {
			throw new ReglaDeNegocioException(campo + " no puede exceder " + longitudMaxima + " caracteres");
		}
		return texto;
	}

	/** Código obligatorio: se normaliza (trim + mayúsculas) y debe cumplir el patrón. */
	public static String requerirCodigo(@Nullable String valor, String campo, Pattern patron) {
		String codigo = requerirTexto(valor, campo, LONGITUD_MAXIMA_CODIGO).toUpperCase(Locale.ROOT);
		if (!patron.matcher(codigo).matches()) {
			throw new ReglaDeNegocioException(campo + " tiene un formato inválido: '" + codigo + "' (esperado " + patron.pattern() + ")");
		}
		return codigo;
	}

	public static int requerirPositivo(int valor, String campo) {
		if (valor <= 0) {
			throw new ReglaDeNegocioException(campo + " debe ser mayor que cero");
		}
		return valor;
	}

	public static long requerirIdPositivo(long valor, String campo) {
		if (valor <= 0) {
			throw new ReglaDeNegocioException(campo + " inválido: " + valor);
		}
		return valor;
	}
}
