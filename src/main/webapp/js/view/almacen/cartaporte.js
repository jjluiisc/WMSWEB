
function initCaptura() {
    var $figuratransporte_clave = $("#transporte-datos [name=figuratransporte_clave]");
    var $autotransporte_clave = $("#transporte-datos [name=autotransporte_clave]");

    var figuratransporteComplete = function(record) {
        $figuratransporte_clave.attr("data-record", JSON.stringify(record));
        $figuratransporte_clave.val(record.clave);
        $("#transporte-datos [name=figuratransporte_nombre]").val(record.nombre);
        $("#transporte-datos [name=figuratransporte_rfc]").val(record.rfc);
        $("#transporte-datos [name=figuratransporte_licencia]").val(record.licencia);
        $("#transporte-datos [name=figuratransporte_tipofigura]").val(record.tipofigura);
        $autotransporte_clave.focus();
    };
    $("#btn-buscar-figuratransporte").click(function() {
        $figuratransporte_clave.val("");
        $figuratransporte_clave[0].dispatchEvent(new KeyboardEvent('keyup', {'keyCode': 36}));
    });
    autoBusquedaTipoFiguraTransporte($figuratransporte_clave, figuratransporteComplete);

    var autotransporteComplete = function(record) {
        $autotransporte_clave.attr("data-record", JSON.stringify(record));
        $autotransporte_clave.val(record.clave);
        $("#transporte-datos [name=autotransporte_tipopermiso]").val(record.tipopermiso);
        $("#transporte-datos [name=autotransporte_numeropermiso]").val(record.numeropermiso);
        $("#transporte-datos [name=autotransporte_placa]").val(record.placa);
        $("#transporte-datos [name=autotransporte_aniomodelo]").val(record.aniomodelo);
        //$buttonAceptar.focus();
    };
    $("#btn-buscar-autotransporte").click(function() {
        $autotransporte_clave.val("");
        $autotransporte_clave[0].dispatchEvent(new KeyboardEvent('keyup', {'keyCode': 36}));
    });
    autoBusquedaAutotransporte($autotransporte_clave, autotransporteComplete);

    creaGridFacturas();

    $("#btn-buscar-factura").click(function() {
        buscaFactura();
    });
    var $factura = $("#factura-datos [name=factura]");
    $factura.keydown(function(e) {
        var key = e.keyCode;
        switch(key) {
            case 13:
                buscaFactura();
                return false;
            case 27:
                limpiaFactura();
                return false;
        }
    });

    notify_info("Listo.");
}

var sourceGridFacturas;

function creaGridFacturas() {
    sourceGridFacturas = {
        localdata: [],
        datafields: [
            {name: "compania", type: "string"},
            {name: "flsurtido", type: "number"},
            {name: "idruta", type: "number"},
            {name: "status", type: "string"},
            {name: "fechafacturacion", type: "date"},
            {name: "usuario", type: "string"},
            {name: "factura", type: "string"},
            {name: "distancia", type: "number"},
            {name: "fechallegada", type: "date"}
        ],
        datatype: "array"
    };
    var dataAdapter = new $.jqx.dataAdapter(sourceGridFacturas);
    var $grid = $("<div id=\"grid_facturas\"></div>");
    $grid.jqxGrid({
        width: "100%",
        height: "280px",
        altrows: true,
        sortable: true,
        columnsresize: true,
        showstatusbar: true,
        statusbarheight: 30,
        showaggregates: true,
        source: dataAdapter,
        columns: [
            {text: "Id", datafield: "idruta", width: "5%",
                aggregates: [
                    {"T":
                        function (aggregatedValue, currentValue) {
                            return aggregatedValue+1;
                        }
                    }
                ],
                aggregatesrenderer: function (aggregates, column, element, summaryData) {
                    var renderstring = "<div class='jqx-widget-content style='float: left; width: 100%; height: 100%;'>";
                    $.each(aggregates, function (key, value) {
                        var name = key==="T" ? "" : key+":";
                        var margin = key==="T" ? "margin-top" : "margin-bottom";
                        renderstring += '<div style="position: relative; '+margin+': 6px; margin-left: 6px; margin-right: 6px; text-align: right; overflow: hidden;"><b>'
                                +name+'&nbsp;'+value+'</b></div>';
                    });
                    renderstring += "</div>";
                    return renderstring;
                }
            },
            {text: "F.OS.", datafield: "flsurtido", width: "10%"},
            {text: "Estatus", datafield: "status", width: "10%"},
            {text: "Fecha Facturacion", datafield: "fechafacturacion", width: "20%", cellsformat: "d/M/yyyy HH:mm"},
            {text: "Factura", datafield: "factura", width: "15%"},
            {text: "Distancia Recorrida", datafield: "distancia", width: "20%"},
            {text: "Fecha Llegada", datafield: "fechallegada", width: "20%", cellsformat: "d/M/yyyy HH:mm"}
        ],
        ready: function() {
            //$grid.jqxGrid("selectrow", 0);
            //$grid.jqxGrid("focus");
        }
    });

    $("#facturas").append($grid);
}

function limpiaFactura() {
    limpiaValoresForma($("#factura-datos"));

    notify_info("Listo.");

    var $factura = $("#factura-datos [name=factura]");
    $factura.focus();
}

function buscaFactura() {
    var $factura = $("#factura-datos [name=factura]");
    var factura = $factura.val();
    if (factura==="") {
        $factura.focus();
        return;
    }

    var onAceptar = function() {
        $factura.focus();
    };
    var onFail = function(err) {
        var msg = "Error al buscar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        notify_error(msg);
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
        agregaFactura(response);
        limpiaFactura();
        notify_info("Listo.");
    };

    var where = "compania = '"+usuario.compania+"' AND factura = '"+factura+"'";

    notify_secondary("Buscando la Factura ...");
    lista("mx.reder.wms.dao.entity.RutaFacturaDAO", where, "fechafacturacion DESC",
        onComplete, onError, onFail);
}

function agregaFactura(response) {
    if (response.length===0)
        return;

    var $grid = $("#grid_facturas");

    for (var i=0; i<response.length; i++) {
        var rowdata = response[i];

        rowdata["distancia"] = 0;
        rowdata["fechallegada"] = "";

        var commit = $grid.jqxGrid("addrow", rowdata.idubicacion, rowdata, "last");
        $grid.jqxGrid("clearselection");
    }
}

function quitaFactura() {
    var $grid = $("#grid_facturas");

    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1)
        return;

    var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);

    var aceptar = function() {
        var rowid = $grid.jqxGrid("getrowid", selectedrowindex);
        var commit = $grid.jqxGrid("deleterow", rowid);
        $grid.jqxGrid("ensurerowvisible", 0);
        $grid.jqxGrid("clearselection");

        notify_info("Listo.");
    };
    pregunta("&iquest;Esta seguro de borrar la factura <b>"+rowdata.factura+"</b>?", aceptar);
}

function datosLlegadaFactura() {
    var $grid = $("#grid_facturas");

    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1)
        return;

    var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);

    var onCancel = function() {
    };
    var onSelected = function(response) {
        var fechallegada = response.fechallegada+":00";
        fechallegada = replaceAll(fechallegada, "T", " ");

        rowdata["distancia"] = parseFloat(response.distancia);
        rowdata["fechallegada"] = fechallegada;

        var commit = $grid.jqxGrid("updaterow", selectedrowindex, rowdata);
        $grid.jqxGrid("ensurerowvisible", selectedrowindex);
        $grid.jqxGrid("clearselection");

        notify_info("Listo.");
    };

    seleccionaDistanciaYFechaLlegada(onSelected, onCancel);
}

function seleccionaDistanciaYFechaLlegada(onSelected, onCanceled) {
    var onComplete = function(response) {
        var $content = $(response);

        var registro;
        var aceptar = function () {
            var $form = $("#destino-datos");
            var pass = validaDatosForma($form);
            if (!pass) {
                notify_error("Hay errores con los datos, favor de corregirlos.", $notify);
                return;
            }

            registro = getValoresForma("#destino-datos");
            $modalDialog3.modal("hide");
        };

        var $buttonAceptar = $("<button type=\"button\" class=\"btn btn-outline-success\">Aceptar</button>");
        $buttonAceptar.click(function() {
            aceptar();
        });
        var $buttonCancelar = $("<button type=\"button\" class=\"btn btn-outline-secondary\">Cancelar</button>");
        $buttonCancelar.click(function() {
            registro = null;
            $modalDialog3.modal("hide");
        });

        $("#modalDialog3Label").html("Datos de Destino");
        $("#modalDialog3Document").addClass("modal-lg");
        $("#modalDialog3Header").removeClass();
        $("#modalDialog3Header").addClass("modal-header bg-info text-white");
        $("#modalDialog3Body").empty();
        $("#modalDialog3Body").append($content);
        $("#modalDialog3Footer").empty();
        $("#modalDialog3Footer").append($buttonCancelar);
        $("#modalDialog3Footer").append($buttonAceptar);

        var $distancia = $("#destino-datos [name=distancia]");

        var $notify = $("#destino-notificacion");

        notify_info("Listo", $notify);

        $modalDialog3.unbind("shown.bs.modal");
        $modalDialog3.on("shown.bs.modal", function() {
            $distancia.focus();
        });
        $modalDialog3.unbind("hidden.bs.modal");
        $modalDialog3.on("hidden.bs.modal", function() {
            if (registro) {
                if (onSelected)
                    onSelected(registro);
            } else {
                if (onCanceled)
                    onCanceled();
            }
        });

        $modalDialog3.modal({
            backdrop: "static"
        });
    };

    loadPage("/wms/view/almacen/destino-seleccion.html", onComplete);
}

function generaCartaPorte() {

    var $grid = $("#grid_facturas");
    var rows = $grid.jqxGrid("getrows");

    if (rows.length===0) {
        precaucion("Debe de agregar Facturas para la Carta Porte.");
        return;
    }

    for (var i=0; i<rows.length; i++) {
        var rowdata = rows[i];
        if (rowdata.distancia===0||rowdata.fechallegada==="") {
            precaucion("Debe de capturar los Datos de Llegada en todas las Facturas de la Carta Porte.");
            return;
        }
    }

    console.log(rows);
}


