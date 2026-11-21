package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

public interface ReportePendientesUseCase {

	/**
	 * Vehículos activos con mantenimiento vencido o próximo (misma clasificación y umbrales que las alertas), vencidos
	 * primero y luego por placa.
	 */
	ReportePendientes generar(FiltroPendientes filtro);
}
