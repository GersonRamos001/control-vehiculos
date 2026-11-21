package com.gerson.ramos.control_vehiculos.vehiculo.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Component
class VehiculoPersistenceMapper {

	Vehiculo aDominio(VehiculoJpaEntity entidad) {
		Long ultimaRutina = entidad.getUltimaRutinaId();
		return Vehiculo.reconstruir(new VehiculoId(entidad.getId()), new Placa(entidad.getPlaca()),
				new CodigoEquipo(entidad.getCodigoEquipo()), new LocalidadId(entidad.getLocalidadId()),
				new TipoUsoId(entidad.getTipoUsoId()),
				ultimaRutina == null ? null : new RutinaMantenimientoId(ultimaRutina), entidad.isActivo(),
				entidad.getVersion());
	}

	VehiculoJpaEntity aNuevaEntidad(Vehiculo vehiculo) {
		return new VehiculoJpaEntity(vehiculo.placa().valor(), vehiculo.codigoEquipo().valor(),
				vehiculo.localidadId().valor(), vehiculo.tipoUsoId().valor(), idRutina(vehiculo), vehiculo.activo());
	}

	void actualizar(Vehiculo vehiculo, VehiculoJpaEntity entidad) {
		entidad.actualizar(vehiculo.placa().valor(), vehiculo.codigoEquipo().valor(), vehiculo.localidadId().valor(),
				vehiculo.tipoUsoId().valor(), idRutina(vehiculo), vehiculo.activo());
	}

	private static Long idRutina(Vehiculo vehiculo) {
		RutinaMantenimientoId rutina = vehiculo.ultimaRutinaId();
		return rutina == null ? null : rutina.valor();
	}
}
