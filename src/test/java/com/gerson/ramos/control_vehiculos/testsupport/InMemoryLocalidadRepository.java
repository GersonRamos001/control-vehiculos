package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.LocalidadRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;

/** Implementación en memoria del puerto, para probar servicios sin Spring ni base de datos. Devuelve copias. */
public class InMemoryLocalidadRepository implements LocalidadRepositoryPort {

	private final Map<Long, Localidad> datos = new LinkedHashMap<>();
	private long secuencia = 0;

	@Override
	public Localidad guardar(Localidad localidad) {
		long id = localidad.id() == null ? ++secuencia : localidad.id().valor();
		Localidad guardada = Localidad.reconstruir(new LocalidadId(id), localidad.codigo(), localidad.nombre(), localidad.activo());
		datos.put(id, guardada);
		return copia(guardada);
	}

	@Override
	public Optional<Localidad> buscarPorId(LocalidadId id) {
		return Optional.ofNullable(datos.get(id.valor())).map(InMemoryLocalidadRepository::copia);
	}

	@Override
	public Optional<Localidad> buscarPorCodigo(CodigoCatalogo codigo) {
		return datos.values().stream().filter(l -> l.codigo().equals(codigo)).findFirst().map(InMemoryLocalidadRepository::copia);
	}

	@Override
	public boolean existePorCodigo(CodigoCatalogo codigo) {
		return datos.values().stream().anyMatch(l -> l.codigo().equals(codigo));
	}

	@Override
	public List<Localidad> listarActivas() {
		return datos.values().stream().filter(Localidad::activo).sorted(Comparator.comparing(Localidad::nombre))
			.map(InMemoryLocalidadRepository::copia).toList();
	}

	@Override
	public List<Localidad> listarTodas() {
		return datos.values().stream().sorted(Comparator.comparing(Localidad::nombre))
			.map(InMemoryLocalidadRepository::copia).toList();
	}

	private static Localidad copia(Localidad l) {
		return Localidad.reconstruir(l.id(), l.codigo(), l.nombre(), l.activo());
	}
}
