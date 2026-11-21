package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.TipoUsoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;

/** Implementación en memoria del puerto, para probar servicios sin Spring ni base de datos. Devuelve copias. */
public class InMemoryTipoUsoRepository implements TipoUsoRepositoryPort {

	private final Map<Long, TipoUso> datos = new LinkedHashMap<>();
	private long secuencia = 0;

	@Override
	public TipoUso guardar(TipoUso tipoUso) {
		long id = tipoUso.id() == null ? ++secuencia : tipoUso.id().valor();
		TipoUso guardado = TipoUso.reconstruir(new TipoUsoId(id), tipoUso.codigo(), tipoUso.nombre(), tipoUso.activo());
		datos.put(id, guardado);
		return copia(guardado);
	}

	@Override
	public Optional<TipoUso> buscarPorId(TipoUsoId id) {
		return Optional.ofNullable(datos.get(id.valor())).map(InMemoryTipoUsoRepository::copia);
	}

	@Override
	public Optional<TipoUso> buscarPorCodigo(CodigoCatalogo codigo) {
		return datos.values().stream().filter(t -> t.codigo().equals(codigo)).findFirst().map(InMemoryTipoUsoRepository::copia);
	}

	@Override
	public boolean existePorCodigo(CodigoCatalogo codigo) {
		return datos.values().stream().anyMatch(t -> t.codigo().equals(codigo));
	}

	@Override
	public List<TipoUso> listarActivos() {
		return datos.values().stream().filter(TipoUso::activo).sorted(Comparator.comparing(TipoUso::nombre))
			.map(InMemoryTipoUsoRepository::copia).toList();
	}

	@Override
	public List<TipoUso> listarTodos() {
		return datos.values().stream().sorted(Comparator.comparing(TipoUso::nombre))
			.map(InMemoryTipoUsoRepository::copia).toList();
	}

	private static TipoUso copia(TipoUso t) {
		return TipoUso.reconstruir(t.id(), t.codigo(), t.nombre(), t.activo());
	}
}
