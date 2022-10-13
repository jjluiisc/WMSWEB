var _rowdata;

function initCertificacionSurtido(rowdata) {
    _rowdata = rowdata;
    pasaValoresOrdenSurtido(rowdata);
    creaGridDetalles();
    creaGridDetallesCertificada();
    obtenDetalles(rowdata);

    var $contenedor = $("#certificacionsurtidodetalle [name=contenedor]");
    $contenedor.keydown(function(e) {
        var key = e.keyCode;
        switch(key) {
            case 13:
                $codigo.select();
                $codigo.focus();
                return false;
            case 27:
                limpiarDetalle();
                return false;
        }
    });

    var $codigo = $("#certificacionsurtidodetalle [name=codigo]");
    $codigo.keydown(function(e) {
        var key = e.keyCode;
        switch(key) {
            case 13:
                buscaProductoAlterno();
                return false;
            case 27:
                limpiarDetalle();
                return false;
        }
    });
    
    var $lote = $("#cambioLotesurtidodetalle [name=lote]");
    $lote.keydown(function(e) {
        var key = e.keyCode;
        switch(key) {
            case 13:
                buscarLotesProducto();
                return false;
        }
    });

    var $certificadas = $("#certificacionsurtidodetalle [name=certificadas]");
    $certificadas.keydown(function(e) {
        var key = e.keyCode;
        switch(key) {
            case 13:
                aceptaCertificacion();
                return false;
            case 27:
                limpiarDetalle();
                return false;
        }
    });

    notify_info("Listo.");
}

var sourceGrid;

function creaGridDetalles() {
    sourceGrid = {
        localdata: [],
        datafields: [
            {name: "compania", type: "string"},
            {name: "flsurtido", type: "number"},
            {name: "partida", type: "number"},
            {name: "idlote", type: "number"},
            {name: "codigo", type: "string"},
            {name: "descripcion", type: "string"},
            {name: "lote", type: "string"},
            {name: "fecaducidad", type: "date"},
            {name: "surtidas", type: "number"}
        ],
        datatype: "array"
    };
    var cellclass = function (row, columnfield, value) {
        return "";
    };
    var dataAdapter = new $.jqx.dataAdapter(sourceGrid);
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
            {text: "Partida", datafield: "partida", width: "5%", cellclassname: cellclass},
            {text: "ID", datafield: "idlote", width: "5%", cellclassname: cellclass},
            {text: "Codigo", datafield: "codigo", width: "10%", cellclassname: cellclass},
            {text: "Descripcion", datafield: "descripcion", width: "40%", cellclassname: cellclass},
            {text: "Lote", datafield: "lote", width: "10%", cellclassname: cellclass},
            {text: "Caducidad", datafield: "fecaducidad", width: "12%", cellclassname: cellclass, cellsformat: "d/M/yyyy"},
            {text: "Surtidas", datafield: "surtidas", width: "10%", cellsformat: "f0", cellsalign: "right", cellclassname: cellclass,
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
    $grid.on("rowselect", function (event) {
        var selectedrowindex = event.args.rowindex;
        if (selectedrowindex===-1)
            return;

        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        if (rowdata) {
        }
    });

    $("#detalles").append($grid);
}

var sourceGridCertificada;

function creaGridDetallesCertificada() {
    sourceGridCertificada = {
        localdata: [],
        datafields: [
            {name: "compania", type: "string"},
            {name: "flsurtido", type: "number"},
            {name: "partida", type: "number"},
            {name: "idlote", type: "number"},
            {name: "idcontenedor", type: "number"},
            {name: "codigo", type: "string"},
            {name: "descripcion", type: "string"},
            {name: "contenedor", type: "string"},
            {name: "lote", type: "string"},
            {name: "fecaducidad", type: "date"},
            {name: "certificadas", type: "number"}
        ],
        datatype: "array"
    };
    var cellclass = function (row, columnfield, value) {
        return "";
    };
    var dataAdapter = new $.jqx.dataAdapter(sourceGridCertificada);
    var $grid = $("<div id=\"grid_detalles_certificada\"></div>");
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
            {text: "Partida", datafield: "partida", width: "5%", cellclassname: cellclass},
            {text: "ID", datafield: "idcontenedor", width: "5%", cellclassname: cellclass},
            {text: "Codigo", datafield: "codigo", width: "10%", cellclassname: cellclass},
            {text: "Descripcion", datafield: "descripcion", width: "38%", cellclassname: cellclass},
            {text: "Contenedor", datafield: "contenedor", width: "10%", cellclassname: cellclass},
            {text: "Lote", datafield: "lote", width: "10%", cellclassname: cellclass},
            {text: "Caducidad", datafield: "fecaducidad", width: "12%", cellclassname: cellclass, cellsformat: "d/M/yyyy"},
            {text: "Certificadas", datafield: "certificadas", width: "10%", cellsformat: "f0", cellsalign: "right", cellclassname: cellclass,
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
    $grid.on("rowselect", function (event) {
        var selectedrowindex = event.args.rowindex;
        if (selectedrowindex===-1)
            return;

        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        if (rowdata) {
        }
    });

    $("#detalles-certificada").append($grid);
}

function pasaValoresOrdenSurtido(rowdata) {
    pasaValoresForma("#ordensurtido-datos", rowdata);
    $("#ordensurtido-datos [name=flsurtido]").attr("data-record", JSON.stringify(rowdata));

    $("#ordensurtido-datos input[name=fechapedido]").val(getISODate(rowdata.fechapedido));
    $("#ordensurtido-datos input[name=cantidad]").val(rowdata.cantidad);
    $("#ordensurtido-datos input[name=total]").val(formatMoney(rowdata.total));
}

function muestraDetalles(response) {
    sourceGrid.localdata = response;

    var $grid = $("#grid_detalles");
    $grid.jqxGrid("updatebounddata", "cells");
    if (response.length>0)
        $grid.jqxGrid("clearselection");
}

function muestraDetallesCertificada(response) {
    sourceGridCertificada.localdata = response;

    var $grid = $("#grid_detalles_certificada");
    $grid.jqxGrid("updatebounddata", "cells");
    if (response.length>0)
        $grid.jqxGrid("clearselection");
}

function obtenDetalles(rowdata) {
    var data = {
        id: "DetallesCertificacionSurtido",
        compania: rowdata.compania,
        usuario: usuario.usuario,
        flsurtido: rowdata.flsurtido
    };

    var onAceptar = function() {
    };
    var onFail = function(err) {
        var msg = "Error al obtener los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        muestraDetalles(array);
    };
    var onComplete = function(response) {
        muestraDetalles(response.lotes);
        muestraDetallesCertificada(response.detalles);

        var $certificadas = $("#certificacionsurtidodetalle [name=certificadas]");
        $(window).scrollTop($certificadas.position().top);

        var $contenedor = $("#certificacionsurtidodetalle [name=contenedor]");
        $contenedor.focus();

        notify_info("Listo.");
    };

    mvc(data, onComplete, onFail, onError);
}

function borrarCertificacion() {
    var $grid = $("#grid_detalles_certificada");

    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1) {
        var msg = "No ha seleccionado ning&uacute;n Detalle de Certificaci&oacute;n.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }

    var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);

    var cancelar = function() {
    };
    var aceptar = function() {
        borrarCertificacionConfirmado(rowdata);
    };
    pregunta("&iquest;Realmente desea Borrar el Detalle de Certificaci&oacute;n de Surtido <b>["
            +rowdata.codigo+" "+rowdata.descripcion+" "+rowdata.contenedor+" "+rowdata.lote+"]</b> ?", aceptar, cancelar);
}

function borrarCertificacionConfirmado(rowdata) {
    var onAceptar = function() {
    };
    var onFail = function(err) {
        var msg = "Error al borrar el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        var msg = "Certifica Surtido borrado correctamente.";
        notify_success(msg);

        borraDetalleCertifica(rowdata);

        limpiarDetalle();
    };

    notify_secondary("Borrando Certificaci&oacute;n Surtido ...");
    accion("mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO", "borrar", JSON.stringify(rowdata),
        onComplete, onError, onFail);
}

function borraDetalleCertifica(rowdata) {
    var $grid = $("#grid_detalles_certificada");

    var selectedrowindex = $grid.jqxGrid("selectedrowindex");

    var rowid = $grid.jqxGrid("getrowid", selectedrowindex);
    var commit = $grid.jqxGrid("deleterow", rowid);
    $grid.jqxGrid("ensurerowvisible", 0);
    $grid.jqxGrid("clearselection");
}

function cancelarCertificacion() {
    if (!validaPermiso("certificacionSurtidoCancelar"))
        return false;

    var $flsurtido = $("#ordensurtido-datos [name=flsurtido]");
    if ($flsurtido.val()==="")
        return;

    var rowdata = JSON.parse($flsurtido.attr("data-record"));

    var aceptar = function() {
        cancelarCertificacionConfirmado(rowdata);
    };
    pregunta("&iquest;Realmente desea <b>CANCELAR LA CERTIFICACI&Oacute;N</b> de la Orden de Surtido de Pedido <b>["+rowdata.flsurtido+" "+rowdata.pedido+"]</b>?<br>"
        +"&iquest;Realmente esta seguro?", aceptar);
}

function cancelarCertificacionConfirmado(rowdata) {
    var $btn = $("#btn-cancela");
    $btn.attr("disabled", true);

    var data = {
        id: "CancelaCertificacionSurtido",
        compania: rowdata.compania,
        usuario: usuario.usuario,
        flsurtido: rowdata.flsurtido
    };

    var onAceptar = function() {
        $btn.removeAttr("disabled");
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
        var aceptar = function() {
            window.history.back();
        };
        var msg = "<b>Cancelaci&oacute;n de Certificaci&oacute;n de Surtido Terminada Correctamente.</b>";
        mensaje(msg, aceptar);
        notify_success(msg);
    };

    mvc(data, onComplete, onFail, onError);
}

function buscaProducto() {
    var $contenedor = $("#certificacionsurtidodetalle [name=contenedor]");
    var contenedor = $contenedor.val();
    if (contenedor==="") {
        $contenedor.focus();
        return;
    }

    var $codigo = $("#certificacionsurtidodetalle [name=codigo]");
    var codigo = $codigo.val();
    if (codigo==="") {
        $codigo.focus();
        return;
    }

    var $flsurtido = $("#ordensurtido-datos [name=flsurtido]");
    var rowdata = JSON.parse($flsurtido.attr("data-record"));

    var onAceptar = function() {
        $codigo.select();
        $codigo.focus();
    };
    var onFail = function(err) {
        var msg = "Error al buscar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        if (response.length===0) {
            buscaProductoClaveAlterna($codigo, rowdata);
            return;
        }
        if (response.length>1) {
            mensaje("Tengo mas de un resultado en la busqueda de productos");
            return;
        }
        tengoProducto($codigo, response[0]);
        notify_info("Listo.");
    };

    var where = "i.CVE_ART = '"+codigo+"'";

    notify_secondary("Buscando Producto ...");
    ASPELcoleccion("mx.reder.wms.collection.ASPELProductosCollection", rowdata.compania, where,
        onComplete, onError, onFail);
}

function buscaProductoAlterno() {
    var $contenedor = $("#certificacionsurtidodetalle [name=contenedor]");
    var contenedor = $contenedor.val();
    if (contenedor==="") {
        $contenedor.focus();
        return;
    }

    var $codigo = $("#certificacionsurtidodetalle [name=codigo]");
    var codigo = $codigo.val();
    if (codigo==="") {
        $codigo.focus();
        return;
    }

    var $flsurtido = $("#ordensurtido-datos [name=flsurtido]");
    var rowdata = JSON.parse($flsurtido.attr("data-record"));

    var onAceptar = function() {
        $codigo.select();
        $codigo.focus();
    };
    var onFail = function(err) {
        var msg = "Error al buscar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        if (response.length===0) {
            var msg = "No hay productos con esta clave alterna ["+codigo+"]";
            precaucion(msg, onAceptar);
            notify_warning(msg);
            return;
        }
        if (response.length>1) {
            mensaje("Tengo mas de un resultado en la busqueda de productos");
            return;
        }
        tengoProducto($codigo, response[0]);
        notify_info("Listo.");
    };

    var where = "i.CVE_ART IN (SELECT CVE_ART FROM REDER20.dbo.CVES_ALTER"+rowdata.compania+" WHERE CVE_ALTER = '"+codigo+"')";

    notify_secondary("Buscando Producto ...");
    ASPELcoleccion("mx.reder.wms.collection.ASPELProductosCollection", rowdata.compania, where,
        onComplete, onError, onFail);
}

function buscaProductoClaveAlterna($codigo, rowdata) {
    var codigo = $codigo.val();

    var onAceptar = function() {
        $codigo.select();
        $codigo.focus();
    };
    var onFail = function(err) {
        var msg = "Error al buscar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        if (response.length===0) {
            var msg = "No hay productos con esta clave ["+codigo+"]";
            precaucion(msg, onAceptar);
            notify_warning(msg);
            return;
        }
        if (response.length>1) {
            mensaje("Tengo mas de un resultado en la busqueda de productos");
            return;
        }
        tengoProducto($codigo, response[0]);
        notify_info("Listo.");
    };

    var where = "i.CVE_ART IN (SELECT CVE_ART FROM REDER20.dbo.CVES_ALTER"+rowdata.compania+" WHERE CVE_ALTER = '"+codigo+"')";

    notify_secondary("Buscando Producto ...");
    ASPELcoleccion("mx.reder.wms.collection.ASPELProductosCollection", rowdata.compania, where,
        onComplete, onError, onFail);
}

function tengoProducto($codigo, record) {
    var onAceptar = function() {
        $codigo.select();
        $codigo.focus();
    };

    var surtidos = buscaDetallesSurtidos(record.CVE_ART);

    if (surtidos.length===0) {
        var msg = "Este producto ["+record.CVE_ART+" "+record.DESCR+"] no esta dentro del surtido.";
        precaucion(msg, onAceptar);
        notify_warning(msg);
        return;
    }

    var $codigo = $("#certificacionsurtidodetalle [name=codigo]");
    $codigo.val(record.CVE_ART);
    $codigo.attr("data-record", JSON.stringify(record));

    $("#certificacionsurtidodetalle [name=descripcion]").val(record.DESCR);

    if (surtidos.length>1) {
        var complete = function(rowdata) {
            seleccionaLoteSurtidoConfirmado(rowdata);
        };
        var cancel = function() {
        };
        seleccionaLoteSurtido(surtidos, complete, cancel);
        return;
    }

    seleccionaLoteSurtidoConfirmado(surtidos[0]);
}

function buscaDetallesSurtidos(codigo) {
    var $grid = $("#grid_detalles");
    var rows = $grid.jqxGrid("getrows");

    var surtidos = [];
    for(var indx=0; indx<rows.length; indx++) {
        var rowdata = $grid.jqxGrid("getrowdata", indx);

        if (rowdata.codigo===codigo) {
            surtidos.push(rowdata);
        }
    }

    return surtidos;
}

function seleccionaLoteSurtidoConfirmado(rowdata) {
    $("#certificacionsurtidodetalle [name=compania]").val(rowdata.compania);
    $("#certificacionsurtidodetalle [name=flsurtido]").val(rowdata.flsurtido);
    $("#certificacionsurtidodetalle [name=partida]").val(rowdata.partida);
    $("#certificacionsurtidodetalle [name=idlote]").val(rowdata.idlote);

    var $lote = $("#certificacionsurtidodetalle [name=lote]");
    $lote.val(rowdata.lote);

    var $fecaducidad = $("#certificacionsurtidodetalle [name=fecaducidad]");
    $fecaducidad.val(getISODate(rowdata.fecaducidad));

    var $certificadas = $("#certificacionsurtidodetalle [name=certificadas]");
    $certificadas.val("0");
    $certificadas.select();
    $certificadas.focus();
}

function aceptaCertificacion() {
    var $form = $("#certificacionsurtidodetalle");

    var pass = validaDatosForma($form);
    if (!pass) {
        notify_error("Hay errores con los datos, favor de corregirlos.");
        return false;
    }

    var valores = getValoresForma("#certificacionsurtidodetalle");
    valores["usuario"] = usuario.usuario;

    var $certificadas = $("#certificacionsurtidodetalle [name=certificadas]");

    var onAceptar = function() {
        $certificadas.select();
        $certificadas.focus();
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
        var msg = "Certifica Surtido aceptado correctamente.";
        notify_success(msg);

        actualizaDetalleCertifica(response);

        limpiarDetalle();
    };

    notify_secondary("Certificando Surtido ...");
    accion("mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO", "certificadas", JSON.stringify(valores),
        onComplete, onError, onFail);
}

function limpiarDetalle() {
    var $contenedor = $("#certificacionsurtidodetalle [name=contenedor]");
    var contenedor = $contenedor.val();

    var $form = $("#certificacionsurtidodetalle");

    limpiaValoresForma($form);

    $contenedor.val(contenedor);

    var $codigo = $("#certificacionsurtidodetalle [name=codigo]");
    $codigo.select();
    $codigo.focus();
}

function actualizaDetalleCertifica(rowdata) {
    var $grid = $("#grid_detalles_certificada");

    $grid.jqxGrid("clearselection");
    var rows = $grid.jqxGrid("getrows");

    for(var indx=0; indx<rows.length; indx++) {
        var _rowdata = $grid.jqxGrid("getrowdata", indx);

        // Si existe, actualizo el que hay
        if (_rowdata.codigo===rowdata.codigo
                && _rowdata.contenedor===rowdata.contenedor
                && _rowdata.lote===rowdata.lote) {

            _rowdata.certificadas = rowdata.certificadas;

            var rowid = $grid.jqxGrid("getrowid", indx);
            var commit = $grid.jqxGrid("updaterow", rowid, _rowdata);
            $grid.jqxGrid("ensurerowvisible", _rowdata.boundindex);

            return;
        }
    }

    // Convierto a Date
    rowdata.fecaducidad = new Date(rowdata.fecaducidad);

    // Agrego uno nuevo
    var commit = $grid.jqxGrid("addrow", null, rowdata, "first");
    $grid.jqxGrid("ensurerowvisible", 0);
}

function confirmarCertificacion() {
    var $flsurtido = $("#ordensurtido-datos [name=flsurtido]");
    if ($flsurtido.val()==="")
        return;

    var rowdata = JSON.parse($flsurtido.attr("data-record"));

    var aceptar = function() {
        confirmarCertificacionConfirmado(rowdata);
    };
    pregunta("&iquest;Realmente desea <b>CONFIRMAR LA CERTIFICACI&Oacute;N</b> de la Orden de Surtido de Pedido <b>["+rowdata.flsurtido+" "+rowdata.pedido+"]</b>?<br>"
        +"&iquest;Esta seguro?", aceptar);
}

function confirmarCertificacionConfirmado(rowdata) {
    var $btn = $("#btn-confirmar");
    $btn.attr("disabled", true);

    var data = {
        id: "ConfirmaCertificacionSurtido",
        compania: rowdata.compania,
        usuario: usuario.usuario,
        flsurtido: rowdata.flsurtido
    };

    var onAceptar = function() {
        $btn.removeAttr("disabled");
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
        var aceptar = function() {
            for (var indx=0; indx<response.contenedores.length; indx++) {
                var params = "compania="+response.ordensurtido.compania
                        +"&flsurtido="+response.ordensurtido.flsurtido
                        +"&contenedor="+response.contenedores[indx].contenedor;
                download("/wms/Reporteador?reporte=OrdenSurtidoPedidoCertificaTicket&"+params);
                //download("/wms/Reporteador?reporte=OrdenSurtidoPedidoCertificaDetalleTicket&"+params);
            }
            window.history.back();
        };
        var msg = "<b>Confirmaci&oacute;n de Certificaci&oacute;n de Surtido Terminada Correctamente.</b>";
        mensaje(msg, aceptar);
        notify_success(msg);
    };

    notify_secondary("Confirmando Certificacion Surtido ...");
    mvc(data, onComplete, onFail, onError);
}

function gurdaCambioLote(){
     var $form = $("#cambioLotesurtidodetalle");

    var pass = validaDatosForma($form);
    if (!pass) {
        notify_error("Hay errores con los datos, favor de corregirlos.");
        return false;
    }

    var valores = getValoresForma("#cambioLotesurtidodetalle");
    valores["usuario"] = usuario.usuario;

    var onAceptar = function() {
        //$certificadas.select();
        //$certificadas.focus();
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
        var msg = "Lote cambiado correctamente.";
        notify_success(msg);

        $("#cambioLotesurtidodetalle [name=compania]").val("");
        $("#cambioLotesurtidodetalle [name=flsurtido]").val(0);
        $("#cambioLotesurtidodetalle [name=partida]").val(0);
        $("#cambioLotesurtidodetalle [name=idlote]").val(0);
        $("#cambioLotesurtidodetalle [name=codigo]").val("");
        $("#cambioLotesurtidodetalle [name=descripcion]").val("");
        $("#cambioLotesurtidodetalle [name=lote]").val(" ");
        $("#cambioLotesurtidodetalle [name=fecaducidad]").val("");            
        obtenDetalles(_rowdata);
        $("#buttonCambiaLote").trigger("click");
        //limpiarDetalle();
    };

    notify_secondary("Cambiando Lote ...");
    accion("mx.reder.wms.dao.entity.OrdenSurtidoPedidoLotesDAO", "cambioLote", JSON.stringify(valores),
        onComplete, onError, onFail);
}

function seleccionaRegistro(){
    //$("[name='lote']").removeAttr("readonly");
    var $grid = $("#grid_detalles");
    var rowindexes = $grid.jqxGrid("getselectedrowindexes");
    if (rowindexes.length===0) {
        var msg = "No ha seleccionado ning&uacute;n contenedor.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }
    
    for(var indx=0; indx<rowindexes.length; indx++) {
        var selectedrowindex = rowindexes[indx];
        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        if (rowdata) {
            $("#cambioLotesurtidodetalle [name=compania]").val(rowdata.compania);
            $("#cambioLotesurtidodetalle [name=flsurtido]").val(rowdata.flsurtido);
            $("#cambioLotesurtidodetalle [name=partida]").val(rowdata.partida);
            $("#cambioLotesurtidodetalle [name=idlote]").val(rowdata.idlote);
            $("#cambioLotesurtidodetalle [name=codigo]").val(rowdata.codigo);
            $("#cambioLotesurtidodetalle [name=descripcion]").val(rowdata.descripcion);
            $("#cambioLotesurtidodetalle [name=lote]").val(rowdata.lote);
            $("#cambioLotesurtidodetalle [name=fecaducidad]").val(getISODate(rowdata.fecaducidad));            
        }
        break;
    }
}

function buscarLotesProducto() {
    var $codigo = $("#cambioLotesurtidodetalle [name=codigo]");
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

        var $descripcion = $("#cambioLotesurtidodetalle [name=descripcion]");
        $descripcion.val(response.DESCR);
        
        var $lote = $("#cambioLotesurtidodetalle [name=lote]");
        $lote.val(response.LOTE);
        
        var $caducidad = $("#cambioLotesurtidodetalle [name=fecaducidad]");
        $caducidad.val(getISODate(response.FCHCADUC));

        $("#cambioLotesurtidodetalle [name=lote]").focus();
        
        notify_info("Listo.");
    };

    busquedaLotesProductosASPEL(usuario.compania, codigo, onComplete, onError);
}