package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarVehiculoComando;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

@Service
@Transactional
public class RegistrarVehiculoService implements RegistrarVehiculoUseCase {

	private final VehiculoRepositoryPort vehiculos;
	private final ReferenciasDeVehiculo referencias;

	RegistrarVehiculoService(VehiculoRepositoryPort vehiculos, ReferenciasDeVehiculo referencias) {
		this.vehiculos = vehiculos;
		this.referencias = referencias;
	}

	@Override
	public Vehiculo registrar(RegistrarVehiculoComando comando) {
		Placa placa = new Placa(comando.placa());
		CodigoEquipo codigoEquipo = new CodigoEquipo(comando.codigoEquipo());
		if (vehiculos.existePorPlaca(placa)) {
			throw new ConflictoDeDatosException("placa", "Ya existe un vehículo con la placa " + placa.valor());
		}
		if (vehiculos.existePorCodigoEquipo(codigoEquipo)) {
			throw new ConflictoDeDatosException("codigoEquipo",
					"Ya existe un vehículo con el código de equipo " + codigoEquipo.valor());
		}
		LocalidadId localidadId = referencias.localidad(comando.localidadId(), null);
		TipoUsoId tipoUsoId = referencias.tipoUso(comando.tipoUsoId(), null);
		RutinaMantenimientoId rutinaId = referencias.rutina(comando.rutinaId(), null);

		Vehiculo vehiculo = Vehiculo.registrar(placa, codigoEquipo, localidadId, tipoUsoId);
		if (rutinaId != null) {
			vehiculo.registrarRutinaRealizada(rutinaId);
		}
		return vehiculos.guardar(vehiculo);
	}
}
