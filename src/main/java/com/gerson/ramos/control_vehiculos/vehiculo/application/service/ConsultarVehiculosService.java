package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Service
@Transactional(readOnly = true)
public class ConsultarVehiculosService implements ConsultarVehiculosUseCase {

	private final VehiculoRepositoryPort vehiculos;
	private final ConsultarCatalogosUseCase catalogos;

	ConsultarVehiculosService(VehiculoRepositoryPort vehiculos, ConsultarCatalogosUseCase catalogos) {
		this.vehiculos = vehiculos;
		this.catalogos = catalogos;
	}

	@Override
	public List<VehiculoDetalle> listar(FiltroEstado filtro) {
		List<Vehiculo> lista = filtro == FiltroEstado.ACTIVOS ? vehiculos.listarActivos() : vehiculos.listarTodos();

		// Los catálogos son pequeños: se leen una vez y se resuelven en memoria (sin N+1).
		Map<LocalidadId, String> localidades = new HashMap<>();
		for (Localidad localidad : catalogos.listarLocalidades(false)) {
			localidades.put(localidad.id(), localidad.nombre());
		}
		Map<TipoUsoId, String> tiposUso = new HashMap<>();
		for (TipoUso tipoUso : catalogos.listarTiposUso(false)) {
			tiposUso.put(tipoUso.id(), tipoUso.nombre());
		}
		Map<RutinaMantenimientoId, String> rutinas = new HashMap<>();
		for (RutinaMantenimiento rutina : catalogos.listarRutinas(false)) {
			rutinas.put(rutina.id(), rutina.codigo().valor());
		}

		return lista.stream().map(v -> aDetalle(v, localidades, tiposUso, rutinas)).toList();
	}

	@Override
	public Vehiculo buscarPorId(long vehiculoId) {
		return vehiculos.buscarPorId(new VehiculoId(vehiculoId))
			.orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", vehiculoId));
	}

	private static VehiculoDetalle aDetalle(Vehiculo vehiculo, Map<LocalidadId, String> localidades,
			Map<TipoUsoId, String> tiposUso, Map<RutinaMantenimientoId, String> rutinas) {
		RutinaMantenimientoId rutinaId = vehiculo.ultimaRutinaId();
		@Nullable Long idRutina = rutinaId == null ? null : rutinaId.valor();
		@Nullable String nombreRutina = rutinaId == null ? null : rutinas.get(rutinaId);
		return new VehiculoDetalle(vehiculo.id().valor(), vehiculo.placa().valor(), vehiculo.codigoEquipo().valor(),
				vehiculo.localidadId().valor(), localidades.getOrDefault(vehiculo.localidadId(), "?"),
				vehiculo.tipoUsoId().valor(), tiposUso.getOrDefault(vehiculo.tipoUsoId(), "?"), idRutina, nombreRutina,
				vehiculo.activo(), vehiculo.version());
	}
}
