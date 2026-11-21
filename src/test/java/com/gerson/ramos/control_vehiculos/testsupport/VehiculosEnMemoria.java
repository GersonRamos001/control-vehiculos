package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.List;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarRutinaRealizadaUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/**
 * Puertos de entrada del módulo de vehículos simplificados sobre repositorios en memoria. Permite probar el módulo de
 * mantenimiento sin instanciar los servicios internos de vehículos.
 */
public class VehiculosEnMemoria implements ConsultarVehiculosUseCase, RegistrarRutinaRealizadaUseCase {

	private final InMemoryVehiculoRepository vehiculos;
	private final InMemoryLocalidadRepository localidades;

	public VehiculosEnMemoria(InMemoryVehiculoRepository vehiculos, InMemoryLocalidadRepository localidades) {
		this.vehiculos = vehiculos;
		this.localidades = localidades;
	}

	@Override
	public List<VehiculoDetalle> listar(FiltroEstado filtro) {
		List<Vehiculo> lista = filtro == FiltroEstado.ACTIVOS ? vehiculos.listarActivos() : vehiculos.listarTodos();
		return lista.stream()
			.map(v -> new VehiculoDetalle(v.id().valor(), v.placa().valor(), v.codigoEquipo().valor(),
					v.localidadId().valor(), localidades.buscarPorId(v.localidadId()).map(Localidad::nombre).orElse("?"),
					v.tipoUsoId().valor(), "Operativo", null, null, v.activo(), v.version()))
			.toList();
	}

	@Override
	public Vehiculo buscarPorId(long vehiculoId) {
		return vehiculos.buscarPorId(new VehiculoId(vehiculoId))
			.orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", vehiculoId));
	}

	@Override
	public Vehiculo registrarRutinaRealizada(long vehiculoId, long rutinaId) {
		Vehiculo vehiculo = buscarPorId(vehiculoId);
		vehiculo.registrarRutinaRealizada(new RutinaMantenimientoId(rutinaId));
		return vehiculos.guardar(vehiculo);
	}
}
