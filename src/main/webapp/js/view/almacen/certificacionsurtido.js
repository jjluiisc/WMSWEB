
function initOrdenesSurtido() {
    datosUsuario();
    creaGridOrdenesSurtido();
    creaGridDetallesOrdenSurtido();
    obtenOrdenesSurtido();

    var $flsurtido = $("#ordensurtido-datos [name=flsurtido]");
    $flsurtido.keypress(function(e) {
        if (e.keyCode===13) {
            certificaOrdenSurtido($flsurtido);
        }
    });
    $flsurtido.focus();

    notify_info("Listo.");
}

function datosUsuario() {
    if (usuario.compania!=="") {
        var $compania = $("#ordensurtido-datos [name=compania]");
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

    var $input = $("#ordensurtido-datos [name="+campo+"]");
    filtros[campo] = aplica ? $input.val() : null;

    obtenOrdenesSurtido();
}

function obtenOrdenesSurtido() {
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
        muestraOrdenesSurtido(array);
    };
    var onComplete = function(response) {
        muestraOrdenesSurtido(response);
        notify_info("Listo.");
    };

    // Todos las ordenes de surtido, TS = Terminado de Surtir o CE = Certificando
    var where = "osp.status IN ('TS','CE')";

    var properties = getProperties(filtros);
    properties.forEach(function(element) {
        if (filtros[element]) {
            if (element==="fechapedido")
                where += " AND FORMAT(osp.fechapedido, 'yyyy-MM-dd') = '"+filtros[element]+"'";
            else if (element==="flsurtido")
                where += " AND osp.flsurtido = "+filtros[element];
            else
                where += " AND osp."+element+" = '"+filtros[element]+"'";
        }
    });

    where += " ORDER BY osp.fechasurtido";

    notify_secondary("Buscando las Ordenes de Surtido ...");
    coleccion("mx.reder.wms.collection.OrdenesSurtidoPedidoCollection", where,
        onComplete, onError, onFail);
}

var selectedRows;
var sourceGrid;

function creaGridOrdenesSurtido() {
    sourceGrid = {
        localdata: [],
        datafields: [
            {name: "compania", type: "string"},
            {name: "flsurtido", type: "number"},
            {name: "status", type: "string"},
            {name: "fechastatus", type: "date"},
            {name: "usuario", type: "string"},
            {name: "equipo", type: "string"},
            {name: "surtidor", type: "string"},
            {name: "fechapedido", type: "date"},
            {name: "cliente", type: "string"},
            {name: "nombrecliente", type: "string"},
            {name: "vendedor", type: "string"},
            {name: "ruta", type: "string"},
            {name: "fechasurtido", type: "date"},
            {name: "fechainicio", type: "date"},
            {name: "fechatermino", type: "date"},
            {name: "cantidad", type: "number"},
            {name: "surtidas", type: "number"},
            {name: "certificadas", type: "number"},
            {name: "total", type: "number"},
            {name: "detalles", type: "number"},
            {name: "flpedido", type: "number"},
            {name: "clase", type: "string"},
            {name: "pedido", type: "string"},
            {name: "cliente", type: "string"},
            {name: "nombrecliente", type: "string"},
            {name: "metodoentrega", type: "string"},
            {name: "fechapedido", type: "date"}
        ],
        datatype: "array"
    };
    var cellclass = function (row, columnfield, value) {
        var selected = selectedRows.get(row);
        var status = sourceGrid.localdata[row].status;
        if (status==="TS") {

        }
        else if (status==="CE") {
            return typeof selected==="undefined" ?
                "row-green" : selected ? "row-dark-green" : "row-green";
        }
        return "";
    };
    var dataAdapter = new $.jqx.dataAdapter(sourceGrid);
    var $grid = $("<div id=\"grid_ordenessurtido\"></div>");
    $grid.jqxGrid({
        width: "100%",
        height: "400px",
        altrows: true,
        sortable: true,
        columnsresize: true,
        source: dataAdapter,
        columns: [
            {text: "Compania", datafield: "compania", width: "5%", cellclassname: cellclass},
            {text: "F.OS.", datafield: "flsurtido", width: "5%", cellclassname: cellclass},
            {text: "Pedido", datafield: "pedido", width: "15%", cellclassname: cellclass},
            {text: "Estatus", datafield: "status", width: "5%", cellclassname: cellclass},
            {text: "Fecha Surtido", datafield: "fechastatus", width: "10%", cellclassname: cellclass, cellsformat: "d/M/yyyy"},
            {text: "Inicio", datafield: "fechainicio", width: "12%", cellclassname: cellclass, cellsformat: "d/M/yyyy HH:mm"},
            {text: "Termino", datafield: "fechatermino", width: "12%", cellclassname: cellclass, cellsformat: "d/M/yyyy HH:mm"},
            {text: "Cantidad", datafield: "cantidad", cellsalign: "right", cellsformat: "f0", width: "8%", cellclassname: cellclass},
            {text: "Surtidas", datafield: "surtidas", cellsalign: "right", cellsformat: "f0", width: "8%", cellclassname: cellclass},
            {text: "Equipo", datafield: "equipo", width: "10%", cellclassname: cellclass},
            {text: "Surtidor", datafield: "surtidor", width: "10%", cellclassname: cellclass}
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
            pasaValoresForma("#ordensurtido-datos", rowdata);

            $("#ordensurtido-datos input[name=fechapedido]").val(getISODate(rowdata.fechapedido));
            $("#ordensurtido-datos input[name=total]").val(formatMoney(rowdata.total));

            var $btn = $("#btn-detalles");
            if ($btn.hasClass("active")) {
                obtenDetalles(rowdata);
            }
        }
    });

    $("#ordenessurtido").append($grid);
}

var sourceGridDetalles;

function creaGridDetallesOrdenSurtido() {
    sourceGridDetalles = {
        localdata: [],
        datafields: [
            {name: "compania", type: "string"},
            {name: "flsurtido", type: "number"},
            {name: "partida", type: "number"},
            {name: "codigo", type: "string"},
            {name: "descripcion", type: "string"},
            {name: "ubicacion", type: "string"},
            {name: "cantidad", type: "number"},
            {name: "surtidas", type: "number"},
            {name: "certificadas", type: "number"},
            {name: "precio", type: "number"},
            {name: "total", type: "number"}
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
            {text: "OS.", datafield: "flsurtido", width: "8%"},
            {text: "Codigo", datafield: "codigo", width: "10%"},
            {text: "Descripcion", datafield: "descripcion", width: "40%"},
            {text: "Cantidad", datafield: "cantidad", cellsalign: "right", cellsformat: "f0", width: "10%",
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
            {text: "Surtidas", datafield: "surtidas", cellsalign: "right", cellsformat: "f0", width: "10%",
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
            {text: "Precio", datafield: "precio", cellsalign: "right", cellsformat: "c2", width: "10%"},
            {text: "Total", datafield: "total", cellsalign: "right", cellsformat: "c2", width: "12%",
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

function muestraOrdenesSurtido(response) {
    sourceGrid.localdata = response;
    selectedRows = new Map();

    var $grid = $("#grid_ordenessurtido");
    $grid.jqxGrid("updatebounddata", "cells");
    $grid.jqxGrid("removesort");
    if (response.length>0)
        $grid.jqxGrid("clearselection");
}

function actualizaOrdenesSurtido() {
    obtenOrdenesSurtido();
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

    var where = "compania = '"+rowdata.compania+"' AND flsurtido = "+rowdata.flsurtido;

    notify_secondary("Buscando los registros de Detalle de Orden de Surtido ...");
    lista("mx.reder.wms.dao.entity.OrdenSurtidoPedidoDetalleDAO", where, "partida",
        onComplete, onError, onFail);
}

function muestraDetalles(response) {
    sourceGridDetalles.localdata = response;

    var $grid = $("#grid_detalles");
    $grid.jqxGrid("updatebounddata", "cells");

    if (response.length>0)
        $grid.jqxGrid("clearselection");
}

function detallesOrdenSurtido() {
    var $btn = $("#btn-detalles");
    if ($btn.hasClass("active")) {
        ocultaDetalles();
        return;
    }

    $btn.addClass("active");

    var $grid = $("#grid_ordenessurtido");
    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1) {
        var msg = "No ha seleccionado ning&uacute;na Orden de Surtido.";
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

function certificaOrdenSurtido($flsurtido) {
    if (!validaPermiso("certificacionSurtidoCaptura"))
        return false;

    var $compania = $("#ordensurtido-datos [name=compania]");
    var compania = $compania.val();
    if (compania==="") {
        $compania.focus();
        return;
    }

    var onAceptar = function() {
        $flsurtido.select();
        $flsurtido.focus();
    };

    var flsurtido = parseInt($flsurtido.val());
    if (isNaN(flsurtido)) {
        onAceptar();
        return;
    }

    // Busco la Orden de Surtido en el grid
    var $grid = $("#grid_ordenessurtido");
    var rows = $grid.jqxGrid("getrows");

    for (var indx=0; indx<rows.length; indx++) {
        var rowdata = rows[indx];
        if (rowdata.compania===compania && rowdata.flsurtido===flsurtido) {
            certificaSurtidoConfirmado(rowdata);
            return;
        }
    }

    var msg = "No encontre esta Orden de Surtido ["+flsurtido+"].";
    precaucion(msg, onAceptar);
    notify_warning(msg);
}

function certificaSurtido() {
    if (!validaPermiso("certificacionSurtidoCaptura"))
        return false;

    var $grid = $("#grid_ordenessurtido");

    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1) {
        var msg = "No ha seleccionado ning&uacute;na Orden de Surtido.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);

    certificaSurtidoConfirmado(rowdata);
}

function certificaSurtidoConfirmado(rowdata) {
    // Si el pedido ya se esta Certificando, me aseguro que sea el mismo usuario
    if (rowdata.status==="CE") {
        if (rowdata.usuario!==usuario.usuario) {
            var msg = "Esta Orden de Surtido de Pedido se esta Certificando por el usuario <b>["+rowdata.usuario+"]</b> en otra terminal.";
            precaucion(msg);
            notify_warning(msg);
            return;
        } else {
            // No pregunta, va directo
            //var onAceptar = function() {
                certificacionSurtidoCaptura(rowdata);
            //};
            //pregunta("Esta Orden de Surtido de Pedido ya se esta Certificando por el usuario <b>["+rowdata.usuario+"]</b> en otra terminal. &iquest;Esta seguro de continuar con la <b>CERTIFICACI&Oacute;N DEL SURTIDO</b> de este Pedido <b>["+rowdata.pedido+"]</b>?", onAceptar);
        }
    }
    // Inicio Certificacion
    else if (rowdata.status==="TS") {
        // No pregunta, va directo
        //var onAceptar = function() {
            certificacionSurtidoCaptura(rowdata);
        //};
        //pregunta("&iquest;Esta seguro de <b>CERTIFICAR EL SURTIDO</b> de esta Orden de Surtido <b>["+rowdata.flsurtido+" "+rowdata.pedido+"]</b>?", onAceptar);
    }
}

function cancelaCertificaSurtido() {
    if (!validaPermiso("certificacionSurtidoCancelar"))
        return false;

    var $grid = $("#grid_ordenessurtido");

    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1) {
        var msg = "No ha seleccionado ning&uacute;na Orden de Surtido.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);

    if (rowdata.status==="CE") {
        var cancelar = function() {
        };
        var aceptar = function() {
            cancelaCertificaOrdenSurtidoConfirmado(rowdata);
        };
        pregunta("&iquest;Realmente desea Cancelar LA CERTIFICACI&Oacute;N de la Orden de Surtido <b>["+rowdata.flsurtido+" "+rowdata.pedido+"]</b> ?", aceptar, cancelar);
    }
    else if (rowdata.status==="TS") {
        var cancelarII = function() {
        };
        var aceptarII = function() {
            cancelaTerminoOrdenSurtidoConfirmado(rowdata);
        };
        pregunta("&iquest;Realmente desea Cancelar EL TERMINO DE SURTIDO DE la Orden de Surtido <b>["+rowdata.flsurtido+" "+rowdata.pedido+"]</b>?<br>"
            +"&iquest;Realmente esta seguro?", aceptarII, cancelarII);
    }
    else {
        var msg = "Estado ["+rowdata.status+"] incorrecto de la Orden de Surtido.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }
}

function cancelaCertificaOrdenSurtidoConfirmado(rowdata) {
    var data = {
        id: "CancelaCertificacionSurtido",
        compania: rowdata.compania,
        usuario: usuario.usuario,
        flsurtido: rowdata.flsurtido
    };

    var onAceptar = function() {
    };
    var onFail = function(err) {
        var msg = "Error al cancelar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        obtenOrdenesSurtido();
        notify_info("Listo.");
    };

    mvc(data, onComplete, onFail, onError);
}

function cancelaTerminoOrdenSurtidoConfirmado(rowdata) {
    var data = {
        id: "CancelaTerminoOrdenSurtidoPedido",
        compania: rowdata.compania,
        usuario: usuario.usuario,
        flsurtido: rowdata.flsurtido
    };

    var onAceptar = function() {
    };
    var onFail = function(err) {
        var msg = "Error al cancelar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        obtenOrdenesSurtido();
        notify_info("Listo.");
    };

    mvc(data, onComplete, onFail, onError);
}
