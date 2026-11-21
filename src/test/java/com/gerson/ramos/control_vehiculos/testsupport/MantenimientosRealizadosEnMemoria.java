package com.gerson.ramos.control_vehiculos.testsupport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarMantenimientosRealizadosUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;

/** Consulta de mantenimientos realizados simulada: filtra por rango inclusivo y ordena del más reciente al más antiguo. */
public class MantenimientosRealizadosEnMemoria implements ConsultarMantenimientosRealizadosUseCase {

	private final List<MantenimientoRealizado> filas = new ArrayList<>();
	private LocalDate ultimoDesde;
	private LocalDate ultimoHasta;

	public MantenimientosRealizadosEnMemoria con(MantenimientoRealizado fila) {
		filas.add(fila);
		return this;
	}

	@Override
	public List<MantenimientoRealizado> entre(LocalDate desde, LocalDate hasta) {
		ultimoDesde = desde;
		ultimoHasta = hasta;
		return filas.stream()
			.filter(fila -> !fila.fecha().isBefore(desde) && !fila.fecha().isAfter(hasta))
			.sorted(Comparator.comparing(MantenimientoRealizado::fecha).reversed()
				.thenComparing(Comparator.comparingLong(MantenimientoRealizado::id).reversed()))
			.toList();
	}

	/** Último rango con el que se consultó (para comprobar los valores por defecto). */
	public LocalDate ultimoDesde() {
		return ultimoDesde;
	}

	public LocalDate ultimoHasta() {
		return ultimoHasta;
	}
}
