package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

public interface ReporteRealizadosUseCase {

	/**
	 * @throws com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException si el rango es inválido (desde
	 * posterior a hasta, o hasta en el futuro)
	 */
	ReporteRealizados generar(FiltroRealizados filtro);
}
