package com.gerson.ramos.control_vehiculos.reportes.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarMantenimientosRealizadosUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ConteoPorClave;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizados;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteRealizadosUseCase;
import com.gerson.ramos.control_vehiculos.reportes.domain.model.RangoDeFechas;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

@Service
@Transactional(readOnly = true)
public class ReporteRealizadosService implements ReporteRealizadosUseCase {

	/** Período por defecto cuando no se indica fecha inicial. */
	static final int DIAS_POR_DEFECTO = 90;

	private static final Comparator<ConteoPorClave> MAYOR_CANTIDAD_PRIMERO = Comparator
		.comparingInt(ConteoPorClave::cantidad).reversed().thenComparing(ConteoPorClave::clave);

	private final ConsultarMantenimientosRealizadosUseCase realizados;
	private final Clock reloj;

	ReporteRealizadosService(ConsultarMantenimientosRealizadosUseCase realizados, Clock reloj) {
		this.realizados = realizados;
		this.reloj = reloj;
	}

	@Override
	public ReporteRealizados generar(FiltroRealizados filtro) {
		Validaciones.requerir(filtro, "El filtro");
		LocalDate hoy = LocalDate.now(reloj);

		LocalDate hasta = filtro.hasta() != null ? filtro.hasta() : hoy;
		LocalDate desde = filtro.desde() != null ? filtro.desde() : hasta.minusDays(DIAS_POR_DEFECTO);
		RangoDeFechas rango = new RangoDeFechas(desde, hasta);
		rango.verificarNoFutura(hoy);

		@Nullable String placa = normalizar(filtro.placa());
		List<MantenimientoRealizado> filas = realizados.entre(rango.desde(), rango.hasta()).stream()
			.filter(fila -> filtro.localidadId() == null || fila.localidadId() == filtro.localidadId())
			.filter(fila -> filtro.rutinaId() == null || fila.rutinaId() == filtro.rutinaId())
			.filter(fila -> placa == null || fila.placa().toUpperCase(Locale.ROOT).contains(placa))
			.toList();

		return new ReporteRealizados(rango, filas, contar(filas, MantenimientoRealizado::rutina),
				contar(filas, MantenimientoRealizado::localidad));
	}

	private static @Nullable String normalizar(@Nullable String placa) {
		if (placa == null || placa.isBlank()) {
			return null;
		}
		return placa.strip().toUpperCase(Locale.ROOT);
	}

	private static List<ConteoPorClave> contar(List<MantenimientoRealizado> filas, Function<MantenimientoRealizado, String> clave) {
		Map<String, Integer> conteos = new LinkedHashMap<>();
		for (MantenimientoRealizado fila : filas) {
			conteos.merge(clave.apply(fila), 1, Integer::sum);
		}
		return conteos.entrySet().stream()
			.map(entrada -> new ConteoPorClave(entrada.getKey(), entrada.getValue()))
			.sorted(MAYOR_CANTIDAD_PRIMERO)
			.toList();
	}
}
