/*
 * Tablas interactivas: ordenar por columna, buscar dentro de la tabla y contador "Mostrando X de Y".
 * Mejora progresiva: sin JavaScript la tabla se ve y funciona igual (solo que no se puede ordenar ni buscar).
 *
 * Convenciones de la plantilla:
 *   <table class="tabla-interactiva" id="mi-tabla">
 *     <thead><tr><th scope="col" data-tipo="texto|numero|fecha">Título</th> ...    (sin data-tipo: columna no ordenable)
 *     <tbody><tr><td data-valor="valor crudo para ordenar">texto mostrado</td> ...  (sin data-valor se usa el texto)
 *     <tfoot>... <span data-contador-visibles></span> ... <span data-contador-total></span> ...
 *   <input type="search" class="buscador-tabla" data-tabla="mi-tabla">
 */
(function () {
	'use strict';

	/** Quita acentos y pasa a minúsculas para comparar y buscar sin distinguir ni acentos ni mayúsculas. */
	function normalizar(texto) {
		return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
	}

	function valorDeCelda(fila, indice) {
		var celda = fila.cells[indice];
		if (!celda) {
			return '';
		}
		var crudo = celda.getAttribute('data-valor');
		return (crudo !== null ? crudo : celda.textContent).trim();
	}

	/** Compara dos valores según el tipo de columna; los vacíos o no numéricos siempre quedan al final. */
	function comparar(a, b, tipo, ascendente) {
		var vacioA = a === '';
		var vacioB = b === '';
		if (vacioA || vacioB) {
			return vacioA && vacioB ? 0 : (vacioA ? 1 : -1);
		}
		var resultado;
		if (tipo === 'numero') {
			var numeroA = parseFloat(a.replace(/,/g, ''));
			var numeroB = parseFloat(b.replace(/,/g, ''));
			var noNumericoA = isNaN(numeroA);
			var noNumericoB = isNaN(numeroB);
			if (noNumericoA || noNumericoB) {
				return noNumericoA && noNumericoB ? 0 : (noNumericoA ? 1 : -1);
			}
			resultado = numeroA - numeroB;
		} else if (tipo === 'fecha') {
			resultado = a < b ? -1 : (a > b ? 1 : 0); // fechas ISO (yyyy-mm-dd): el orden de texto es el cronológico
		} else {
			resultado = a.localeCompare(b, 'es', { sensitivity: 'base', numeric: true });
		}
		return ascendente ? resultado : -resultado;
	}

	function actualizarContador(tabla) {
		var filas = tabla.tBodies[0] ? Array.prototype.slice.call(tabla.tBodies[0].rows) : [];
		var visibles = filas.filter(function (fila) { return !fila.hidden; }).length;
		var elementoVisibles = tabla.querySelector('[data-contador-visibles]');
		var elementoTotal = tabla.querySelector('[data-contador-total]');
		if (elementoVisibles) {
			elementoVisibles.textContent = String(visibles);
		}
		if (elementoTotal) {
			elementoTotal.textContent = String(filas.length);
		}
	}

	function ordenar(tabla, indice, tipo, ascendente) {
		var cuerpo = tabla.tBodies[0];
		var filas = Array.prototype.slice.call(cuerpo.rows);
		// Array.prototype.sort es estable: las filas con el mismo valor conservan su orden anterior.
		filas.sort(function (a, b) {
			return comparar(valorDeCelda(a, indice), valorDeCelda(b, indice), tipo, ascendente);
		});
		filas.forEach(function (fila) { cuerpo.appendChild(fila); });
	}

	function prepararOrden(tabla) {
		var encabezados = Array.prototype.slice.call(tabla.querySelectorAll('thead th'));
		encabezados.forEach(function (encabezado, indice) {
			var tipo = encabezado.getAttribute('data-tipo');
			if (!tipo) {
				return;
			}
			var boton = document.createElement('button');
			boton.type = 'button';
			boton.className = 'orden';
			boton.title = 'Ordenar por esta columna';
			while (encabezado.firstChild) {
				boton.appendChild(encabezado.firstChild);
			}
			encabezado.appendChild(boton);
			encabezado.setAttribute('aria-sort', 'none');

			boton.addEventListener('click', function () {
				var ascendente = encabezado.getAttribute('aria-sort') !== 'ascending';
				encabezados.forEach(function (otro) {
					if (otro.hasAttribute('data-tipo')) {
						otro.setAttribute('aria-sort', 'none');
					}
				});
				encabezado.setAttribute('aria-sort', ascendente ? 'ascending' : 'descending');
				ordenar(tabla, indice, tipo, ascendente);
			});
		});
	}

	function prepararBusqueda(tabla) {
		var buscadores = document.querySelectorAll('.buscador-tabla[data-tabla="' + tabla.id + '"]');
		buscadores.forEach(function (buscador) {
			buscador.addEventListener('input', function () {
				var consulta = normalizar(buscador.value.trim());
				Array.prototype.slice.call(tabla.tBodies[0].rows).forEach(function (fila) {
					fila.hidden = consulta !== '' && normalizar(fila.textContent).indexOf(consulta) === -1;
				});
				actualizarContador(tabla);
			});
		});
	}

	document.querySelectorAll('table.tabla-interactiva').forEach(function (tabla) {
		if (!tabla.tBodies[0]) {
			return;
		}
		prepararOrden(tabla);
		if (tabla.id) {
			prepararBusqueda(tabla);
		}
		actualizarContador(tabla);
	});
})();
