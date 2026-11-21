package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

public interface ReporteVencimientosUseCase {

	/**
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException si algún horizonte no es positivo
	 */
	ReporteVencimientos generar(FiltroVencimientos filtro);
}
