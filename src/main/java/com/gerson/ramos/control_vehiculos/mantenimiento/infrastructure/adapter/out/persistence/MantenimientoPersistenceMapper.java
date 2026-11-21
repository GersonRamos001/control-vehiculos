package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.MantenimientoId;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Component
class MantenimientoPersistenceMapper {

	Mantenimiento aDominio(MantenimientoJpaEntity entidad) {
		return Mantenimiento.reconstruir(new MantenimientoId(entidad.getId()), new VehiculoId(entidad.getVehiculoId()),
				new RutinaMantenimientoId(entidad.getRutinaId()), entidad.getFecha(), entidad.getKilometraje(),
				entidad.getObservaciones());
	}

	MantenimientoJpaEntity aNuevaEntidad(Mantenimiento mantenimiento) {
		return new MantenimientoJpaEntity(mantenimiento.vehiculoId().valor(), mantenimiento.rutinaId().valor(),
				mantenimiento.fecha(), mantenimiento.kilometraje(), mantenimiento.observaciones());
	}
}
