package com.gerson.ramos.control_vehiculos.configuracion.application.port.in;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;

/** Consulta de los parámetros globales de mantenimiento (los usa el módulo de mantenimiento). */
public interface ConsultarParametrosMantenimientoUseCase {

	ParametrosMantenimiento obtener();
}
