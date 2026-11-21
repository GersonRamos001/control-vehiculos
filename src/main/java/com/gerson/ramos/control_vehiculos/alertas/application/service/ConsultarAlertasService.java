package com.gerson.ramos.control_vehiculos.alertas.application.service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.AlertaDetalle;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ConsultarAlertasUseCase;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.PanelDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ResumenDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.Alerta;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.ReglasDeAlerta;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.SituacionDeMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosAlertasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarSeguimientoUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;

@Service
@Transactional(readOnly = true)
public class ConsultarAlertasService implements ConsultarAlertasUseCase {

	private static final Comparator<AlertaDetalle> ORDEN = Comparator
		.comparing(AlertaDetalle::severidad)
		.thenComparing(AlertaDetalle::placa)
		.thenComparing(AlertaDetalle::tipo);

	private final ConsultarSeguimientoUseCase seguimiento;
	private final ConsultarParametrosAlertasUseCase parametros;

	ConsultarAlertasService(ConsultarSeguimientoUseCase seguimiento, ConsultarParametrosAlertasUseCase parametros) {
		this.seguimiento = seguimiento;
		this.parametros = parametros;
	}

	@Override
	public PanelDeAlertas consultar() {
		ParametrosAlertas umbrales = parametros.obtener();

		List<AlertaDetalle> alertas = new ArrayList<>();
		for (FilaSeguimiento fila : seguimiento.seguimientoSemanal()) {
			SituacionDeMantenimiento situacion = new SituacionDeMantenimiento(fila.kmRestantes(), fila.diasRestantes(),
					fila.sinProgramacion(), fila.pendiente());
			for (Alerta alerta : ReglasDeAlerta.evaluar(situacion, umbrales)) {
				alertas.add(detalle(fila, alerta));
			}
		}
		alertas.sort(ORDEN);
		return new PanelDeAlertas(List.copyOf(alertas), ResumenDeAlertas.de(alertas), umbrales);
	}

	private static AlertaDetalle detalle(FilaSeguimiento fila, Alerta alerta) {
		return new AlertaDetalle(fila.vehiculoId(), fila.placa(), fila.codigoEquipo(), fila.localidad(), alerta.tipo(),
				alerta.severidad(), mensaje(fila, alerta), fila.kmRestantes(), fila.diasRestantes());
	}

	private static String mensaje(FilaSeguimiento fila, Alerta alerta) {
		return switch (alerta.tipo()) {
			case MANTENIMIENTO_VENCIDO -> "Vencido: " + String.join(" y ", motivosDeVencimiento(fila, alerta));
			case MANTENIMIENTO_PROXIMO -> "Próximo: " + String.join(" · ", motivosDeProximidad(fila, alerta));
			case SIN_MANTENIMIENTO -> "Sin mantenimientos registrados: no se puede programar el siguiente";
			case LECTURA_PENDIENTE -> fila.sinLectura() ? "Sin lecturas de kilometraje"
					: "Última lectura hace " + dias(fila.diasSinLectura());
		};
	}

	private static List<String> motivosDeVencimiento(FilaSeguimiento fila, Alerta alerta) {
		List<String> motivos = new ArrayList<>(2);
		if (alerta.porKm() && fila.kmRestantes() != null) {
			motivos.add("se pasó por " + km(-fila.kmRestantes()));
		}
		if (alerta.porFecha() && fila.diasRestantes() != null) {
			motivos.add("venció hace " + dias(-fila.diasRestantes()));
		}
		return motivos;
	}

	private static List<String> motivosDeProximidad(FilaSeguimiento fila, Alerta alerta) {
		List<String> motivos = new ArrayList<>(2);
		if (alerta.porKm() && fila.kmRestantes() != null) {
			motivos.add("faltan " + km(fila.kmRestantes()));
		}
		if (alerta.porFecha() && fila.diasRestantes() != null) {
			long restantes = fila.diasRestantes();
			motivos.add(restantes == 0 ? "vence hoy" : "faltan " + dias(restantes));
		}
		return motivos;
	}

	private static String km(long kilometros) {
		return NumberFormat.getIntegerInstance(Locale.US).format(kilometros) + " km";
	}

	private static String dias(Long cantidad) {
		return cantidad == null ? "?" : cantidad + (cantidad == 1 ? " día" : " días");
	}
}
