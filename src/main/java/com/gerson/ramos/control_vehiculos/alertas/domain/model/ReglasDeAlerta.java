package com.gerson.ramos.control_vehiculos.alertas.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/**
 * Reglas que deciden qué alertas tiene un vehículo.
 *
 * <ul>
 * <li><b>Vencido</b>: faltan menos de 0 km o menos de 0 días (por km, por fecha o ambos).</li>
 * <li><b>Próximo</b> (si no está vencido): faltan {@code umbralKm} km o menos, o {@code umbralDias} días o menos
 * (los límites exactos cuentan como próximo).</li>
 * <li><b>Sin mantenimiento</b>: no hay programación posible; en ese caso no hay vencido ni próximo.</li>
 * <li><b>Lectura pendiente</b>: puede acompañar a cualquiera de las anteriores.</li>
 * </ul>
 */
public final class ReglasDeAlerta {

	private ReglasDeAlerta() {
	}

	public static List<Alerta> evaluar(SituacionDeMantenimiento situacion, ParametrosAlertas umbrales) {
		Validaciones.requerir(situacion, "La situación del vehículo");
		Validaciones.requerir(umbrales, "Los umbrales de alerta");

		List<Alerta> alertas = new ArrayList<>(2);
		if (situacion.sinProgramacion()) {
			alertas.add(new Alerta(TipoAlerta.SIN_MANTENIMIENTO, false, false));
		}
		else {
			Long km = situacion.kmRestantes();
			Long dias = situacion.diasRestantes();

			boolean vencidoPorKm = km != null && km < 0;
			boolean vencidoPorFecha = dias != null && dias < 0;
			if (vencidoPorKm || vencidoPorFecha) {
				alertas.add(new Alerta(TipoAlerta.MANTENIMIENTO_VENCIDO, vencidoPorKm, vencidoPorFecha));
			}
			else {
				boolean proximoPorKm = km != null && km <= umbrales.umbralKm();
				boolean proximoPorFecha = dias != null && dias <= umbrales.umbralDias();
				if (proximoPorKm || proximoPorFecha) {
					alertas.add(new Alerta(TipoAlerta.MANTENIMIENTO_PROXIMO, proximoPorKm, proximoPorFecha));
				}
			}
		}
		if (situacion.lecturaPendiente()) {
			alertas.add(new Alerta(TipoAlerta.LECTURA_PENDIENTE, false, false));
		}
		return List.copyOf(alertas);
	}
}
