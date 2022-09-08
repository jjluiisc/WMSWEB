
function initPedidos() {
    datosUsuario();
    creaGridPedidos();
    creaGridDetallesPedido();

    notify_info("Listo.");
}

function datosUsuario() {
    if (usuario.compania!=="") {
        var $compania = $("#pedido-datos [name=compania]");
        $compania.val(usuario.compania);
        $compania.attr("disabled", true);
        var $btncompania = $("#btn-compania");
        $btncompania.click();
        $btncompania.attr("disabled", true);
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

    var $input = $("#pedido-datos [name="+campo+"]");
    filtros[campo] = aplica ? $input.val() : null;

    obtenPedidos();
}

function obtenPedidos() {
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
        muestraPedidos(array);
    };
    var onComplete = function(response) {
        muestraPedidos(response);
        notify_info("Listo.");
    };

    // Todos los pedidos con estado O = original
    var where = "p.STATUS = 'O'";

    var properties = getProperties(filtros);
    properties.forEach(function(element) {
        if (filtros[element]) {
            if (element==="compania")
                ;
            else if (element==="FECHA_DOC")
                where += " AND FORMAT(p.FECHA_DOC, 'yyyy-MM-dd') = '"+filtros[element]+"'";
            else if (element==="CVE_VEND")
                where += " AND p.CVE_VEND = '"+filtros[element]+"'";
            else
                where += " AND "+element+" = '"+filtros[element]+"'";
        }
    });

    where += " ORDER BY p.FECHA_DOC";

    notify_secondary("Buscando los registros de Pedido ...");
    ASPELcoleccion("mx.reder.wms.collection.ASPELPedidosCollection", usuario.compania, where,
        onComplete, onError, onFail);
}

var selectedRows;
var sourceGrid;

function creaGridPedidos() {
    sourceGrid = {
        localdata: [],
        datafields: [
            {name: "TIP_DOC", type: "string"},
            {name: "CVE_DOC", type: "string"},
            {name: "CVE_CLPV", type: "string"},
            {name: "NOMBRE", type: "string"},
            {name: "CAMPLIB1", type: "string"},
            {name: "STATUS", type: "string"},
            {name: "CVE_VEND", type: "string"},
            {name: "FECHA_DOC", type: "date"},
            {name: "IMPORTE", type: "number"}
        ],
        datatype: "array"
    };
    var cellclass = function (row, columnfield, value) {
        return "";
    };
    var dataAdapter = new $.jqx.dataAdapter(sourceGrid);
    var $grid = $("<div id=\"grid_pedidos\"></div>");
    $grid.jqxGrid({
        width: "100%",
        height: "400px",
        altrows: true,
        sortable: true,
        columnsresize: true,
        selectionmode: "multiplerows",
        source: dataAdapter,
        columns: [
            {text: "Tipo", datafield: "TIP_DOC", width: "5%", cellclassname: cellclass},
            {text: "Pedido", datafield: "CVE_DOC", width: "10%", cellclassname: cellclass},
            {text: "Estado", datafield: "STATUS", width: "5%", cellclassname: cellclass},
            {text: "Cliente", datafield: "CVE_CLPV", width: "10%", cellclassname: cellclass},
            {text: "Nombre", datafield: "NOMBRE", width: "25%", cellclassname: cellclass},
            {text: "Ruta", datafield: "CAMPLIB1", width: "10%", cellclassname: cellclass},
            {text: "Vendedor", datafield: "CVE_VEND", width: "10%", cellclassname: cellclass},
            {text: "Fecha", datafield: "FECHA_DOC", width: "15%", cellclassname: cellclass, cellsformat: "d/M/yyyy HH:mm"},
            {text: "Total", datafield: "IMPORTE", cellsalign: "right", cellsformat: "c2", width: "10%", cellclassname: cellclass}
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
            pasaValoresForma("#pedido-datos", rowdata);

            $("#pedido-datos input[name=FECHA_DOC]").val(getISODate(rowdata.FECHA_DOC));
            $("#pedido-datos input[name=IMPORTE]").val(formatMoney(rowdata.IMPORTE));

            var $btn = $("#btn-detalles");
            if ($btn.hasClass("active")) {
                obtenDetalles(rowdata);
            }
        }
    });

    $("#pedidos").append($grid);
}

var sourceGridDetalles;

function creaGridDetallesPedido() {
    sourceGridDetalles = {
        localdata: [],
        datafields: [
            {name: "CVE_DOC", type: "string"},
            {name: "NUM_PAR", type: "number"},
            {name: "CVE_ART", type: "string"},
            {name: "CANT", type: "number"},
            {name: "DESCR", type: "string"},
            {name: "CANT", type: "number"},
            {name: "PREC", type: "number"},
            {name: "TOT_PARTIDA", type: "number"}
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
            {text: "Pedido", datafield: "CVE_DOC", width: "10%"},
            {text: "Partida", datafield: "NUM_PAR", width: "5%"},
            {text: "Codigo", datafield: "CVE_ART", width: "10%"},
            {text: "Descripcion", datafield: "DESCR", width: "40%"},
            {text: "Cantidad", datafield: "CANT", cellsalign: "right", cellsformat: "f0", width: "10%",
                aggregates: [
                    {"T":
                        function (aggregatedValue, currentValue) {
                            return aggregatedValue+currentValue;
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
            {text: "Precio", datafield: "PREC", cellsalign: "right", cellsformat: "c2", width: "10%"},
            {text: "Total", datafield: "TOT_PARTIDA", cellsalign: "right", cellsformat: "c2", width: "10%",
                aggregates: [
                    {"T":
                        function (aggregatedValue, currentValue) {
                            return aggregatedValue+currentValue;
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
            }
        ],
        ready: function() {
            //$grid.jqxGrid("selectrow", 0);
            //$grid.jqxGrid("focus");
        }
    });

    $("#detalles").append($grid);
}

function muestraPedidos(response) {
    sourceGrid.localdata = response;
    selectedRows = new Map();

    var $grid = $("#grid_pedidos");
    $grid.jqxGrid("updatebounddata", "cells");

    if (response.length>0)
        $grid.jqxGrid("clearselection");
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

    var where = "CVE_DOC = '"+rowdata.CVE_DOC+"'";

    notify_secondary("Buscando los registros de Detalle de Pedido ...");
    ASPELcoleccion("mx.reder.wms.collection.ASPELPedidosDetallesCollection", usuario.compania, where,
        onComplete, onError, onFail);
}

function muestraDetalles(response) {
    sourceGridDetalles.localdata = response;

    var $grid = $("#grid_detalles");
    $grid.jqxGrid("updatebounddata", "cells");

    if (response.length>0)
        $grid.jqxGrid("clearselection");
}

function detallesPedido() {
    var $btn = $("#btn-detalles");
    if ($btn.hasClass("active")) {
        ocultaDetalles();
        return;
    }

    $btn.addClass("active");

    var $grid = $("#grid_pedidos");
    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1) {
        var msg = "No ha seleccionado ning&uacute;n Pedido.";
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

function seleccionaTodosPedidos() {
    var $button = $("#btn-selecciona-todos");
    var seleccionado = $button.hasClass("active");
    if (!seleccionado) {
        $button.addClass("active");
    } else {
        $button.removeClass("active");
    }

    var $grid = $("#grid_pedidos");

    if (!seleccionado) {
        var rows = $grid.jqxGrid("getrows");
        for(var indx=0; indx<rows.length; indx++) {
            selectedRows.put(indx, true);
        }
        $grid.jqxGrid("selectallrows");
    } else {
        selectedRows = new Map();
        $grid.jqxGrid("clearselection");

        ocultaDetalles();
    }

    $grid.jqxGrid("refresh");
}

function generaSurtido() {
    var $grid = $("#grid_pedidos");
    var rowindexes = $grid.jqxGrid("getselectedrowindexes");
    if (rowindexes.length===0) {
        var msg = "No ha seleccionado ning&uacute;n pedido.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var pedidos = "";
    var registros = "";
    for(var indx=0; indx<rowindexes.length; indx++) {
        var selectedrowindex = rowindexes[indx];
        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        if (rowdata) {
            pedidos = pedidos+" "+rowdata.CVE_DOC+";";
            registros = registros+rowdata.CVE_DOC+"|";
        }
    }

    if (pedidos.length===0) {
        var msg = "No hay información de Pedidos seleccionados.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var onAceptar = function() {
        generaSurtidoConfirmado(registros);
    };
    pregunta("&iquest;Esta seguro de <b>INICIAR EL SURTIDO</b> de estos Pedidos <b>["+pedidos+"]</b>?", onAceptar);
}

function generaSurtidoConfirmado(registros) {
    var $btn = $("#btn-asigna-surtido");
    $btn.attr("disabled", true);

    var data = {
        id: "GeneraOrdenesSurtidoPedidos",
        compania: usuario.compania,
        usuario: usuario.usuario,
        registros: registros
    };

    var onAceptar = function(err) {
        $btn.removeAttr("disabled");
        $btn.focus();
    };
    var onFail = function(err) {
        var mensaje = "Error al Generar las Ordenes de Surtido.<br><b>("+err.status+") "+err.statusText+"</b>";
        notify_error(mensaje);
        error(err.responseText, onAceptar);
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
        notify_info("Generaci&oacute;n de Ordenes de Surtido Terminada Correctamente.");
        var aceptar = function() {
            onAceptar();
            obtenPedidos();
        };
        mensaje("<b>Generaci&oacute;n de Ordenes de Surtido Terminada Correctamente.</b>", aceptar);
    };
    notify_secondary("Generando Ordenes de Surtido ...");
    mvc(data, onComplete, onFail, onError);
}
