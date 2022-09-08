
function initCatalogo(direccion) {
    var _catalogo;
    var catalogo = new catalogoABC();
    catalogo.registroCatalogo = "mx.reder.wms.dao.entity.DireccionDAO";

    catalogo.initialize = function() {
        var $direccion = $("#datos [name=direccion]");
        $direccion.keypress(function(e) {
            if (e.keyCode===13) {
                _catalogo.buscarRegistros();
            }
        });

        $direccion.val(direccion);
        $direccion.attr("readonly", true);

        var $calle = $("#datos [name=calle]");
        $calle.focus();

        $("#btn-codigopostal").click(function() {
            _catalogo.buscarCodigoPostal();
        });

        var $codigopostal = $("#datos [name=codigopostal]");
        $codigopostal.keypress(function(e) {
            if (e.keyCode===13) {
                _catalogo.buscarCodigoPostal();
            }
        });

        notify_info("Listo.");
    };

    catalogo.buscarRegistrosValores = function() {
        var $direccion = $("#datos [name=direccion]");
        var direccion = $direccion.val();

        return direccion+"|";
    };

    catalogo.buscarRegistrosBusqueda = function(complete, error) {
        // No busca nada mas, porque siembre llega este catalogo con el ID de Direccion
    };

    catalogo.buscarRegistrosOnAceptar = function() {
        var $calle = $("#datos [name=calle]");
        $calle.select();
        $calle.focus();
    };

    catalogo.buscarRegistrosOnComplete = function(response) {
        pasaValoresForma("#datos", response);
        _catalogo.buscarColonia();

        notify_info("Listo.");
    };

    catalogo.guardarRegistroOnComplete = function(response) {
        notify_success("Registro guardado correctamente.");
    };

    catalogo.borrarRegistroConfirmadoOnComplete = function(response) {
        notify_success("Registro borrado correctamente.");

        _catalogo.limpiarRegistro();
    };

    catalogo.limpiarRegistroInit = function() {
        var $calle = $("#datos [name=calle]");
        $calle.focus();
    };


    catalogo.buscarColonia = function() {
        var $compania = $("#datos [name=compania]");
        var compania = $compania.val();

        var $colonia = $("#datos [name=colonia]");
        var colonia = $colonia.val();
        if (colonia==="")
            return;

        var onAceptar = function() {
            $colonia.select();
            $colonia.focus();
        };
        var onFail = function(err) {
            var mensaje = "Error al buscar el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
            notify_error(mensaje);
        };
        var onError = function(response) {
            if (response.exception.indexOf("WebException")!==-1) {
                precaucion(response.mensaje, onAceptar);
                notify_warning(response.mensaje);
            } else {
                error(response.exception, onAceptar);
                notify_error(response.exception);
            }
        };
        var onComplete = function(response) {
            $("#datos [name=dscolonia]").val(response[0].dscolonia);
            $("#datos [name=dspoblacion]").val(response[0].dspoblacion);
            $("#datos [name=dsentidadfederativa]").val(response[0].dsentidadfederativa);
            $("#datos [name=dspais]").val(response[0].dspais);
            notify_info("Listo.");
        };
        notify_info("Buscando el registro ...");
        coleccion("mx.reder.wms.collection.ColoniasCollection", "c.colonia = '"+colonia+"'",
            onComplete, onError, onFail);
    };

    catalogo.buscarCodigoPostal = function() {
        var $codigopostal = $("#datos [name=codigopostal]");
        var codigopostal = $codigopostal.val();
        if (codigopostal==="")
            return;

        var onAceptar = function() {
            $codigopostal.select();
            $codigopostal.focus();
        };
        var onFail = function(err) {
            var mensaje = "Error al buscar el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
            notify_error(mensaje);
        };
        var onError = function(response) {
            if (response.exception.indexOf("WebException")!==-1) {
                precaucion(response.mensaje, onAceptar);
                notify_warning(response.mensaje);
            } else {
                error(response.exception, onAceptar);
                notify_error(response.exception);
            }
        };
        var onComplete = function(response) {
            pasaValoresForma("#datos", response);
            notify_info("Listo.");
        };
        notify_info("Buscando el registro ...");
        busquedaColonias(codigopostal, onComplete, onError);
    };

    catalogo.initCaptura();

    _catalogo = catalogo;

    _catalogo.buscarRegistros();
}
