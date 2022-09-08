
function initAnalisisInventario() {
    creaGridProductos();
    buscaInventario();

    notify_info("Listo.");
}

function buscaInventario() {
    var $flinventario = $("#form-inventario [name=flinventario]");
    var valores = {
        compania: usuario.compania
    };

    var onAceptar = function() {
        $flinventario.select();
        $flinventario.focus();
    };
    var onFail = function(err) {
        var msg = "Error al ejecutar la accion en el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        notify_error(msg);
    };
    var onError = function(response) {
        if (response.exception.indexOf("WebException")!==-1) {
            precaucion(response.mensaje, onAceptar);
            notify_warning(response.mensaje);
            if (response.mensaje==="No existe un Inventario [PE] en esta compania.") {
                $("#inicia :input").removeAttr("disabled");
            }
        } else {
            error(response.exception, onAceptar);
            notify_error(response.exception);
        }
    };
    var onComplete = function(response) {
        pasaValoresForma("#form-inventario", response);
        obtenDatos(response);
    };
    notify_info("Buscando el registro de Inventario ...");
    accion("mx.reder.wms.dao.entity.InventarioDAO", "busca", JSON.stringify(valores),
        onComplete, onError, onFail);
}

function actualizaDatos() {
    var datos = getValoresForma("#form-inventario");
    obtenDatos(datos);
}

function obtenDatos(response) {
    if (response.fase!=="1ER"&&response.fase!=="2DO"&&response.fase!=="3ER") {
        var msg = "La fase del Inventario es incorrecta ["+response.fase+"].";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

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
        muestraProductos(array);
    };
    var onComplete = function(response) {
        muestraProductos(response);
        notify_info("Listo.");
    };

    var where = "ic.flinventario = "+response.flinventario +" AND ic.existenciac > 0"; 

    notify_secondary("Buscando los registros de Inventario ...");
    coleccion("mx.reder.wms.collection.AnalisisInventarioCollection", where,
        onComplete, onError, onFail);
}

function muestraProductos(response) {
    sourceGrid.localdata = response;
    selectedRows = new Map();

    var analizando = 0;
    for(var indx=0; indx<response.length; indx++) {
        if (response[indx].status==="AN")
            analizando++;
    }

    $("#analisis-totales input[name=tanalizando]").val(analizando);

    var $grid = $("#grid_productos");
    $grid.jqxGrid("updatebounddata", "cells");

    $grid.jqxGrid("clearselection");
}

var selectedRows;
var sourceGrid;

function creaGridProductos() {
    sourceGrid = {
        localdata: [],
        datafields: [
            {name: "fldinventario", type: "int"},
            {name: "flinventario", type: "int"},
            {name: "codigo", type: "string"},
            {name: "descripcion", type: "string"},
            {name: "precio", type: "number"},
            {name: "costo", type: "number"},
            {name: "existencia", type: "number"},
            {name: "existenciac", type: "number"},
            {name: "diferencia", type: "number"},
            {name: "valor", type: "number"},
            {name: "status", type: "string"},
            {name: "terminal", type: "string"},
            {name: "laboratorio", type: "string"}
        ],
        datatype: "array"
    };
    var cellclass = function (row, columnfield, value) {
        var status = sourceGrid.localdata[row].status;
        if (status==="AN") {
            var selected = selectedRows.get(row);
            return typeof selected==="undefined" ?
                "row-green" : selected ? "row-dark-green" : "row-green";
        }
        else if (status==="TE") {
            var selected = selectedRows.get(row);
            return typeof selected==="undefined" ?
                "row-blue" : selected ? "row-dark-blue" : "row-blue";
        }
        return "";
    };
    var dataAdapter = new $.jqx.dataAdapter(sourceGrid);
    var $grid = $("<div id=\"grid_productos\"></div>");
    $grid.jqxGrid({
        width: "100%",
        height: "400px",
        altrows: true,
        sortable: true,
        columnsresize: true,
        selectionmode: "multiplerows",
        showstatusbar: true,
        statusbarheight: 50,
        showaggregates: true,
        source: dataAdapter,
        columns: [
            {text: "C&oacute;digo", datafield: "codigo", width: "10%", cellclassname: cellclass,
                aggregates: ["count"],
                aggregatesrenderer: function (aggregates, column, element, summaryData) {
                    var renderstring = "<div class='jqx-widget-content style='float: left; width: 100%; height: 100%;'>";
                    $.each(aggregates, function (key, value) {
                        var name = key === "count" ? "Cuenta:" : "Total:";
                        var color = "green";
                        renderstring += '<div style="color: '+color+'; position: relative; margin: 6px; text-align: right; overflow: hidden;"><b>'+name+'</b><br>'+formatNumber(value)+'</div>';
                    });
                    renderstring += "</div>";
                    return renderstring;
                }
            },
            {text: "Descripci&oacute;n", datafield: "descripcion", width: "40%", cellclassname: cellclass},
            //{text: "Costo", datafield: "costo",  cellsalign: "right", cellsformat: "c2", width: "10%", cellclassname: cellclass},
            {text: "Existencia", datafield: "existencia", cellsalign: "right", cellsformat: "f0", width: "9%", cellclassname: cellclass,
                aggregates: [
                    {'T':
                        function (aggregatedValue, currentValue) {
                            return aggregatedValue+currentValue;
                        }
                    },
                    {'CE':
                        function (aggregatedValue, currentValue) {
                            if (currentValue!==0) {
                                return aggregatedValue+1;
                            }
                            return aggregatedValue;
                        }
                    }
                ],
                aggregatesrenderer: function (aggregates, column, element, summaryData) {
                    var renderstring = "<div class='jqx-widget-content style='float: left; width: 100%; height: 100%;'>";
                    $.each(aggregates, function (key, value) {
                        var name = key === "T" ? "&Sigma;" : key+":";
                        var margin = key === "T" ? "margin-top" : "margin-bottom";
                        var color = "green";
                        renderstring += '<div style="color: '+color+'; position: relative; '+margin+': 6px; margin-left: 6px; margin-right: 6px; text-align: right; overflow: hidden;"><b>'+name+'</b>&nbsp;'+value+'</div>';
                    });
                    renderstring += "</div>";
                    return renderstring;
                }
            },
            {text: "Conteo", datafield: "existenciac", cellsalign: "right", cellsformat: "f0", width: "9%", cellclassname: cellclass,
                aggregates: [
                    {'T':
                        function (aggregatedValue, currentValue) {
                            return aggregatedValue+currentValue;
                        }
                    },
                    {'CE':
                        function (aggregatedValue, currentValue) {
                            if (currentValue!==0) {
                                return aggregatedValue+1;
                            }
                            return aggregatedValue;
                        }
                    }
                ],
                aggregatesrenderer: function (aggregates, column, element, summaryData) {
                    var renderstring = "<div class='jqx-widget-content style='float: left; width: 100%; height: 100%;'>";
                    $.each(aggregates, function (key, value) {
                        var name = key === "T" ? "&Sigma;" : key+":";
                        var margin = key === "T" ? "margin-top" : "margin-bottom";
                        var color = "green";
                        renderstring += '<div style="color: '+color+'; position: relative; '+margin+': 6px; margin-left: 6px; margin-right: 6px; text-align: right; overflow: hidden;"><b>'+name+'</b>&nbsp;'+value+'</div>';
                    });
                    renderstring += "</div>";
                    return renderstring;
                }
            },
            {text: "Diferencia", datafield: "diferencia", cellsalign: "right", cellsformat: "f0", width: "9%", cellclassname: cellclass,
                aggregates: ["sum"],
                aggregatesrenderer: function (aggregates, column, element, summaryData) {
                    var renderstring = "<div class='jqx-widget-content style='float: left; width: 100%; height: 100%;'>";
                    $.each(aggregates, function (key, value) {
                        var name = key === "sum" ? "Total:" : "Promedio:";
                        var color = "green";
                        renderstring += '<div style="color: '+color+'; position: relative; margin: 6px; text-align: right; overflow: hidden;"><b>'+name+'</b><br>'+value+'</div>';
                    });
                    renderstring += "</div>";
                    return renderstring;
                }
            },
            {text: "Laboratorio", datafield: "laboratorio", width: "10%", cellclassname: cellclass},
            /*{text: "Valor", datafield: "valor", cellsalign: "right", cellsformat: "c2", width: "13%", cellclassname: cellclass,
                aggregates: ["sum"],
                aggregatesrenderer: function (aggregates, column, element, summaryData) {
                    var renderstring = "<div class='jqx-widget-content style='float: left; width: 100%; height: 100%;'>";
                    $.each(aggregates, function (key, value) {
                        var name = key === "sum" ? "Total:" : "Promedio:";
                        var color = "green";
                        renderstring += '<div style="color: '+color+'; position: relative; margin: 6px; text-align: right; overflow: hidden;"><b>'+name+'</b><br>'+value+'</div>';
                    });
                    renderstring += "</div>";
                    return renderstring;
                }
            },*/
            {text: "Estado", datafield: "status", cellsalign: "middle", width: "9%", cellclassname: cellclass,
                aggregates: [
                    {'PE':
                        function (aggregatedValue, currentValue) {
                            if (currentValue==="PE") {
                                return aggregatedValue+1;
                            }
                            return aggregatedValue;
                        }
                    },
                    {'AN':
                        function (aggregatedValue, currentValue) {
                            if (currentValue==="AN") {
                                return aggregatedValue+1;
                            }
                            return aggregatedValue;
                        }
                    }
                ],
                aggregatesrenderer: function (aggregates, column, element, summaryData) {
                    var renderstring = "<div class='jqx-widget-content style='float: left; width: 100%; height: 100%;'>";
                    $.each(aggregates, function (key, value) {
                        var name = key + ":";
                        var margin = key === "PE" ? "margin-top" : "margin-bottom";
                        var color = "green";
                        renderstring += '<div style="color: '+color+'; position: relative; '+margin+': 6px; margin-left: 6px; margin-right: 6px; text-align: right; overflow: hidden;"><b>'+name+'</b>&nbsp;'+formatNumber(value)+'</div>';
                    });
                    renderstring += "</div>";
                    return renderstring;
                }
            },
            {text: "Terminal", datafield: "terminal", cellsalign: "left", width: "9%", cellclassname: cellclass}
        ],
        ready: function() {
            $grid.jqxGrid("clearselection");
            $grid.jqxGrid("focus");
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
    });

    $("#productos").append($grid);
}

function mandaAnalizar() {
    var $grid = $("#grid_productos");
    var rowindexes = $grid.jqxGrid("getselectedrowindexes");
    if (rowindexes.length===0) {
        var msg = "No ha seleccionado ningun rengl&oacute;n.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var onCancel = function() {
    };
    var onSelected = function(response) {
        enviaAnalizar(response);
    };
    seleccionaTerminal(onSelected, onCancel);
}

function enviaAnalizar(response) {
    var datos = getValoresForma("#form-inventario");

    var data = {
        id: "AppValidaTerminalInventario",
        compania: usuario.compania,
        folio: datos.flinventario,
        terminal: response.terminal
    };

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
    };
    var onComplete = function(response) {
        enviaAnalizarConfirmado(data);
        notify_info("Listo.");
    };

    mvc(data, onComplete, onFail, onError);
}

function enviaAnalizarConfirmado(data) {
    var $grid = $("#grid_productos");
    var rowindexes = $grid.jqxGrid("getselectedrowindexes");
    if (rowindexes.length===0) {
        var msg = "No ha seleccionado ningun rengl&oacute;n.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var indices = "";
    var detalles = "";
    var codigos = "";
    for(var indx=0; indx<rowindexes.length; indx++) {
        var selectedrowindex = rowindexes[indx];
        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        if (rowdata) {
            indices = indices+selectedrowindex+";";
            detalles = detalles+rowdata.fldinventario+";";
            codigos = codigos+rowdata.codigo+";";
        }
    }

    data["id"] = "AppAnalizaProductosInventario";
    data["indices"] = indices;
    data["detalles"] = detalles;
    data["codigos"] = codigos;

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
    };
    var onComplete = function(response) {
        actualizaDatos();
        notify_info("Listo.");
    };

    mvc(data, onComplete, onFail, onError);
}



