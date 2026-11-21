package com.gerson.ramos.control_vehiculos.mantenimiento.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoComando;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.out.MantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarRutinaRealizadaUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

@Service
@Transactional
public class RegistrarMantenimientoService implements RegistrarMantenimientoUseCase {

	private final MantenimientoRepositoryPort mantenimientos;
	private final ConsultarVehiculosUseCase vehiculos;
	private final RegistrarRutinaRealizadaUseCase rutinaRealizada;
	private final ConsultarCatalogosUseCase catalogos;
	private final Clock reloj;

	RegistrarMantenimientoService(MantenimientoRepositoryPort mantenimientos, ConsultarVehiculosUseCase vehiculos,
			RegistrarRutinaRealizadaUseCase rutinaRealizada, ConsultarCatalogosUseCase catalogos, Clock reloj) {
		this.mantenimientos = mantenimientos;
		this.vehiculos = vehiculos;
		this.rutinaRealizada = rutinaRealizada;
		this.catalogos = catalogos;
		this.reloj = reloj;
	}

	@Override
	public Mantenimiento registrar(RegistrarMantenimientoComando comando) {
		Vehiculo vehiculo = vehiculos.buscarPorId(comando.vehiculoId());
		if (!vehiculo.activo()) {
			throw new ReglaDeNegocioException(
					"El vehículo " + vehiculo.placa().valor() + " está inactivo: actívalo para registrarle mantenimientos");
		}
		RutinaMantenimientoId rutinaId = new RutinaMantenimientoId(comando.rutinaId());
		boolean rutinaActiva = catalogos.buscarRutina(rutinaId).map(RutinaMantenimiento::activo).orElse(false);
		if (!rutinaActiva) {
			throw new ConflictoDeDatosException("rutinaId", "La rutina seleccionada no existe o está inactiva");
		}

		Mantenimiento nuevo = Mantenimiento.registrar(vehiculo.id(), rutinaId, comando.fecha(), comando.kilometraje(),
				comando.observaciones(), LocalDate.now(reloj));
		List<Mantenimiento> historial = mantenimientos.historialDe(vehiculo.id());
		nuevo.verificarCoherenciaDeOdometro(historial);

		Mantenimiento guardado = mantenimientos.guardar(nuevo);

		boolean esElMasReciente = historial.stream().noneMatch(anterior -> anterior.esMasRecienteQue(guardado));
		if (esElMasReciente) {
			rutinaRealizada.registrarRutinaRealizada(comando.vehiculoId(), comando.rutinaId());
		}
		return guardado;
	}
}
