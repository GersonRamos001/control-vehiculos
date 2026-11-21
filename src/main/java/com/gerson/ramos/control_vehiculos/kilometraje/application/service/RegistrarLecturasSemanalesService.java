package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.LecturaPorVehiculo;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaComando;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturasSemanalesUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoLote;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoRegistro;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

/**
 * Captura semanal. Deliberadamente <b>sin</b> {@code @Transactional}: cada fila se registra con su propia transacción
 * (a través del caso de uso individual), así una fila inválida no deshace las demás.
 */
@Service
public class RegistrarLecturasSemanalesService implements RegistrarLecturasSemanalesUseCase {

	private final RegistrarLecturaUseCase registrarLectura;

	RegistrarLecturasSemanalesService(RegistrarLecturaUseCase registrarLectura) {
		this.registrarLectura = registrarLectura;
	}

	@Override
	public ResultadoLote registrar(LocalDate fecha, List<LecturaPorVehiculo> lecturas) {
		int nuevas = 0;
		int reemplazadas = 0;
		int sinCambios = 0;
		Map<Long, String> errores = new LinkedHashMap<>();

		for (LecturaPorVehiculo fila : lecturas) {
			try {
				ResultadoRegistro resultado = registrarLectura
					.registrar(new RegistrarLecturaComando(fila.vehiculoId(), fecha, fila.kilometraje()));
				switch (resultado.tipo()) {
					case NUEVA -> nuevas++;
					case REEMPLAZADA -> reemplazadas++;
					case SIN_CAMBIOS -> sinCambios++;
				}
			}
			catch (ReglaDeNegocioException excepcion) {
				errores.put(fila.vehiculoId(), excepcion.getMessage());
			}
			catch (DataIntegrityViolationException excepcion) {
				errores.put(fila.vehiculoId(), "No se pudo guardar la lectura; vuelve a intentarlo.");
			}
		}
		return new ResultadoLote(nuevas, reemplazadas, sinCambios, errores);
	}
}
