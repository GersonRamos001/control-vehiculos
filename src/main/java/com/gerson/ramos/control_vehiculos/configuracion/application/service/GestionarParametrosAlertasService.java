package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ActualizarParametrosAlertasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosAlertasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.ParametrosAlertasRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

@Service
@Transactional
public class GestionarParametrosAlertasService implements ConsultarParametrosAlertasUseCase, ActualizarParametrosAlertasUseCase {

	private final ParametrosAlertasRepositoryPort parametros;

	public GestionarParametrosAlertasService(ParametrosAlertasRepositoryPort parametros) {
		this.parametros = parametros;
	}

	@Override
	@Transactional(readOnly = true)
	public ParametrosAlertas obtener() {
		return parametros.obtener();
	}

	@Override
	public ParametrosAlertas actualizar(int umbralKm, int umbralDias, int diasLecturaPendiente) {
		return parametros.guardar(new ParametrosAlertas(umbralKm, umbralDias, diasLecturaPendiente));
	}
}
