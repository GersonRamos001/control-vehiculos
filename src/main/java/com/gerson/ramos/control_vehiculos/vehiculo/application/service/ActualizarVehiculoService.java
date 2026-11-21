package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeConcurrenciaException;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ActualizarVehiculoComando;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ActualizarVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Service
@Transactional
public class ActualizarVehiculoService implements ActualizarVehiculoUseCase {

	private final VehiculoRepositoryPort vehiculos;
	private final ReferenciasDeVehiculo referencias;

	ActualizarVehiculoService(VehiculoRepositoryPort vehiculos, ReferenciasDeVehiculo referencias) {
		this.vehiculos = vehiculos;
		this.referencias = referencias;
	}

	@Override
	public Vehiculo actualizar(ActualizarVehiculoComando comando) {
		Vehiculo vehiculo = vehiculos.buscarPorId(new VehiculoId(comando.id()))
			.orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", comando.id()));
		if (!Objects.equals(vehiculo.version(), comando.version())) {
			throw new ConflictoDeConcurrenciaException("El vehículo " + vehiculo.placa().valor());
		}

		Placa placa = new Placa(comando.placa());
		CodigoEquipo codigoEquipo = new CodigoEquipo(comando.codigoEquipo());
		if (!placa.equals(vehiculo.placa()) && vehiculos.existePorPlaca(placa)) {
			throw new ConflictoDeDatosException("placa", "Ya existe un vehículo con la placa " + placa.valor());
		}
		if (!codigoEquipo.equals(vehiculo.codigoEquipo()) && vehiculos.existePorCodigoEquipo(codigoEquipo)) {
			throw new ConflictoDeDatosException("codigoEquipo",
					"Ya existe un vehículo con el código de equipo " + codigoEquipo.valor());
		}
		LocalidadId localidadId = referencias.localidad(comando.localidadId(), vehiculo.localidadId());
		TipoUsoId tipoUsoId = referencias.tipoUso(comando.tipoUsoId(), vehiculo.tipoUsoId());
		RutinaMantenimientoId rutinaId = referencias.rutina(comando.rutinaId(), vehiculo.ultimaRutinaId());

		// Cada operación del dominio rechaza la modificación si el vehículo está inactivo.
		vehiculo.corregirIdentificacion(placa, codigoEquipo);
		vehiculo.reasignarLocalidad(localidadId);
		vehiculo.cambiarTipoUso(tipoUsoId);
		if (rutinaId != null) {
			vehiculo.registrarRutinaRealizada(rutinaId);
		}
		return vehiculos.guardar(vehiculo);
	}
}
