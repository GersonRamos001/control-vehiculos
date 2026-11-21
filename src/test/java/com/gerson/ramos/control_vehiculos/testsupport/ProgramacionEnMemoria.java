package com.gerson.ramos.control_vehiculos.testsupport;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarProgramacionUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.FilaProgramacion;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;

/**
 * Puerto de programación de mantenimiento simulado: los tests fijan el último mantenimiento y los resultados de la
 * programación de cada vehículo; los demás (sin mantenimiento) salen "sin registro".
 */
public class ProgramacionEnMemoria implements ConsultarProgramacionUseCase {

	private record UltimoMantenimiento(LocalDate fecha, int kilometraje, long proximoKm, long diasRestantes) {
	}

	private final VehiculosEnMemoria vehiculos;
	private final Map<Long, UltimoMantenimiento> ultimos = new HashMap<>();

	public ProgramacionEnMemoria(VehiculosEnMemoria vehiculos) {
		this.vehiculos = vehiculos;
	}

	/** Fija el último mantenimiento del vehículo y la programación que de él resulta. */
	public void establecer(long vehiculoId, LocalDate fecha, int kilometraje, long proximoKm, long diasRestantes) {
		ultimos.put(vehiculoId, new UltimoMantenimiento(fecha, kilometraje, proximoKm, diasRestantes));
	}

	@Override
	public List<FilaProgramacion> programacionDeVehiculosActivos() {
		return vehiculos.listar(FiltroEstado.ACTIVOS).stream().map(this::fila).toList();
	}

	@Override
	public FilaProgramacion programacionDe(long vehiculoId) {
		return vehiculos.listar(FiltroEstado.TODOS).stream()
			.filter(v -> v.id() == vehiculoId)
			.findFirst()
			.map(this::fila)
			.orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", vehiculoId));
	}

	private FilaProgramacion fila(VehiculoDetalle vehiculo) {
		UltimoMantenimiento ultimo = ultimos.get(vehiculo.id());
		if (ultimo == null) {
			return new FilaProgramacion(vehiculo.id(), vehiculo.placa(), vehiculo.codigoEquipo(), vehiculo.localidad(), null,
					null, null, null, null, null);
		}
		return new FilaProgramacion(vehiculo.id(), vehiculo.placa(), vehiculo.codigoEquipo(), vehiculo.localidad(),
				ultimo.fecha(), ultimo.kilometraje(), "10K", ultimo.proximoKm(), ultimo.fecha().plusMonths(6),
				ultimo.diasRestantes());
	}
}
