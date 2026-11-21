package com.gerson.ramos.control_vehiculos.configuracion.application.port.out;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;

/** Puerto de salida: persistencia de los parámetros globales de mantenimiento (siempre existen, son una sola fila). */
public interface ParametrosMantenimientoRepositoryPort {

	ParametrosMantenimiento obtener();

	ParametrosMantenimiento guardar(ParametrosMantenimiento parametros);
}
