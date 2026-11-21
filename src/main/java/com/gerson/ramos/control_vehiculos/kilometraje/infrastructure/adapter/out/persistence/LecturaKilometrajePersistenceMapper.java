package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaId;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Component
class LecturaKilometrajePersistenceMapper {

	LecturaKilometraje aDominio(LecturaKilometrajeJpaEntity entidad) {
		return LecturaKilometraje.reconstruir(new LecturaId(entidad.getId()), new VehiculoId(entidad.getVehiculoId()),
				entidad.getFecha(), entidad.getKilometraje());
	}

	LecturaKilometrajeJpaEntity aNuevaEntidad(LecturaKilometraje lectura) {
		return new LecturaKilometrajeJpaEntity(lectura.vehiculoId().valor(), lectura.fecha(), lectura.kilometraje());
	}
}
