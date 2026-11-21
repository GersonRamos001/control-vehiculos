package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.RutinaMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;

/** Implementación en memoria del puerto, para probar servicios sin Spring ni base de datos. Devuelve copias. */
public class InMemoryRutinaMantenimientoRepository implements RutinaMantenimientoRepositoryPort {

	private final Map<Long, RutinaMantenimiento> datos = new LinkedHashMap<>();
	private long secuencia = 0;

	@Override
	public RutinaMantenimiento guardar(RutinaMantenimiento rutina) {
		long id = rutina.id() == null ? ++secuencia : rutina.id().valor();
		RutinaMantenimiento guardada = RutinaMantenimiento.reconstruir(new RutinaMantenimientoId(id), rutina.codigo(),
				rutina.nombre(), rutina.activo());
		datos.put(id, guardada);
		return copia(guardada);
	}

	@Override
	public Optional<RutinaMantenimiento> buscarPorId(RutinaMantenimientoId id) {
		return Optional.ofNullable(datos.get(id.valor())).map(InMemoryRutinaMantenimientoRepository::copia);
	}

	@Override
	public Optional<RutinaMantenimiento> buscarPorCodigo(CodigoCatalogo codigo) {
		return datos.values().stream().filter(r -> r.codigo().equals(codigo)).findFirst()
			.map(InMemoryRutinaMantenimientoRepository::copia);
	}

	@Override
	public boolean existePorCodigo(CodigoCatalogo codigo) {
		return datos.values().stream().anyMatch(r -> r.codigo().equals(codigo));
	}

	@Override
	public List<RutinaMantenimiento> listarActivas() {
		return datos.values().stream().filter(RutinaMantenimiento::activo).sorted(porCodigo())
			.map(InMemoryRutinaMantenimientoRepository::copia).toList();
	}

	@Override
	public List<RutinaMantenimiento> listarTodas() {
		return datos.values().stream().sorted(porCodigo()).map(InMemoryRutinaMantenimientoRepository::copia).toList();
	}

	private static Comparator<RutinaMantenimiento> porCodigo() {
		return Comparator.comparing(r -> r.codigo().valor());
	}

	private static RutinaMantenimiento copia(RutinaMantenimiento r) {
		return RutinaMantenimiento.reconstruir(r.id(), r.codigo(), r.nombre(), r.activo());
	}
}
