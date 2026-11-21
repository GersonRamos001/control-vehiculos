package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.ParametrosMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;

@Repository
@Transactional(readOnly = true)
public class ParametrosMantenimientoPersistenceAdapter implements ParametrosMantenimientoRepositoryPort {

	private final ParametrosMantenimientoJpaRepository repositorio;

	ParametrosMantenimientoPersistenceAdapter(ParametrosMantenimientoJpaRepository repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public ParametrosMantenimiento obtener() {
		return aDominio(cargar());
	}

	@Override
	@Transactional
	public ParametrosMantenimiento guardar(ParametrosMantenimiento parametros) {
		ParametrosMantenimientoJpaEntity entidad = cargar();
		entidad.actualizar(parametros.intervaloKm(), parametros.intervaloMeses());
		return aDominio(repositorio.saveAndFlush(entidad));
	}

	/** La fila única la crea la migración V3; si falta, la base de datos está mal migrada. */
	private ParametrosMantenimientoJpaEntity cargar() {
		return repositorio.findById(ParametrosMantenimientoJpaEntity.ID_UNICO).orElseThrow(
				() -> new IllegalStateException("Falta la fila de parametros_mantenimiento (id = 1): revisa las migraciones"));
	}

	private static ParametrosMantenimiento aDominio(ParametrosMantenimientoJpaEntity entidad) {
		return new ParametrosMantenimiento(entidad.getIntervaloKm(), entidad.getIntervaloMeses());
	}
}
