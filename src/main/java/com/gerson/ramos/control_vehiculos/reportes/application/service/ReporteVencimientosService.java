package com.gerson.ramos.control_vehiculos.reportes.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarSeguimientoUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaVencimiento;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientos;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReporteVencimientosUseCase;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;

@Service
@Transactional(readOnly = true)
public class ReporteVencimientosService implements ReporteVencimientosUseCase {

	private static final Comparator<FilaVencimiento> ORDEN = Comparator
		.comparingLong(FilaVencimiento::diasRestantes)
		.thenComparingLong(FilaVencimiento::kmRestantes)
		.thenComparing(FilaVencimiento::placa);

	private final ConsultarSeguimientoUseCase seguimiento;
	private final ConsultarVehiculosUseCase vehiculos;
	private final Clock reloj;

	ReporteVencimientosService(ConsultarSeguimientoUseCase seguimiento, ConsultarVehiculosUseCase vehiculos, Clock reloj) {
		this.seguimiento = seguimiento;
		this.vehiculos = vehiculos;
		this.reloj = reloj;
	}

	@Override
	public ReporteVencimientos generar(FiltroVencimientos filtro) {
		Validaciones.requerir(filtro, "El filtro");
		if (filtro.horizonteDias() <= 0) {
			throw new ConflictoDeDatosException("horizonteDias", "El horizonte en días debe ser mayor que cero");
		}
		if (filtro.horizonteDias() > FiltroVencimientos.HORIZONTE_DIAS_MAXIMO) {
			throw new ConflictoDeDatosException("horizonteDias", "El horizonte en días no puede superar 3,650 (10 años)");
		}
		if (filtro.horizonteKm() <= 0) {
			throw new ConflictoDeDatosException("horizonteKm", "El horizonte en kilómetros debe ser mayor que cero");
		}
		if (filtro.horizonteKm() > FiltroVencimientos.HORIZONTE_KM_MAXIMO) {
			throw new ConflictoDeDatosException("horizonteKm", "El horizonte en kilómetros no puede superar 1,000,000");
		}
		LocalDate hoy = LocalDate.now(reloj);

		Map<Long, VehiculoDetalle> flota = new HashMap<>();
		for (VehiculoDetalle vehiculo : vehiculos.listar(FiltroEstado.ACTIVOS)) {
			flota.put(vehiculo.id(), vehiculo);
		}

		List<FilaVencimiento> filas = seguimiento.seguimientoSemanal().stream()
			.filter(fila -> !fila.sinProgramacion())
			.filter(ReporteVencimientosService::tieneProgramacionCompleta)
			.filter(fila -> perteneceALocalidad(flota.get(fila.vehiculoId()), filtro.localidadId()))
			.filter(fila -> filtro.incluirVencidos() || !vencida(fila))
			.filter(fila -> dentroDelHorizonte(fila, filtro))
			.map(fila -> aFila(fila, hoy))
			.sorted(ORDEN)
			.toList();

		int vencidos = (int) filas.stream().filter(FilaVencimiento::vencido).count();
		return new ReporteVencimientos(filas, vencidos, filas.size() - vencidos);
	}

	private static boolean perteneceALocalidad(VehiculoDetalle vehiculo, Long localidadId) {
		return localidadId == null || (vehiculo != null && vehiculo.localidadId() == localidadId);
	}

	/**
	 * Un vencimiento necesita todos los datos de la programación. Si por algún motivo falta alguno, la fila se omite en
	 * lugar de hacer fallar todo el reporte.
	 */
	private static boolean tieneProgramacionCompleta(FilaSeguimiento fila) {
		return fila.proximoKm() != null && fila.kmRestantes() != null && fila.diasRestantes() != null
				&& fila.ultimoMantenimientoFecha() != null && fila.ultimoMantenimientoKm() != null;
	}

	private static boolean vencida(FilaSeguimiento fila) {
		return fila.vencidaPorKm() || fila.vencidaPorFecha();
	}

	private static boolean dentroDelHorizonte(FilaSeguimiento fila, FiltroVencimientos filtro) {
		boolean porDias = fila.diasRestantes() != null && fila.diasRestantes() <= filtro.horizonteDias();
		boolean porKm = fila.kmRestantes() != null && fila.kmRestantes() <= filtro.horizonteKm();
		return porDias || porKm;
	}

	private static FilaVencimiento aFila(FilaSeguimiento fila, LocalDate hoy) {
		long dias = fila.diasRestantes();
		return new FilaVencimiento(fila.vehiculoId(), fila.placa(), fila.codigoEquipo(), fila.localidad(),
				fila.ultimoMantenimientoFecha(), fila.ultimoMantenimientoKm(), fila.proximoKm(), fila.kmRestantes(), hoy.plusDays(dias),
				dias, vencida(fila));
	}
}
