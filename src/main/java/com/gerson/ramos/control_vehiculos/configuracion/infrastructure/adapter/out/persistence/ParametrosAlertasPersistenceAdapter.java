package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.ParametrosAlertasRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

@Repository
@Transactional(readOnly = true)
public class ParametrosAlertasPersistenceAdapter implements ParametrosAlertasRepositoryPort {

	private final ParametrosAlertasJpaRepository repositorio;

	ParametrosAlertasPersistenceAdapter(ParametrosAlertasJpaRepository repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public ParametrosAlertas obtener() {
		return aDominio(cargar());
	}

	@Override
	@Transactional
	public ParametrosAlertas guardar(ParametrosAlertas parametros) {
		ParametrosAlertasJpaEntity entidad = cargar();
		entidad.actualizar(parametros.umbralKm(), parametros.umbralDias(), parametros.diasLecturaPendiente());
		return aDominio(repositorio.saveAndFlush(entidad));
	}

	/** La fila única la crea la migración V6; si falta, la base de datos está mal migrada. */
	private ParametrosAlertasJpaEntity cargar() {
		return repositorio.findById(ParametrosAlertasJpaEntity.ID_UNICO).orElseThrow(
				() -> new IllegalStateException("Falta la fila de parametros_alertas (id = 1): revisa las migraciones"));
	}

	private static ParametrosAlertas aDominio(ParametrosAlertasJpaEntity entidad) {
		return new ParametrosAlertas(entidad.getUmbralKm(), entidad.getUmbralDias(), entidad.getDiasLecturaPendiente());
	}
}
