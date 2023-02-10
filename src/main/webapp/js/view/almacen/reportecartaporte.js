
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

function getWhereCartasPorte() {
    var parametros = getValoresForma("#parametros");
    parametros["compania"] = usuario.compania;
    parametros["usuario"] = usuario.usuario;

    var whereCartasPorte = "";
    whereCartasPorte += "CPF.compania = '"+parametros.compania+"' ";
    if (parametros.fechainicial!=="")
        whereCartasPorte += "AND CPCFDI.fechatimbre >= '"+parametros.fechainicial+"' ";
    if (parametros.fechafinal!=="")
        whereCartasPorte += "AND CPCFDI.fechatimbre <= '"+parametros.fechafinal+" 23:59:59' ";
    return whereCartasPorte;
}

function verDatos() {
    if (!validaParametrosReporte())
        return;

    var parametros;

    parametros = {
        coleccion: "mx.reder.wms.collection.CartasPorteCollection",
        where: getWhereCartasPorte(),
        height: 370,
        title: "Cartas porte por fecha",
        datafields: [
            {name: "compania", type: "string"},
            {name: "idcartaporte", type: "number"},
            {name: "fechatimbre", type: "date"},
            {name: "uuid", type: "string"},
            {name: "factura", type: "string"}
        ],
        
        columns: [
            {text: "Carta porte", datafield: "idcartaporte", width: "10%", cellsalign: "right"},
            {text: "Fecha timbre", datafield: "fechatimbre", width: "18%", cellsformat: "d/M/yyyy HH:mm:ss"},
            {text: "UUID", datafield: "uuid", width: "34%"},
            {text: "Factura", datafield: "factura", width: "16%"}
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

    var parametros = getValoresForma("#parametros");
    parametros["compania"] = usuario.compania;
    parametros["usuario"] = usuario.usuario;
    
    var reporte = "CartaPorte";
    
    notify_secondary("Generando Reporte ...");

    var complete = function() {
        notify_success("Reporte Generado.");
    };
    download("/wms/ExportadorExcel?export="+reporte+getValoresParametros(parametros), complete);
}
