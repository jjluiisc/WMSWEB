
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
    whereOrdenesSurtido += "os.compania = '"+parametros.compania+"' AND os.status = 'CA' ";
    if (parametros.fechainicial!=="")
        whereOrdenesSurtido += "AND os.fechapedido >= '"+parametros.fechainicial+"' ";
    if (parametros.fechafinal!=="")
        whereOrdenesSurtido += "AND os.fechapedido <= '"+parametros.fechafinal+" 23:59:59' ";
    if (parametros.pedido!=="")
        whereOrdenesSurtido += "AND os.pedido LIKE '%"+parametros.pedido+"%' ";
    if (parametros.clavecliente!=="")
        whereOrdenesSurtido += "AND os.cliente LIKE '%"+parametros.cliente+"%' ";
    return whereOrdenesSurtido;
}

function verDatos() {
    if (!validaParametrosReporte())
        return;

    var reporte = $("input[name='reporte']:checked").val();

    var parametros;

    if (reporte==="OrdenesSurtidoPedidoCancelados") {
        parametros = {
            coleccion: "mx.reder.wms.collection.OrdenesSurtidoPedidoCanceladosCollection",
            where: getWhereOrdenesSurtido(),
            height: 370,
            title: "Por Orden de Surtido",
            datafields: [
                {name: "compania", type: "string"},
                {name: "status", type: "string"},
                {name: "fechapedido", type: "date"},
                {name: "fechasurtido", type: "date"},
                {name: "fechacancelacion", type: "date"},
                {name: "flsurtido", type: "number"},
                {name: "pedido", type: "string"},
                {name: "ruta", type: "string"},
                {name: "cliente", type: "string"},
                {name: "nombrecliente", type: "string"},
                {name: "vendedor", type: "string"},
                {name: "surtidor", type: "string"},
                {name: "motivocancelacion", type: "string"}
            ],
            columns: [
                {text: "Fecha de OS ", datafield: "fechapedido", width: "20%", cellsformat: "d/M/yyyy HH:mm:ss"},
                {text: "Fecha Cancelacion", datafield: "fechacancelacion", width: "20%", cellsformat: "d/M/yyyy HH:mm:ss"},
                {text: "Folio OS", datafield: "flsurtido", width: "15%", cellsalign: "right"},
                {text: "Documento", datafield: "pedido", width: "15%"},
                {text: "No.Cliente", datafield: "cliente", width: "10%"},
                {text: "No.Vendedor", datafield: "vendedor", width: "10%"},
                {text: "Surtidor", datafield: "surtidor", width: "10%"},
                {text: "Motivo Cancelacion", datafield: "motivocancelacion", width: "20%"}
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