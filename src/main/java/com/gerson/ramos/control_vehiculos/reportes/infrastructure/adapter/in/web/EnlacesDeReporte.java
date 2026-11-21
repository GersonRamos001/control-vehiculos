package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import java.util.Optional;

import org.springframework.web.util.UriComponentsBuilder;

import com.gerson.ramos.control_vehiculos.reportes.domain.model.RangoDeFechas;

/**
 * Arma los enlaces de descarga de cada reporte con los mismos filtros que tiene la pantalla. {@code formato} es
 * {@code csv} o {@code xlsx}. Los valores se codifican (la placa, por ejemplo, puede traer espacios).
 */
final class EnlacesDeReporte {

	private EnlacesDeReporte() {
	}

	static String pendientes(String formato, FiltroPendientesForm filtro) {
		return base("pendientes", formato)
			.queryParam("estado", filtro.getEstado())
			.queryParamIfPresent("localidadId", Optional.ofNullable(filtro.getLocalidadId()))
			.queryParamIfPresent("tipoUsoId", Optional.ofNullable(filtro.getTipoUsoId()))
			.build().encode().toUriString();
	}

	/** Usa el período efectivo (con los valores por defecto ya aplicados) para que la descarga coincida con la pantalla. */
	static String realizados(String formato, FiltroRealizadosForm filtro, RangoDeFechas rango) {
		return base("realizados", formato)
			.queryParam("desde", rango.desde())
			.queryParam("hasta", rango.hasta())
			.queryParamIfPresent("localidadId", Optional.ofNullable(filtro.getLocalidadId()))
			.queryParamIfPresent("rutinaId", Optional.ofNullable(filtro.getRutinaId()))
			.queryParamIfPresent("placa", Optional.ofNullable(filtro.getPlaca()).filter(placa -> !placa.isBlank()))
			.build().encode().toUriString();
	}

	static String vencimientos(String formato, FiltroVencimientosForm filtro) {
		return base("vencimientos", formato)
			.queryParam("horizonteDias", filtro.getHorizonteDias())
			.queryParam("horizonteKm", filtro.getHorizonteKm())
			.queryParam("vencidos", filtro.getVencidos())
			.queryParamIfPresent("localidadId", Optional.ofNullable(filtro.getLocalidadId()))
			.build().encode().toUriString();
	}

	private static UriComponentsBuilder base(String reporte, String formato) {
		return UriComponentsBuilder.fromPath("/reportes/" + reporte + "/" + formato);
	}
}
