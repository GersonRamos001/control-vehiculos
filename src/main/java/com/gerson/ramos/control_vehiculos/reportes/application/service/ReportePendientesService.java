package com.gerson.ramos.control_vehiculos.reportes.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.AlertaDetalle;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ConsultarAlertasUseCase;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarSeguimientoUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.EstadoPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FilaPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroPendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientes;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.ReportePendientesUseCase;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;

/** Vehículos con mantenimiento vencido o próximo: usa la misma clasificación (y umbrales) que las alertas. */
@Service
@Transactional(readOnly = true)
public class ReportePendientesService implements ReportePendientesUseCase {

	private final ConsultarAlertasUseCase alertas;
	private final ConsultarSeguimientoUseCase seguimiento;
	private final ConsultarVehiculosUseCase vehiculos;
	private final Clock reloj;

	ReportePendientesService(ConsultarAlertasUseCase alertas, ConsultarSeguimientoUseCase seguimiento,
			ConsultarVehiculosUseCase vehiculos, Clock reloj) {
		this.alertas = alertas;
		this.seguimiento = seguimiento;
		this.vehiculos = vehiculos;
		this.reloj = reloj;
	}

	@Override
	public ReportePendientes generar(FiltroPendientes filtro) {
		Validaciones.requerir(filtro, "El filtro");
		LocalDate hoy = LocalDate.now(reloj);

		Map<Long, FilaSeguimiento> detalleDeSeguimiento = new HashMap<>();
		for (FilaSeguimiento fila : seguimiento.seguimientoSemanal()) {
			detalleDeSeguimiento.put(fila.vehiculoId(), fila);
		}
		Map<Long, VehiculoDetalle> flota = new HashMap<>();
		for (VehiculoDetalle vehiculo : vehiculos.listar(FiltroEstado.ACTIVOS)) {
			flota.put(vehiculo.id(), vehiculo);
		}

		// Las alertas ya vienen ordenadas (vencidas primero, luego por placa) y hay a lo sumo una de mantenimiento por vehículo.
		List<FilaPendiente> filas = alertas.consultar().alertas().stream()
			.filter(alerta -> alerta.severidad() == Severidad.VENCIDA || alerta.severidad() == Severidad.PROXIMA)
			.filter(alerta -> incluye(filtro.estado(), alerta.severidad()))
			.map(alerta -> aFila(alerta, flota.get(alerta.vehiculoId()), detalleDeSeguimiento.get(alerta.vehiculoId()), hoy))
			.filter(Objects::nonNull)
			.filter(fila -> filtro.localidadId() == null || coincide(flota.get(fila.vehiculoId()), filtro.localidadId(), true))
			.filter(fila -> filtro.tipoUsoId() == null || coincide(flota.get(fila.vehiculoId()), filtro.tipoUsoId(), false))
			.toList();

		int vencidos = (int) filas.stream().filter(FilaPendiente::vencido).count();
		return new ReportePendientes(filas, vencidos, filas.size() - vencidos);
	}

	private static boolean incluye(EstadoPendiente estado, Severidad severidad) {
		return switch (estado) {
			case TODOS -> true;
			case VENCIDOS -> severidad == Severidad.VENCIDA;
			case PROXIMOS -> severidad == Severidad.PROXIMA;
		};
	}

	private static boolean coincide(VehiculoDetalle vehiculo, long id, boolean porLocalidad) {
		return vehiculo != null && (porLocalidad ? vehiculo.localidadId() : vehiculo.tipoUsoId()) == id;
	}

	private static FilaPendiente aFila(AlertaDetalle alerta, VehiculoDetalle vehiculo, FilaSeguimiento seguimiento, LocalDate hoy) {
		if (vehiculo == null || seguimiento == null) {
			return null;
		}
		Long dias = alerta.diasRestantes();
		return new FilaPendiente(alerta.vehiculoId(), alerta.placa(), alerta.codigoEquipo(), alerta.localidad(), vehiculo.tipoUso(),
				alerta.severidad(), alerta.mensaje(), seguimiento.ultimoMantenimientoFecha(), seguimiento.ultimoMantenimientoKm(),
				seguimiento.proximoKm(), seguimiento.kmActual(), alerta.kmRestantes(), dias, dias == null ? null : hoy.plusDays(dias));
	}
}
