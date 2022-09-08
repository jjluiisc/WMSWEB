
function initFacturar() {
    datosUsuario();
    creaGridOrdenesSurtido();
    creaGridDetallesOrdenSurtido();
    obtenOrdenesSurtido();

    notify_info("Listo.");
}

function datosUsuario() {
    if (usuario.compania!=="") {
        var $compania = $("#ordensurtido-datos [name=compania]");
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

    // Todos las ordenes de surtido, PE = Pendientes, SU = Surtiendose, TS = Terminado de Surtir, CE = Certificandose y CO = Confirmada
    //var where = "osp.status IN ('PE','SU','TS', 'CE', 'CO')";
    //
    // Vicky: 10 Noviembre 2021
    // 10. En la pantalla de facturar ruta que solo muestre los pedidos que ya fueron certificados o confirmados, que NO muestre los que están pendientes por surtir.
    var where = "osp.status IN ('CO')";

    var properties = getProperties(filtros);
    properties.forEach(function(element) {
        if (filtros[element]) {
            if (element==="fechapedido")
                where += " AND FORMAT(osp.fechapedido, 'yyyy-MM-dd') = '"+filtros[element]+"'";
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
            {name: "fechapedido", type: "date"},
            {name: "fechaconfirmada", type: "date"},
            {name: "fechafacturada", type: "date"},
        ],
        datatype: "array"
    };
    var cellclass = function (row, columnfield, value) {
        var selected = selectedRows.get(row);
        var status = sourceGrid.localdata[row].status;
        if (status==="SU")
            return selected ? "row-dark-green" : "row-green";
        else if (status==="TS"||status==="CE")
            return selected ? "row-dark-yellow" : "row-yellow";
        else if (status==="CO")
            return selected ? "row-dark-blue" : "row-blue";
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
            {text: "Sucursal", datafield: "compania", width: "5%", cellclassname: cellclass},
            {text: "F.OS.", datafield: "flsurtido", width: "5%", cellclassname: cellclass},
            {text: "Pedido", datafield: "pedido", width: "13%", cellclassname: cellclass},
            {text: "Estatus", datafield: "status", width: "5%", cellclassname: cellclass},
            {text: "Fecha Surtido", datafield: "fechastatus", width: "10%", cellclassname: cellclass, cellsformat: "d/M/yyyy"},
            {text: "Cantidad", datafield: "cantidad", cellsalign: "right", cellsformat: "f0", width: "8%", cellclassname: cellclass},
            {text: "Surtidas", datafield: "surtidas", cellsalign: "right", cellsformat: "f0", width: "8%", cellclassname: cellclass},
            {text: "Certificadas", datafield: "certificadas", cellsalign: "right", cellsformat: "f0", width: "8%", cellclassname: cellclass},
            {text: "Equipo", datafield: "equipo", width: "10%", cellclassname: cellclass},
            {text: "Surtidor", datafield: "surtidor", width: "10%", cellclassname: cellclass},
            {text: "Ruta", datafield: "ruta", width: "8%", cellclassname: cellclass},
            {text: "Fecha Confirmada", datafield: "fechaconfirmada", width: "10%", cellclassname: cellclass, cellsformat: "d/M/yyyy"}
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
            {text: "Descripcion", datafield: "descripcion", width: "36%"},
            {text: "Cantidad", datafield: "cantidad", cellsalign: "right", cellsformat: "f0", width: "8%",
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
            {text: "Surtidas", datafield: "surtidas", cellsalign: "right", cellsformat: "f0", width: "8%",
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
            {text: "Certificadas", datafield: "certificadas", cellsalign: "right", cellsformat: "f0", width: "8%",
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

    var pendientes = 0;
    var surtiendo = 0;
    var terminando = 0;
    var confirmado = 0;
    for(var indx=0; indx<response.length; indx++) {
        if (response[indx].status==="PE")
            pendientes++;
        else if (response[indx].status==="SU")
            surtiendo++;
        else if (response[indx].status==="TS"||response[indx].status==="CE")
            terminando++;
        else if (response[indx].status==="CO")
            confirmado++;
    }

    $("#ordenessurtido-totales input[name=tpendientes]").val(pendientes);
    $("#ordenessurtido-totales input[name=tsurtiendo]").val(surtiendo);
    $("#ordenessurtido-totales input[name=tterminados]").val(terminando);
    $("#ordenessurtido-totales input[name=tconfirmados]").val(confirmado);

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

function facturarOrdenesSurtidoRuta() {
    var $btn = $("#btn-ruta");
    var aplica = $btn[0].style.backgroundColor ? true : false;

    var $ruta = $("#ordensurtido-datos [name=ruta]");

    var aceptarII = function() {
        $ruta.select();
        $ruta.focus();
    };

    if (!aplica) {
        mensaje("Para Facturar una Ruta, debe de utilizar el filtro por Ruta.", aceptarII);
        return;
    }

    var ruta = $ruta.val();
    if (ruta==="") {
        mensaje("Para Facturar una Ruta, debe de tener un valor en la Ruta.", aceptarII);
        return;
    }

    var aceptar = function() {
        facturarOrdenesSurtidoRutaConfirmado(ruta);
    };
    pregunta("&iquest;Realmente desea <b>FACTURAR LA RUTA <span class=\"f_size_big\">"+ruta+"</span></b>?<br>"
        +"&iquest;Esta seguro?", aceptar);
}

function facturarOrdenesSurtidoRutaConfirmado(ruta) {
    var $btn = $("#btn-facturar");
    $btn.attr("disabled", true);

    var data = {
        id: "FacturaRuta",
        compania: usuario.compania,
        usuario: usuario.usuario,
        ruta: ruta
    };

    var $grid = $("#grid_ordenessurtido");
    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex!==-1) {
        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        data["flsurtido"] = rowdata.flsurtido;
    }

    var onAceptar = function() {
        $btn.removeAttr("disabled");
    };
    var onFail = function(err) {
        var msg = "Error al facturar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
            obtenOrdenesSurtido();
        };
        var msg = "<b>Facturacion de Ruta Terminada Correctamente.</b><br>";
        var facturas = "";
        for (var indx=0; indx<response.resultados.length; indx++) {
            if (response.resultados[indx].error) {
                var ordensurtido = response.ordenessurtido[indx];
                msg += "<br>Error al facturar la <b>Orden de Surtido ["+ordensurtido.flsurtido+"] del Pedido ["+
                        ordensurtido.pedido+"]</b>, mensaje <span class=\"row-yellow\">{"+response.resultados[indx].mensaje+"}</span>";
            } else {
                facturas += response.resultados[indx].mensaje+" ";
            }
        }
        msg += "<br><br>Facturas Generadas: <b>"+facturas+"</b>";
        mensaje(msg, aceptar);
        notify_success(msg);
    };

    notify_secondary("Facturando Ruta ...");
    mvc(data, onComplete, onFail, onError);
}
