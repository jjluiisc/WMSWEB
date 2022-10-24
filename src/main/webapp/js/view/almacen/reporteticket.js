
function initReporte() {
    notify_info("Listo.");
    
    document.querySelectorAll('input[type=text]').forEach(node => node.addEventListener('keypress', e => {
            if (e.keyCode == 13) {
                e.preventDefault();
            }
        }));
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
    whereOrdenesSurtido += "OSPC.compania = '"+parametros.compania+"' AND OSP.status != 'CA' ";
    if (parametros.contenedor!=="")
        whereOrdenesSurtido += "AND OSPC.contenedor = '"+parametros.contenedor+"' ";
    return whereOrdenesSurtido;
}

function verDatos() {
    if (!validaParametrosReporte())
        return;

    var parametros;

    parametros = {
            coleccion: "mx.reder.wms.collection.OrdenesSurtidoTicketCollection",
            where: getWhereOrdenesSurtido(),
            height: 370,
            title: "Por Orden de Surtido",
            datafields: [
                {name: "compania", type: "string"},
                {name: "flsurtido", type: "number"},
                {name: "contenedor", type: "string"},
                {name: "pedido", type: "string"},
                {name: "cliente", type: "string"},
                {name: "nombrecliente", type: "string"},
                {name: "ruta", type: "string"}
            ],
            columns: [
                {text: "Surtido", datafield: "flsurtido", width: "10%", cellsalign: "right"},
                {text: "Contenedor", datafield: "contenedor", width: "10%"},
                {text: "Pedido", datafield: "pedido", width: "10%"},
                {text: "No.Cliente", datafield: "cliente", width: "10%"},
                {text: "Cliente", datafield: "nombrecliente", width: "30%"},
                {text: "Ruta", datafield: "ruta", width: "10%"}
            ]
        };

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
    
    var $grid = $("#grid_datos");
    var rowindexes = $grid.jqxGrid("getselectedrowindexes");
    if (rowindexes.length===0) {
        var msg = "No ha seleccionado ning&uacute;n contenedor.";
        precaucion(msg);
        notify_warning(msg);
        return;
    }
    
    var s_flsurtido = "", s_contenedor = "";
    for(var indx=0; indx<rowindexes.length; indx++) {
        var selectedrowindex = rowindexes[indx];
        var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);
        if (rowdata) {
            s_flsurtido = rowdata.flsurtido;
            s_contenedor = rowdata.contenedor;
        }
        break;
    }
    
    var aceptar = function() {
        
        var params = "compania="+usuario.compania
                +"&flsurtido="+s_flsurtido
                +"&contenedor="+s_contenedor
        download("/wms/Reporteador?reporte=OrdenSurtidoPedidoCertificaTicket&"+params);
        //download("/wms/Reporteador?reporte=OrdenSurtidoPedidoCertificaDetalleTicket&"+params);
        
        window.history.back();
    };
    var msg = "<b>Descargar Ticket.</b>";
    mensaje(msg, aceptar);
    notify_success(msg);

}