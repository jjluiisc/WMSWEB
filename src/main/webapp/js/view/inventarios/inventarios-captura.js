
function initCaptura(flinventario) {
    var $flinventario = $("#form-inventario [name=flinventario]");
    $flinventario.val(flinventario);

    initGridDetalles();
    deshabilitaTodo();
    buscaCaptura();

    var $codigo = $("#form-productos [name=codigo]");
    var complete = function(record) {
        tengoProducto(record);
    };
    autoBusquedaCodigo($codigo, complete);

    var $cantidad = $("#form-productos [name=cantidad]");
    $cantidad.keypress(function(e) {
        if (e.keyCode===13) {
            agregarDetalle();
        }
    });

    notify_info("Listo.");
}

function initGridDetalles() {
    var parametros = {
        datafields: [
            {name: "fldcapturainventario", type: "int"},
            {name: "compania", type: "string"},
            {name: "flcapturainventario", type: "int"},
            {name: "flinventario", type: "int"},
            {name: "codigo", type: "string"},
            {name: "descripcion", type: "string"},
            {name: "ubicacion", type: "string"},
            {name: "cantidad", type: "number"}
        ],
        columns: [
            {text: "Folio", datafield: "flinventario", width: "5%"},
            {text: "Codigo", datafield: "codigo", width: "20%"},
            {text: "Descripci&oacute;n", datafield: "descripcion", width: "45%"},
            {text: "Ubicaci&oacute;n", datafield: "ubicacion", width: "20%"},
            {text: "Cantidad", datafield: "cantidad", width: "10%", cellsalign: "right"}
        ]
    };

    var source = {
        datafields: parametros.datafields,
        datatype: "array"
    };
    var dataAdapter = new $.jqx.dataAdapter(source);
    var $grid = $("#grid-detalles");
    $grid.jqxGrid({
        width: "100%",
        height: 400,
        altrows: true,
        sortable: true,
        source: dataAdapter,
        columns: parametros.columns
    });
}

function deshabilitaTodo() {
    $("#inventario :input").attr("disabled", true);
    $("#inventario-comandos :button").attr("disabled", true);
    $("#productos :input").attr("disabled", true);
    $("#detalles :input").attr("disabled", true);
    $("#detalles-comandos :button").attr("disabled", true);
}

function habilitaCampos(response) {
    if (response.status==="PE") {
        $("#btn-eliminar").removeAttr("disabled");
        $("#btn-imprimir").removeAttr("disabled");
        $("#btn-terminar").removeAttr("disabled");
        $("#productos :input").removeAttr("disabled");
        $("#detalles :input").removeAttr("disabled");
        $("#detalles-commandos :button").removeAttr("disabled");
        $("#btn-agregar-detalle").removeAttr("disabled");
        $("#btn-eliminar-detalle").removeAttr("disabled");
    }
    else if (response.status==="BO") {
        $("#btn-crear").removeAttr("disabled");
    }
    else if (response.status==="TE") {
        $("#btn-crear").removeAttr("disabled");
        $("#btn-imprimir").removeAttr("disabled");
    }
}

function verificaInventario(response) {
    pasaValoresForma("#form-inventario", response);
    habilitaCampos(response);
    notify_success("Listo.");
}

function limpiaDetalles() {
    var $grid = $("#grid-detalles");
    $grid.jqxGrid("clear");
}

function cargaDetalles(response) {
    var $flcapturainventario = $("#form-inventario [name=flcapturainventario]");
    var flcapturainventario = $flcapturainventario.val();

    var onAceptar = function() {
    };
    var onFail = function(err) {
        var msg = "Error al ejecutar la accion en el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        var $grid = $("#grid-detalles");
        for(var index=0; index<response.length; index++) {
            var rowdata = response[index];
            var commit = $grid.jqxGrid("addrow", null, rowdata);
        }
        notify_info("Listo.");
    };
    notify_secondary("Buscando los registros de Inventario Captura ...");
    lista("mx.com.kimberlyclark.wms.dao.entity.InventarioCapturaDetalleDAO",
        "compania = '"+usuario.compania+"' AND flcapturainventario = "+flcapturainventario+" AND status = 'PE'",
        "fldcapturainventario DESC",
        onComplete, onError, onFail);
}

function buscaCaptura() {
    var $flinventario = $("#form-inventario [name=flinventario]");
    var flinventario = $flinventario.val();

    var valores = {
        compania: usuario.compania,
        flinventario: flinventario
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
            if (response.mensaje==="No existe una Captura de Inventario [PE] en esta compania.") {
                $("#btn-crear").removeAttr("disabled");
            }
        } else {
            error(response.exception, onAceptar);
            notify_error(response.exception);
        }
    };
    var onComplete = function(response) {
        verificaInventario(response);
        cargaDetalles(response);
    };
    notify_info("Buscando el registro de Inventario ...");
    accion("mx.com.kimberlyclark.wms.dao.entity.InventarioCapturaDAO", "busca", JSON.stringify(valores),
        onComplete, onError, onFail);
}

function crearCaptura() {
    var $btncrear = $("#btn-crear");

    var $flinventario = $("#form-inventario [name=flinventario]");
    var flinventario = $flinventario.val();

    var valores = {
        compania: usuario.compania,
        flinventario: flinventario,
        usuario: usuario.usuario
    };

    var onAceptar = function() {
        $btncrear.focus();
    };
    var onFail = function(err) {
        var msg = "Error al ejecutar la accion en el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        verificaInventario(response);
        limpiaDetalles();
    };
    notify_info("Creando el registro de Captura de Inventario ...");
    accion("mx.com.kimberlyclark.wms.dao.entity.InventarioCapturaDAO", "crea", JSON.stringify(valores),
        onComplete, onError, onFail);
}

function borrarCaptura() {
    var aceptar = function() {
        borrarCapturaConfirmado();
    };
    pregunta("&iquest;Realmente desea <b>borrar</b> la captura?", aceptar);
}

function borrarCapturaConfirmado() {
    var $btneliminar = $("#btn-eliminar");

    var $flcapturainventario = $("#form-inventario [name=flcapturainventario]");
    var flcapturainventario = $flcapturainventario.val();

    var valores = {
        compania: usuario.compania,
        flcapturainventario: flcapturainventario,
        usuario: usuario.usuario
    };

    var onAceptar = function() {
        $btneliminar.focus();
    };
    var onFail = function(err) {
        var msg = "Error al ejecutar la accion en el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        deshabilitaTodo();
        verificaInventario(response);
    };
    notify_info("Eliminando el registro de Captura de Inventario ...");
    accion("mx.com.kimberlyclark.wms.dao.entity.InventarioCapturaDAO", "borra", JSON.stringify(valores),
        onComplete, onError, onFail);
}

function terminarCaptura() {
    var aceptar = function() {
        terminarCapturaConfirmado();
    };
    pregunta("&iquest;Realmente desea <b>terminar</b> la captura?", aceptar);
}

function terminarCapturaConfirmado() {
    var $btnterminar = $("#btn-terminar");

    var $flcapturainventario = $("#form-inventario [name=flcapturainventario]");
    var flcapturainventario = $flcapturainventario.val();

    var valores = {
        compania: usuario.compania,
        flcapturainventario: flcapturainventario,
        usuario: usuario.usuario
    };

    var onAceptar = function() {
        $btnterminar.focus();
    };
    var onFail = function(err) {
        var msg = "Error al ejecutar la accion en el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        deshabilitaTodo();
        verificaInventario(response);
    };
    notify_info("Eliminando el registro de Captura de Inventario ...");
    accion("mx.com.kimberlyclark.wms.dao.entity.InventarioCapturaDAO", "termina", JSON.stringify(valores),
        onComplete, onError, onFail);
}

function tengoProducto(record) {
    $("#form-productos [name=codigo]").val(record.codigo);
    $("#form-productos [name=descripcion]").val(record.descripcion);

    $("#form-productos [name=cantidad]").focus();

    notify_info("Listo.");
}

function validaDatosProducto() {
    var $form = $("#form-productos");
    var pass = validaDatosForma($form);
    if (!pass)
        notify_error("Hay errores con los datos, favor de corregirlos.");
    return pass;
}

function agregarDetalle() {
    if (!validaDatosProducto())
        return;

    var valores = getValoresForma("#form-productos");

    var $btnagregardetalle = $("#btn-agregar-detalle");

    var $flcapturainventario = $("#form-inventario [name=flcapturainventario]");
    var flcapturainventario = $flcapturainventario.val();
    var $flinventario = $("#form-inventario [name=flinventario]");
    var flinventario = $flinventario.val();

    valores["compania"] = usuario.compania;
    valores["flcapturainventario"] = flcapturainventario;
    valores["flinventario"] = flinventario;
    valores["cantidad"] = parseFloat(valores.cantidad);

    var onAceptar = function() {
        $btnagregardetalle.focus();
    };
    var onFail = function(err) {
        var msg = "Error al ejecutar la accion en el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        var $grid = $("#grid-detalles");
        var rowdata = response;

        var commit = $grid.jqxGrid("addrow", null, rowdata, "first");
        $grid.jqxGrid("ensurerowvisible", 0);
        $grid.jqxGrid("clearselection");

        var $form = $("#form-productos");
        limpiaValoresForma($form);

        $("#form-productos [name=ubicacion]").val(rowdata.ubicacion);
        $("#form-productos [name=codigo]").focus();

        notify_info("Listo.");
    };
    notify_secondary("Creando el registro de Detalle de Captura de Inventario ...");
    accion("mx.com.kimberlyclark.wms.dao.entity.InventarioCapturaDetalleDAO", "agrega", JSON.stringify(valores),
        onComplete, onError, onFail);
}

function eliminarDetalle() {
    var $grid = $("#grid-detalles");
    var selectedrowindex = $grid.jqxGrid("selectedrowindex");
    if (selectedrowindex===-1) {
        notify_warning("Debe de seleccionar un rengl&oacute;n.");
        return;
    }

    var rowdata = $grid.jqxGrid("getrowdata", selectedrowindex);

    var cancelar = function() {
        $grid.jqxGrid("clearselection");
    };
    var aceptar = function() {
        eliminarDetalleConfirmado(rowdata, selectedrowindex);
    };
    pregunta("&iquest;Realmente desea borrar el registro <b>"
            +(rowdata.ubicacion!=="" ? rowdata.ubicacion+" - " : "")
            +rowdata.descripcion+"</b>?", aceptar, cancelar);
}

function eliminarDetalleConfirmado(rowdata, rowindex) {
    var $btneliminardetalle = $("#btn-eliminar-detalle");

    var onAceptar = function() {
        $btneliminardetalle.focus();
    };
    var onFail = function(err) {
        var msg = "Error al ejecutar la accion en el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        var $grid = $("#grid-detalles");
        var rowid = $grid.jqxGrid("getrowid", rowindex);
        $grid.jqxGrid("deleterow", rowid);

        $("#form-productos [name=codigo]").focus();

        notify_info("Listo.");
    };
    notify_info("Eliminando el registro de Detalle de Captura de Inventario ...");
    accion("mx.com.kimberlyclark.wms.dao.entity.InventarioCapturaDetalleDAO", "elimina", JSON.stringify(rowdata),
        onComplete, onError, onFail);
}
