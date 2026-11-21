/*
 * Seguimiento semanal de kilometraje: recálculo en vivo mientras se escribe (como el Excel que reemplaza).
 * Es solo una ayuda visual; la validación autoritativa la hace el servidor.
 *
 * Cada <tr data-vehiculo> trae en atributos data-*:
 *   data-ultima-lectura  km de la última lectura (ausente si no hay)
 *   data-km-base         mayor entre la última lectura y el último mantenimiento (ausente si no hay ninguno)
 *   data-proximo-km      km del próximo mantenimiento (ausente si el vehículo no tiene mantenimientos)
 * y una celda .km-restantes con data-texto-original / data-vencido-original (valor mostrado sin lectura nueva).
 */
(function () {
	'use strict';

	// Aviso suave (no bloquea): un salto mayor a este número de km desde la última lectura suele ser un error de dedo.
	var SALTO_SOSPECHOSO_KM = 5000;

	var formulario = document.getElementById('captura');
	var resumen = document.getElementById('resumen-captura');
	if (!formulario) {
		return;
	}
	var filas = formulario.querySelectorAll('tr[data-vehiculo]');

	function numeroDeAtributo(valor) {
		if (valor === undefined || valor === '') {
			return null;
		}
		var numero = Number(valor);
		return Number.isFinite(numero) ? numero : null;
	}

	function formato(numero) {
		return numero.toLocaleString('en-US');
	}

	function restaurarRestantes(celda) {
		celda.textContent = celda.dataset.textoOriginal || '—';
		celda.classList.toggle('vencido', celda.dataset.vencidoOriginal === 'true');
	}

	/** Recalcula una fila y devuelve {datos, bloqueante, sospechoso} para el resumen. */
	function recalcular(fila) {
		var entrada = fila.querySelector('.lectura-nueva');
		var celdaRecorridos = fila.querySelector('.recorridos');
		var celdaRestantes = fila.querySelector('.km-restantes');
		var aviso = fila.querySelector('.aviso-fila');
		var texto = entrada.value.trim();

		entrada.classList.remove('aviso');
		aviso.textContent = '';

		if (texto === '') {
			celdaRecorridos.textContent = '—';
			restaurarRestantes(celdaRestantes);
			return { datos: false, bloqueante: false, sospechoso: false };
		}

		var nuevo = Number(texto);
		if (!Number.isInteger(nuevo) || nuevo < 0) {
			celdaRecorridos.textContent = '—';
			restaurarRestantes(celdaRestantes);
			entrada.classList.add('aviso');
			aviso.textContent = 'Escribe un número entero válido';
			return { datos: true, bloqueante: true, sospechoso: false };
		}

		var ultima = numeroDeAtributo(fila.dataset.ultimaLectura);
		var base = numeroDeAtributo(fila.dataset.kmBase);
		var proximo = numeroDeAtributo(fila.dataset.proximoKm);

		var recorridos = ultima === null ? null : nuevo - ultima;
		celdaRecorridos.textContent = recorridos === null ? '—' : formato(recorridos) + ' km';

		if (proximo === null) {
			restaurarRestantes(celdaRestantes);
		} else {
			var restantes = proximo - nuevo;
			celdaRestantes.textContent = formato(restantes) + ' km';
			celdaRestantes.classList.toggle('vencido', restantes < 0);
		}

		if (base !== null && nuevo < base) {
			entrada.classList.add('aviso');
			aviso.textContent = 'Menor que el último km conocido (' + formato(base) + ')';
			return { datos: true, bloqueante: true, sospechoso: false };
		}
		if (recorridos !== null && recorridos > SALTO_SOSPECHOSO_KM) {
			entrada.classList.add('aviso');
			aviso.textContent = 'Revisa: salto de ' + formato(recorridos) + ' km';
			return { datos: true, bloqueante: false, sospechoso: true };
		}
		return { datos: true, bloqueante: false, sospechoso: false };
	}

	function recalcularTodo() {
		var datos = 0;
		var bloqueantes = 0;
		var sospechosos = 0;
		filas.forEach(function (fila) {
			var resultado = recalcular(fila);
			datos += resultado.datos ? 1 : 0;
			bloqueantes += resultado.bloqueante ? 1 : 0;
			sospechosos += resultado.sospechoso ? 1 : 0;
		});
		var texto = datos + (datos === 1 ? ' fila con datos' : ' filas con datos');
		if (bloqueantes > 0) {
			texto += ' · ' + bloqueantes + (bloqueantes === 1 ? ' con error' : ' con errores');
		}
		if (sospechosos > 0) {
			texto += ' · ' + sospechosos + (sospechosos === 1 ? ' por revisar' : ' por revisar');
		}
		resumen.textContent = texto;
		resumen.classList.toggle('vencido', bloqueantes > 0);
		return { bloqueantes: bloqueantes, sospechosos: sospechosos };
	}

	formulario.addEventListener('input', function (evento) {
		if (evento.target.classList && evento.target.classList.contains('lectura-nueva')) {
			recalcularTodo();
		}
	});

	// Como en una hoja de cálculo: Enter pasa a la lectura de la fila siguiente en lugar de enviar el formulario.
	formulario.addEventListener('keydown', function (evento) {
		if (evento.key !== 'Enter' || !evento.target.classList || !evento.target.classList.contains('lectura-nueva')) {
			return;
		}
		evento.preventDefault();
		var entradas = Array.prototype.slice.call(formulario.querySelectorAll('.lectura-nueva'));
		var siguiente = entradas[entradas.indexOf(evento.target) + 1];
		if (siguiente) {
			siguiente.focus();
			siguiente.select();
		}
	});

	formulario.addEventListener('submit', function (evento) {
		var estado = recalcularTodo();
		if (estado.bloqueantes === 0 && estado.sospechosos === 0) {
			return;
		}
		var mensaje = 'Hay filas con avisos (' + estado.bloqueantes + ' con error, ' + estado.sospechosos
			+ ' por revisar). El servidor rechazará las que tengan error. ¿Guardar de todos modos?';
		if (!window.confirm(mensaje)) {
			evento.preventDefault();
		}
	});

	recalcularTodo();
})();
