package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaComando;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoRegistro;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.out.LecturaKilometrajeRepositoryPort;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarProgramacionUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.FilaProgramacion;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

@Service
@Transactional
public class RegistrarLecturaService implements RegistrarLecturaUseCase {

	private final LecturaKilometrajeRepositoryPort lecturas;
	private final ConsultarVehiculosUseCase vehiculos;
	private final ConsultarProgramacionUseCase programacion;
	private final Clock reloj;

	RegistrarLecturaService(LecturaKilometrajeRepositoryPort lecturas, ConsultarVehiculosUseCase vehiculos,
			ConsultarProgramacionUseCase programacion, Clock reloj) {
		this.lecturas = lecturas;
		this.vehiculos = vehiculos;
		this.programacion = programacion;
		this.reloj = reloj;
	}

	@Override
	public ResultadoRegistro registrar(RegistrarLecturaComando comando) {
		Vehiculo vehiculo = vehiculos.buscarPorId(comando.vehiculoId());
		if (!vehiculo.activo()) {
			throw new ReglaDeNegocioException(
					"El vehículo " + vehiculo.placa().valor() + " está inactivo: actívalo para registrarle lecturas");
		}

		LecturaKilometraje nueva = LecturaKilometraje.registrar(vehiculo.id(), comando.fecha(), comando.kilometraje(),
				LocalDate.now(reloj));
		List<LecturaKilometraje> historial = lecturas.historialDe(vehiculo.id());
		nueva.verificarCoherenciaDeOdometro(historial);

		FilaProgramacion mantenimiento = programacion.programacionDe(comando.vehiculoId());
		LocalDate fechaMantenimiento = mantenimiento.ultimaFecha();
		Integer kmMantenimiento = mantenimiento.ultimoKilometraje();
		if (fechaMantenimiento != null && kmMantenimiento != null) {
			nueva.verificarContraMantenimiento(fechaMantenimiento, kmMantenimiento);
		}

		Optional<LecturaKilometraje> existente = lecturas.buscarPorVehiculoYFecha(vehiculo.id(), comando.fecha());
		if (existente.isEmpty()) {
			return new ResultadoRegistro(lecturas.guardar(nueva), ResultadoRegistro.Tipo.NUEVA);
		}
		LecturaKilometraje actual = existente.get();
		if (actual.kilometraje() == comando.kilometraje()) {
			return new ResultadoRegistro(actual, ResultadoRegistro.Tipo.SIN_CAMBIOS);
		}
		return new ResultadoRegistro(lecturas.guardar(actual.conKilometraje(comando.kilometraje())),
				ResultadoRegistro.Tipo.REEMPLAZADA);
	}
}
