
function initFacturar() {
    datosUsuario();
    creaGridRutas();
    creaGridDetallesRutas();
    obtenRutas();

    notify_info("Listo.");
}

function datosUsuario() {
    if (usuario.compania!=="") {
        var $compania = $("#ruta-datos [name=compania]");
        $compania.val(usuario.compania);
        $compania.attr("disabled", true);
    }
}

var filterColor = "#FFFECF";
var filtros = {};

function filtroCampo(button, campo) {
    if (button.style.backgroundColor)
        button.style.removeProperty("background-color");
    else
        button.style.backgroundColor = filterColor;
    var aplica = button.style.backgroundColor ? true : false;

    var $input = $("#ruta-datos [name="+campo+"]");
    filtros[campo] = aplica ? $input.val() : null;

    obtenRutas();
}

function obtenRutas() {
    var onAceptar = function() {
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
        var array = {};
        muestraRutas(array);
    };
    var onComplete = function(response) {
        muestraRutas(response);
        notify_info("Listo.");
    };

    // Todos las rutas que no esten cerradas o canceladas
    var where = "r.compania = '"+usuario.compania+"' AND r.status NOT IN ('CE','CA')";

    var properties = getProperties(filtros);
    properties.forEach(function(element) {
        if (filtros[element]) {
            if (element==="fechacierre")
                where += " AND FORMAT(r.fechacierre, 'yyyy-MM-dd') = '"+filtros[element]+"'";
            else
                where += " AND r."+element+" = '"+filtros[element]+"'";
        }
    });

    where += " ORDER BY r.fechacreacion";

    notify_secondary("Buscando las Rutas ...");
    coleccion("mx.reder.wms.collection.RutasCollection", where,
        onComplete, onError, onFail);
}

var selectedRows;
var sourceGrid;

function creaGridRutas() {
    sourceGrid = {
        localdata: [],
        datafields: [
            {name: "id", type: "number"},
            {name: "compania", type: "string"},
            {name: "ruta", type: "string"},
            {name: "status", type: "string"},
            {name: "fechastatus", type: "date"},
            {name: "usuario", type: "string"},
            {name: "fechacreacion", type: "date"},
            {name: "fechacierre", type: "date"}
        ],
        datatype: "array"
    };
    var cellclass = function (row, columnfield, value) {
        var selected = selectedRows.get(row);
        var status = sourceGrid.localdata[row].status;
        if (status==="IF"||status==="TF")
            return selected ? "row-dark-green" : "row-green";
        else if (status==="FA")
            return selected ? "row-dark-blue" : "row-blue";
        return "";
    };
    var dataAdapter = new $.jqx.dataAdapter(sourceGrid);
    var $grid = $("<div id=\"grid_rutas\"></div>");
    $grid.jqxGrid({
        width: "100%",
        height: "200px",
        altrows: true,
        sortable: true,
        columnsresize: true,
        source: dataAdapter,
        columns: [
            {text: "Id", datafield: "id", width: "10%", cellclassname: cellclass},
            {text: "Sucursal", datafield: "compania", width: "10%", cellclassname: cellclass},
            {text: "Ruta", datafield: "ruta", width: "10%", cellclassname: cellclass},
            {text: "Estatus", datafield: "status", width: "10%", cellclassname: cellclass},
            {text: "Fecha Estatus", datafield: "fechastatus", width: "15%", cellclassname: cellclass, cellsformat: "d/M/yyyy HH:mm"},
            {text: "Usuario", datafield: "usuario", width: "15%", cellclassname: cellclass},
            {text: "Fecha Creacion", datafield: "fechacreacion", width: "15%", cellclassname: cellclass, cellsformat: "d/M/yyyy HH:mm"},
            {text: "Fecha Cierre", datafield: "fechacierre", width: "15%", cellclassname: cellclass, cellsformat: "d/M/yyyy HH:mm"}
        ],
        ready: function() {
            //$grid.jqxGrid("selectrow", 0);
            //$grid.jqxGrid("focus");
        }
    });
    $grid.on("rowunselect", function (event) {
        var unselectedrowindex = event.args.rowindex;

        selectedRows.put(unselectedrowindex, false);
        $grid.jqxGrid("refresh");
    });
    $grid.on("rowselect", function (event) {
        var selectedrowindex = event.args.rowindex;
        if (selectedrowindex===-1)
            return;

        selectedRows.put(selectedrowindex, true);
        $grid.jqxGrid("refresh");

        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        if (rowdata) {
            pasaValoresForma("#ruta-datos", rowdata);

            $("#ruta-datos input[name=fechastatus]").val(getISODate(rowdata.fechastatus));
            $("#ruta-datos input[name=fechacreacion]").val(getISODate(rowdata.fechacreacion));
            $("#ruta-datos input[name=fechacierre]").val(getISODate(rowdata.fechacierre));

            var $btn = $("#btn-detalles");
            if ($btn.hasClass("active")) {
                obtenDetalles(rowdata);
            }
        }
    });

    $("#rutas").append($grid);
}

var sourceGridDetalles;

function creaGridDetallesRutas() {
    sourceGridDetalles = {
        localdata: [],
        datafields: [
            {name: "compania", type: "string"},
            {name: "flsurtido", type: "number"},
            {name: "idruta", type: "number"},
            {name: "status", type: "string"},
            {name: "fechastatus", type: "date"},
            {name: "usuario", type: "string"},
            {name: "factura", type: "string"},
            {name: "fechafacturacion", type: "date"},
            {name: "mensaje", type: "string"}
        ],
        datatype: "array"
    };
    var dataAdapter = new $.jqx.dataAdapter(sourceGridDetalles);
    var $grid = $("<div id=\"grid_detalles\"></div>");
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
            {text: "Estatus", datafield: "status", width: "5%"},
            {text: "Fecha Estatus", datafield: "fechastatus", width: "15%", cellsformat: "d/M/yyyy HH:mm"},
            {text: "Factura", datafield: "factura", width: "10%"},
            {text: "Fecha Facturada", datafield: "fechafacturacion", width: "15%", cellsformat: "d/M/yyyy HH:mm"},
            {text: "Mensaje", datafield: "mensaje", width: "40%"}
        ],
        ready: function() {
            //$grid.jqxGrid("selectrow", 0);
            //$grid.jqxGrid("focus");
        }
    });

    $("#detalles").append($grid);
}

function muestraRutas(response) {
    sourceGrid.localdata = response;
    selectedRows = new Map();

    var pendientes = 0;
    var proceso = 0;
    var facturadas = 0;
    for(var indx=0; indx<response.length; indx++) {
        if (response[indx].status==="PE")
            pendientes++;
        else if (response[indx].status==="IF"||response[indx].status==="TF")
            proceso++;
        else if (response[indx].status==="FA")
            facturadas++;
    }

    $("#rutas-totales input[name=tpendientes]").val(pendientes);
    $("#rutas-totales input[name=tproceso]").val(proceso);
    $("#rutas-totales input[name=tfacturadas]").val(facturadas);

    var $grid = $("#grid_rutas");
    $grid.jqxGrid("updatebounddata", "cells");
    $grid.jqxGrid("removesort");
    if (response.length>0)
        $grid.jqxGrid("clearselection");
}

function actualizaRutas() {
    obtenRutas();
}

function ocultaDetalles() {
    var $div = $("#div-detalles");
    $div.addClass("invisible");

    var $btn = $("#btn-detalles");
    $btn.removeClass("active");
}

function limpiaDetalles() {
    sourceGridDetalles.localdata = [];

    var $grid = $("#grid_detalles");
    $grid.jqxGrid("clear");
}

function obtenDetalles(rowdata) {
    var onFail = function(err) {
        var msg = "Error al buscar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        notify_error(msg);
    };
    var onError = function(response) {
        if (response.exception.indexOf("WebException")!==-1) {
            notify_warning(response.mensaje);
        } else {
            notify_error(response.exception);
        }
        limpiaDetalles();
    };
    var onComplete = function(response) {
        muestraDetalles(response);
        notify_info("Listo.");
    };

    var where = "compania = '"+rowdata.compania+"' AND idruta = "+rowdata.id;

    notify_secondary("Buscando los registros de Detalle de Ruta ...");
    lista("mx.reder.wms.dao.entity.RutaFacturaDAO", where, "flsurtido",
        onComplete, onError, onFail);
}

function muestraDetalles(response) {
    sourceGridDetalles.localdata = response;

    var $grid = $("#grid_detalles");
    $grid.jqxGrid("updatebounddata", "cells");

    if (response.length>0)
        $grid.jqxGrid("clearselection");
}

function detallesRuta() {
    var $btn = $("#btn-detalles");
    if ($btn.hasClass("active")) {
        ocultaDetalles();
        return;
    }

    $btn.addClass("active");

    var $grid = $("#grid_rutas");
    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1) {
        var msg = "No ha seleccionado ning&uacute;na Ruta.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);

    var $div = $("#div-detalles");
    $div.removeClass("invisible");

    $(window).scrollTop($div.position().top);

    obtenDetalles(rowdata);
}

function verMensaje() {
    var $grid = $("#grid_detalles");
    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1) {
        var msg = "No ha seleccionado ning&uacute;n Detalle de Ruta.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);

    mensaje(rowdata.mensaje);
}

function facturarRuta() {
    var $ruta = $("#ruta-datos [name=ruta]");

    var aceptarII = function() {
        $ruta.select();
        $ruta.focus();
    };

    var ruta = $ruta.val();
    if (ruta==="") {
        mensaje("Para Facturar una Ruta, debe de tener un valor en la Ruta.", aceptarII);
        return;
    }

    var aceptar = function() {
        facturarRutaConfirmado(ruta);
    };
    pregunta("&iquest;Realmente desea <b>FACTURAR LA RUTA <span class=\"f_size_big\">"+ruta+"</span></b>?<br>"
        +"&iquest;Esta seguro?", aceptar);
}

function facturarRutaConfirmado(ruta) {
    var $btn = $("#btn-facturar");
    $btn.attr("disabled", true);

    var data = {
        id: "FacturaRuta",
        compania: usuario.compania,
        usuario: usuario.usuario,
        ruta: ruta
    };

    var onAceptar = function() {
        $btn.removeAttr("disabled");
    };
    var onFail = function(err) {
        var msg = "Error al facturar la ruta.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        error(msg, onAceptar);
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
        onAceptar();

        var aceptar = function() {
            obtenRutas();
            detallesRuta();
        };
        var msg = "<b>Facturacion de Ruta Terminada.</b>";
        msg += "<br>Facturas Generadas: <b>"+response.facturas.length+"</b>";
        if (response.errores.length>0) {
            for (var indx=0; indx<response.errores.length; indx++) {
                msg += "<br>Error: <span class=\"row-yellow\">{"+response.errores[indx].mensaje+"}</span>";
            }
        }
        mensaje(msg, aceptar);
        notify_success(msg);
    };

    notify_secondary("Facturando Ruta ...");
    mvc(data, onComplete, onFail, onError);
}

function paqueteDocumentalRuta() {
    var $ruta = $("#ruta-datos [name=ruta]");

    var aceptarII = function() {
        $ruta.select();
        $ruta.focus();
    };

    var ruta = $ruta.val();
    if (ruta==="") {
        mensaje("Para generar el Paquete Documental de una Ruta, debe de tener un valor en la Ruta.", aceptarII);
        return;
    }

    var aceptar = function() {
        paqueteDocumentalRutaConfirmado(ruta);
    };
    pregunta("&iquest;Realmente desea <b>GENERAR EL PAQUETE DOCUMENTAL DE LA RUTA <span class=\"f_size_big\">"+ruta+"</span></b>?<br>"
        +"&iquest;Esta seguro?", aceptar);
}

function paqueteDocumentalRutaConfirmado(ruta) {
    var $btn = $("#btn-paquete-documental");
    $btn.attr("disabled", true);

    var data = {
        id: "PaqueteDocumental",
        compania: usuario.compania,
        usuario: usuario.usuario,
        ruta: ruta
    };

    var onAceptar = function() {
        $btn.removeAttr("disabled");
    };
    var onFail = function(err) {
        var msg = "Error al generar el paquete documental de la ruta.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        error(msg, onAceptar);
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
        onAceptar();

        download("/wms/Descarga?archivo="+response.wrn);

        var aceptar = function() {
            obtenRutas();
            detallesRuta();
        };
        var msg = "<b>Paquete Documental de Ruta Generado Correctamente.</b>";

        mensaje(msg, aceptar);
        notify_success(msg);
    };

    notify_secondary("Paquete Documental de Ruta ...");
    mvc(data, onComplete, onFail, onError);
}
