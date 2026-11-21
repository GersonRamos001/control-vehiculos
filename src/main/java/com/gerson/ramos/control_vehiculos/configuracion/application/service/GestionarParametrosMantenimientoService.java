package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ActualizarParametrosMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.ParametrosMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;

@Service
@Transactional
public class GestionarParametrosMantenimientoService
		implements ConsultarParametrosMantenimientoUseCase, ActualizarParametrosMantenimientoUseCase {

	private final ParametrosMantenimientoRepositoryPort parametros;

	public GestionarParametrosMantenimientoService(ParametrosMantenimientoRepositoryPort parametros) {
		this.parametros = parametros;
	}

	@Override
	@Transactional(readOnly = true)
	public ParametrosMantenimiento obtener() {
		return parametros.obtener();
	}

	@Override
	public ParametrosMantenimiento actualizar(int intervaloKm, int intervaloMeses) {
		return parametros.guardar(new ParametrosMantenimiento(intervaloKm, intervaloMeses));
	}
}
