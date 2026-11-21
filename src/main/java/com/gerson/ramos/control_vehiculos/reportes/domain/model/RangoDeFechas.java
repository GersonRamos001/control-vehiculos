package com.gerson.ramos.control_vehiculos.reportes.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/**
 * Período de un reporte, con ambos extremos incluidos y de a lo sumo {@link #MAXIMO_DIAS} días (5 años). Los errores se
 * lanzan como {@link ConflictoDeDatosException} ligados al campo del formulario ({@code desde} o {@code hasta}).
 */
public record RangoDeFechas(LocalDate desde, LocalDate hasta) {

	/** Máximo de días de un período (5 años): evita consultas y archivos desmesurados. */
	public static final int MAXIMO_DIAS = 1_827;

	public RangoDeFechas {
		Validaciones.requerir(desde, "La fecha inicial");
		Validaciones.requerir(hasta, "La fecha final");
		if (desde.isAfter(hasta)) {
			throw new ConflictoDeDatosException("desde", "La fecha inicial no puede ser posterior a la final");
		}
		if (ChronoUnit.DAYS.between(desde, hasta) + 1 > MAXIMO_DIAS) {
			throw new ConflictoDeDatosException("desde", "El período no puede superar 5 años");
		}
	}

	/** Un reporte de lo ya realizado no puede llegar al futuro. */
	public void verificarNoFutura(LocalDate hoy) {
		if (hasta.isAfter(Validaciones.requerir(hoy, "La fecha actual"))) {
			throw new ConflictoDeDatosException("hasta", "La fecha final no puede ser futura");
		}
	}

	/** Cantidad de días del rango contando ambos extremos. */
	public long dias() {
		return ChronoUnit.DAYS.between(desde, hasta) + 1;
	}
}
