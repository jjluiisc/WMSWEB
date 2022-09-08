
function initReporte() {
    notify_info("Listo.");
}

function validaParametrosReporte() {
    var $form = $("#parametros");
    var pass = validaDatosForma($form);
    if (!pass)
        notify_error("Hay errores con los par&aacute;metros, favor de corregirlosp.");
    return pass;
}

function getWhereOrdenesSurtido() {
    var parametros = getValoresForma("#parametros");
    parametros["compania"] = usuario.compania;
    parametros["usuario"] = usuario.usuario;

    var whereOrdenesSurtido = "";
    whereOrdenesSurtido += "osp.compania = '"+parametros.compania+"' AND osp.status != 'CA' ";
    if (parametros.fechainicial!=="")
        whereOrdenesSurtido += "AND osp.fechapedido >= '"+parametros.fechainicial+"' ";
    if (parametros.fechafinal!=="")
        whereOrdenesSurtido += "AND osp.fechapedido <= '"+parametros.fechafinal+" 23:59:59' ";
    if (parametros.pedido!=="")
        whereOrdenesSurtido += "AND osp.pedido LIKE '%"+parametros.pedido+"%' ";
    if (parametros.clavecliente!=="")
        whereOrdenesSurtido += "AND osp.cliente LIKE '%"+parametros.cliente+"%' ";
    return whereOrdenesSurtido;
}

function verDatos() {
    if (!validaParametrosReporte())
        return;

    var reporte = $("input[name='reporte']:checked").val();

    var parametros;

    if (reporte==="CertificacionSurtidoTiempos") {
        parametros = {
            coleccion: "mx.reder.wms.collection.OrdenesSurtidoPedidoCollection",
            where: getWhereOrdenesSurtido(),
            height: 370,
            title: "Por Orden de Surtido",
            datafields: [
                {name: "compania", type: "string"},
                {name: "flsurtido", type: "number"},
                {name: "pedido", type: "string"},
                {name: "status", type: "string"},
                {name: "fechastatus", type: "date"},
                {name: "usuario", type: "string"},
                {name: "equipo", type: "string"},
                {name: "usuario", type: "string"},
                {name: "surtidor", type: "string"},
                {name: "fechapedido", type: "date"},
                {name: "cliente", type: "string"},
                {name: "nombrecliente", type: "string"},
                {name: "vendedor", type: "string"},
                {name: "ruta", type: "string"},
                {name: "fechasurtido", type: "date"},
                {name: "fechacertificando", type: "date"},
                {name: "fechaconfirmada", type: "date"},
                {name: "cantidad", type: "number"},
                {name: "surtidas", type: "number"},
                {name: "certificadas", type: "number"},
                {name: "total", type: "number"},
                {name: "detalles", type: "number"},
                {name: "fechaconfirmada", type: "date"},
                {name: "fechafacturada", type: "date"}
            ],
            columns: [
                {text: "Surtido", datafield: "flsurtido", width: "10%", cellsalign: "right"},
                {text: "Pedido", datafield: "pedido", width: "10%"},
                {text: "No.Cliente", datafield: "cliente", width: "10%"},
                {text: "Cliente", datafield: "nombrecliente", width: "30%"},
                {text: "Surtidas", datafield: "surtidas", width: "10%", cellsalign: "right"},
                {text: "Certificadas", datafield: "certificadas", width: "10%", cellsalign: "right"},
                {text: "Usuario", datafield: "usuario", width: "15%"},
                {text: "Inicio", datafield: "fechacertificando", width: "15%", cellsformat: "d/M/yyyy HH:mm:ss"},
                {text: "Fin", datafield: "fechaconfirmada", width: "15%", cellsformat: "d/M/yyyy HH:mm:ss"},
                {text: "Tiempo (seg)", datafield: "tiemposurtido", width: "10%", cellsalign: "right", cellsrenderer:
                    function (row, columnname, value, defaulthtml, columnproperties, rowdata) {
                        var diff = !rowdata.fechacertificando || !rowdata.fechaconfirmada ? 0 : rowdata.fechaconfirmada.getTime() - rowdata.fechacertificando.getTime();
                        var value = Math.floor(diff / 1000);
                        return "<div style=\"margin:4px; margin-top:8px; text-align:"+columnproperties.cellsalign+";\">"+value+"</div>";
                    }},
                {text: "T.Promedio (piezas/seg)", datafield: "promediosurtido", width: "15%", cellsalign: "right", cellsrenderer:
                    function (row, columnname, value, defaulthtml, columnproperties, rowdata) {
                        var diff = !rowdata.fechacertificando || !rowdata.fechaconfirmada ? 0 : rowdata.fechaconfirmada.getTime() - rowdata.fechacertificando.getTime();
                        var value = rowdata.certificadas===0 ? 0 : Math.floor(diff / 1000) / rowdata.certificadas;
                        value = redondea(value);
                        return "<div style=\"margin:4px; margin-top:8px; text-align:"+columnproperties.cellsalign+";\">"+value+"</div>";
                    }}
            ]
        };
    }

    var onFail = function(err) {
        var mensaje = "Error al buscar el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        notify_error(mensaje);
    };
    var onError = function(response) {
        if (response.exception.indexOf("WebException")!==-1) {
            notify_warning(response.mensaje);
        } else {
            notify_error(response.exception);
        }
    };
    var onComplete = function(response) {
        dibujaTablaDatos(parametros, response);
        notify_info("Listo.");
    };
    coleccion(parametros.coleccion, parametros.where, onComplete, onError, onFail);
}

function dibujaTablaDatos(parametros, response) {
    var source = {
        localdata: response,
        datafields: parametros.datafields,
        datatype: "array"
    };
    var dataAdapter = new $.jqx.dataAdapter(source);
    var $grid = $("<div id=\"grid_datos\"></div>");
    $grid.jqxGrid({
        width: "100%",
        height: parametros.height,
        altrows: true,
        sortable: true,
        columnsresize: true,
        source: dataAdapter,
        columns: parametros.columns
    });

    var $datos = $("#datos");
    $datos.empty();
    $datos.append($grid);
}

function generarReporte() {
    if (!validaParametrosReporte())
        return;

    var parametros = getValoresForma("#parametros");
    parametros["compania"] = usuario.compania;
    parametros["usuario"] = usuario.usuario;

    var reporte = $("input[name='reporte']:checked").val();

    notify_secondary("Generando Reporte ...");

    var complete = function() {
        notify_success("Reporte Generado.");
    };
    download("/wms/ExportadorExcel?export="+reporte+getValoresParametros(parametros), complete);
}

function imprimeTicket() {
    
    var $grid = $("#grid_datos");
    var rowindexes = $grid.jqxGrid("getselectedrowindexes");
    if (rowindexes.length===0) {
        var msg = "No ha seleccionado ning&uacute;n pedido.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }
    
    //var pedidos = "";
    //var registros = "";
    var s_flsurtido = "";
    for(var indx=0; indx<rowindexes.length; indx++) {
        var selectedrowindex = rowindexes[indx];
        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        if (rowdata) {
            //pedidos = pedidos+" "+rowdata.CVE_DOC+";";
            //registros = registros+rowdata.CVE_DOC+"|";
            s_flsurtido = rowdata.flsurtido;
        }
        break;
    }
    
    var aceptar = function() {
        
        var params = "compania="+usuario.compania
                +"&flsurtido="+s_flsurtido
        download("/wms/Reporteador?reporte=OrdenSurtidoPedidoCertificaTicket&"+params);
        download("/wms/Reporteador?reporte=OrdenSurtidoPedidoCertificaDetalleTicket&"+params);
        
        window.history.back();
    };
    var msg = "<b>Confirmaci&oacute;n de Certificaci&oacute;n de Surtido Terminada Correctamente.</b>";
    mensaje(msg, aceptar);
    notify_success(msg);
}