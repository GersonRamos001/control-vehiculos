package com.gerson.ramos.control_vehiculos.vehiculo.application.service;

import com.gerson.ramos.control_vehiculos.configuracion.application.service.ConsultarCatalogosService;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryLocalidadRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryRutinaMantenimientoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryTipoUsoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryVehiculoRepository;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarVehiculoComando;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

/**
 * Escenario común de los tests de servicios: repositorios en memoria, servicios reales y catálogos sembrados
 * (una localidad, un tipo de uso y una rutina activos, y uno de cada uno inactivo).
 */
class EscenarioVehiculos {

	final InMemoryVehiculoRepository vehiculos = new InMemoryVehiculoRepository();
	final InMemoryLocalidadRepository localidades = new InMemoryLocalidadRepository();
	final InMemoryTipoUsoRepository tiposUso = new InMemoryTipoUsoRepository();
	final InMemoryRutinaMantenimientoRepository rutinas = new InMemoryRutinaMantenimientoRepository();

	final ConsultarCatalogosService catalogos = new ConsultarCatalogosService(localidades, tiposUso, rutinas);
	final ReferenciasDeVehiculo referencias = new ReferenciasDeVehiculo(catalogos);

	final RegistrarVehiculoService registrar = new RegistrarVehiculoService(vehiculos, referencias);
	final ActualizarVehiculoService actualizar = new ActualizarVehiculoService(vehiculos, referencias);
	final CambiarEstadoVehiculoService cambiarEstado = new CambiarEstadoVehiculoService(vehiculos);
	final ConsultarVehiculosService consultar = new ConsultarVehiculosService(vehiculos, catalogos);
	final RegistrarRutinaRealizadaService rutinaRealizada = new RegistrarRutinaRealizadaService(vehiculos, referencias);

	final Localidad localidadActiva;
	final Localidad localidadInactiva;
	final TipoUso tipoUsoActivo;
	final TipoUso tipoUsoInactivo;
	final RutinaMantenimiento rutinaActiva;
	final RutinaMantenimiento rutinaInactiva;

	EscenarioVehiculos() {
		localidadActiva = localidades.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "San Pedro Sula"));
		Localidad tgu = Localidad.crear(new CodigoCatalogo("TGU"), "Tegucigalpa");
		tgu.desactivar();
		localidadInactiva = localidades.guardar(tgu);

		tipoUsoActivo = tiposUso.guardar(TipoUso.crear(new CodigoCatalogo("OPERATIVO"), "Operativo"));
		TipoUso relevo = TipoUso.crear(new CodigoCatalogo("RELEVO"), "Relevo");
		relevo.desactivar();
		tipoUsoInactivo = tiposUso.guardar(relevo);

		rutinaActiva = rutinas.guardar(RutinaMantenimiento.crear(new CodigoCatalogo("10K"), "Preventivo 10K"));
		RutinaMantenimiento r20k = RutinaMantenimiento.crear(new CodigoCatalogo("20K"), "Preventivo 20K");
		r20k.desactivar();
		rutinaInactiva = rutinas.guardar(r20k);
	}

	long idLocalidadActiva() {
		return localidadActiva.id().valor();
	}

	long idTipoUsoActivo() {
		return tipoUsoActivo.id().valor();
	}

	long idRutinaActiva() {
		return rutinaActiva.id().valor();
	}

	/** Registra un vehículo válido (localidad y tipo de uso activos, sin rutina). */
	Vehiculo registrarVehiculo(String placa, String codigoEquipo) {
		return registrar.registrar(
				new RegistrarVehiculoComando(placa, codigoEquipo, idLocalidadActiva(), idTipoUsoActivo(), null));
	}
}
