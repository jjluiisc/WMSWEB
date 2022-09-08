

function initCatalogo() {
    var _catalogo;
    var catalogo = new catalogoABC();
    catalogo.registroCatalogo = "mx.reder.wms.dao.entity.ClaveAlternaDAO";

    catalogo.initialize = function() {
        var $compania = $("#datos [name=compania]");
        $compania.val(usuario.compania);
        var onComplete = function(response) {
            var $razonsocial = $("#datos [name=razonsocial]");
            $razonsocial.val(response.razonsocial);
        };
        buscarCompania(usuario.compania, onComplete);

        $("#btn-lotes").click(function() {
            _catalogo.lotesProducto();
        });
        $("#btn-buscar-lote").click(function() {
            _catalogo.buscarLotesProducto();
        });
        $("#btn-agrega-lote").click(function() {
            _catalogo.agregaLote();
        });
        var $codigo = $("#lotes [name=CVE_ART]");
        $codigo.keypress(function(e) {
            if (e.keyCode===13) {
                _catalogo.buscarLotesProducto();
                return false;
            }
        });

        var $clave = $("#datos [name=clave]");
        $clave.keypress(function(e) {
            if (e.keyCode===13) {
                _catalogo.buscarRegistros();
            }
        });
        $clave.focus();
        notify_info("Listo.");
    };

    catalogo.buscarRegistrosValores = function() {
        var $clave = $("#datos [name=clave]");
        var clave = $clave.val();

        return usuario.compania+"|"+clave;
    };

    catalogo.buscarRegistrosBusqueda = function(complete, error) {
        var $clave = $("#datos [name=clave]");

        busquedaClavesAlternas(usuario.compania, $clave.val(), complete, error);
    };

    catalogo.buscarRegistrosOnAceptar = function() {
        var $clave = $("#datos [name=clave]");
        $clave.select();
        $clave.focus();
    };

    catalogo.guardarRegistroOnComplete = function(response) {
        notify_success("Registro guardado correctamente.");
    };

    catalogo.borrarRegistroConfirmadoOnComplete = function(response) {
        notify_success("Registro borrado correctamente.");

        _catalogo.limpiarRegistro();
    };

    catalogo.limpiarRegistroInit = function() {
        var $clave = $("#datos [name=clave]");
        $clave.focus();
    };

    catalogo.reportesRegistro = function() {
        var params = "compania="+usuario.compania;

        var $clave = $("#datos [name=clave]");
        var clave = $clave.val();
        if (clave!=="")
            params += "&clave="+clave;

        var opciones = [
            {nombre: "Codigos QR", funcion: "ejecutaReporteExcel('ClavesAlternas','"+params+"'); return false;"},
        ];
        abreOpciones("Reportes", opciones);
    };

    catalogo.lotesProducto = function() {
        var $div = $("#div-lotes");
        if ($div.hasClass("invisible")) {
            $div.removeClass("invisible");
        } else {
            $div.addClass("invisible");
        }
    };

    catalogo.buscarLotesProducto = function() {
        var $codigo = $("#lotes [name=CVE_ART]");
        var codigo = $codigo.val();
        if (codigo==="") {
            $codigo.focus();
            return;
        }

        codigo = codigo.toUpperCase();

        var onAceptar = function() {
            $codigo.select();
            $codigo.focus();
            return;
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
            $codigo.attr("data-record", JSON.stringify(response));
            pasaValoresForma("#lotes", response);

            var $caducidad = $("#lotes [name=FCHCADUC]");
            $caducidad.val(getISODate(response.FCHCADUC));

            var $cantidad = $("#lotes [name=CANT]");
            $cantidad.val("1");

            notify_info("Listo.");
        };

        busquedaLotesProductosASPEL(usuario.compania, codigo, onComplete, onError);
    };

    catalogo.agregaLote = function() {
        var $form = $("#lotes");
        var pass = validaDatosForma($form);
        if (!pass) {
            notify_error("Hay errores con los datos, favor de corregirlos.");
            return;
        }

        var $codigo = $("#lotes [name=CVE_ART]");

        var record;
        try  {
            record = JSON.parse($codigo.attr("data-record"));
        } catch(e) {
            $codigo.focus();
            return;
        }

        var valores = getValoresForma("#lotes");

        var qr = valores.CANT+"*"+valores.CVE_ART+"*"+valores.DESCR+"*"+valores.LOTE+"*"+replaceAll(valores.FCHCADUC,"-","");

        var $codigod = $("#datos [name=codigo]");
        $codigod.val(qr);
    };

    catalogo.initCaptura();

    _catalogo = catalogo;
}
