
function login() {
    function onComplete() {
        function onCompleteII() {
            initLogin();
        };
        loadScript("/wms/js/view/login.js", onCompleteII);
    };

    loadMainPage("/wms/view/login.html", onComplete);
}

function main() {
    var state = {
        func: "_main"
    };
    pushState(state);

    _main();
}

function _main() {
    function onComplete() {
        function onCompleteII() {
            initMain();
        };
        loadScript("/wms/js/view/main.js", onCompleteII);
    };

    loadMainPage("/wms/view/main.html", onComplete);
}

function verLog() {
    var state = {
        func: "_verLog"
    };
    pushState(state);

    _verLog();
}

function _verLog() {
    if (!validaPermiso("verLog"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initLog();
        };
        loadScript("/wms/js/view/util/log.js", onCompleteII);
    };

    loadMainPage("/wms/view/util/log.html", onComplete);
}

function catalogoPermisos() {
    var state = {
        func: "_catalogoPermisos"
    };
    pushState(state);

    _catalogoPermisos();
}

function _catalogoPermisos() {
    if (!validaPermiso("catalogoPermisos"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCatalogo();
        };
        loadScript("/wms/js/view/catalogos/permisos.js", onCompleteII);
    };

    loadMainPage("/wms/view/catalogos/permisos.html", onComplete);
}

function catalogoPerfiles() {
    var state = {
        func: "_catalogoPerfiles"
    };
    pushState(state);

    _catalogoPerfiles();
}

function _catalogoPerfiles() {
    if (!validaPermiso("catalogoPerfiles"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCatalogo();
        };
        loadScript("/wms/js/view/catalogos/perfiles.js", onCompleteII);
    };

    loadMainPage("/wms/view/catalogos/perfiles.html", onComplete);
}

function catalogoUsuarios() {
    var state = {
        func: "_catalogoUsuarios"
    };
    pushState(state);

    _catalogoUsuarios();
}

function _catalogoUsuarios() {
    if (!validaPermiso("catalogoUsuarios"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCatalogo();
        };
        loadScript("/wms/js/view/catalogos/usuarios.js", onCompleteII);
    };

    loadMainPage("/wms/view/catalogos/usuarios.html", onComplete);
}

function catalogoSurtidores() {
    var state = {
        func: "_catalogoSurtidores"
    };
    pushState(state);

    _catalogoSurtidores();
}

function _catalogoSurtidores() {
    if (!validaPermiso("catalogoSurtidores"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCatalogo();
        };
        loadScript("/wms/js/view/catalogos/surtidores.js", onCompleteII);
    };

    loadMainPage("/wms/view/catalogos/surtidores.html", onComplete);
}

function catalogoClavesAlternas() {
    var state = {
        func: "_catalogoClavesAlternas"
    };
    pushState(state);

    _catalogoClavesAlternas();
}

function _catalogoClavesAlternas() {
    if (!validaPermiso("catalogoClavesAlternas"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCatalogo();
        };
        loadScript("/wms/js/view/catalogos/clavesalternas.js", onCompleteII);
    };

    loadMainPage("/wms/view/catalogos/clavesalternas.html", onComplete);
}

function catalogoMotivoCancelacion() {
    var state = {
        func: "_catalogoMotivoCancelacion"
    };
    pushState(state);

    _catalogoMotivoCancelacion();
}

function _catalogoMotivoCancelacion() {
    if (!validaPermiso("catalogoMotivoCancelacion"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCatalogo();
        };
        loadScript("/wms/js/view/catalogos/motivoscancelacion.js", onCompleteII);
    };

    loadMainPage("/wms/view/catalogos/motivoscancelacion.html", onComplete);
}

function catalogoDirecciones(direccion) {
    var state = {
        func: "_catalogoDirecciones",
        data: {
            direccion: direccion
        }
    };
    pushState(state);

    _catalogoDirecciones(direccion);
}

function _catalogoDirecciones(direccion) {
    if (!validaPermiso("catalogoDirecciones"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCatalogo(direccion);
        };
        loadScript("/wms/js/view/catalogos/direcciones.js", onCompleteII);
    };

    loadMainPage("/wms/view/catalogos/direcciones.html", onComplete);
}

function catalogoCompania() {
    var state = {
        func: "_catalogoCompania"
    };
    pushState(state);

    _catalogoCompania();
}

function _catalogoCompania() {
    if (!validaPermiso("catalogoCompania"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCatalogo();
        };
        loadScript("/wms/js/view/catalogos/companias.js", onCompleteII);
    };

    loadMainPage("/wms/view/catalogos/companias.html", onComplete);
}

function asignaSurtido() {
    var state = {
        func: "_asignaSurtido"
    };
    pushState(state);

    _asignaSurtido();
}

function _asignaSurtido() {
    if (!validaPermiso("asignaSurtido"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initPedidos();
        };
        loadScript("/wms/js/view/almacen/asignasurtido.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/asignasurtido.html", onComplete);
}

function ordenesSurtido() {
    var state = {
        func: "_ordenesSurtido"
    };
    pushState(state);

    _ordenesSurtido();
}

function _ordenesSurtido() {
    if (!validaPermiso("ordenesSurtido"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initOrdenesSurtido();
        };
        loadScript("/wms/js/view/almacen/ordenessurtido.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/ordenessurtido.html", onComplete);
}

function reporteOrdenesSurtido() {
    var state = {
        func: "_reporteOrdenesSurtido"
    };
    pushState(state);

    _reporteOrdenesSurtido();
}

function _reporteOrdenesSurtido() {
    if (!validaPermiso("reporteOrdenesSurtido"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initReporte();
        };
        loadScript("/wms/js/view/almacen/reportesurtido.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/reportesurtido.html", onComplete);
}

function reporteOrdenesSurtidoCanceladas() {
    var state = {
        func: "_reporteOrdenesSurtidoCanceladas"
    };
    pushState(state);

    _reporteOrdenesSurtidoCanceladas();
}

function _reporteOrdenesSurtidoCanceladas() {
    if (!validaPermiso("reporteOrdenesSurtidoCanceladas"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initReporte();
        };
        loadScript("/wms/js/view/almacen/reportesurtidocanceladas.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/reportesurtidocanceladas.html", onComplete);
}

function certificacionSurtido() {
    var state = {
        func: "_certificacionSurtido"
    };
    pushState(state);

    _certificacionSurtido();
}

function _certificacionSurtido() {
    if (!validaPermiso("certificacionSurtido"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initOrdenesSurtido();
        };
        loadScript("/wms/js/view/almacen/certificacionsurtido.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/certificacionsurtido.html", onComplete);
}

function certificacionSurtidoCaptura(rowdata) {
    var state = {
        func: "_certificacionSurtidoCaptura",
        data: rowdata
    };
    pushState(state);

    _certificacionSurtidoCaptura(rowdata);
}

function _certificacionSurtidoCaptura(rowdata) {
    if (!validaPermiso("certificacionSurtidoCaptura"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCertificacionSurtido(rowdata);
        };
        loadScript("/wms/js/view/almacen/certificacionsurtidocaptura.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/certificacionsurtidocaptura.html", onComplete);
}

function reporteCertificacion() {
    var state = {
        func: "_reporteCertificacion"
    };
    pushState(state);

    _reporteCertificacion();
}

function _reporteCertificacion() {
    if (!validaPermiso("reporteCertificacion"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initReporte();
        };
        loadScript("/wms/js/view/almacen/reportecertificacion.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/reportecertificacion.html", onComplete);
}

function reporteTicket() {
    var state = {
        func: "_reporteTicket"
    };
    pushState(state);

    _reporteTicket();
}

function _reporteTicket() {
    if (!validaPermiso("reporteTicket"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initReporte();
        };
        loadScript("/wms/js/view/almacen/reporteticket.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/reporteticket.html", onComplete);
}

function reporteCartaPorte() {
    var state = {
        func: "_reporteCartaPorte"
    };
    pushState(state);

    _reporteCartaPorte();
}

function _reporteCartaPorte() {
    if (!validaPermiso("reporteCartaPorte"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initReporte();
        };
        loadScript("/wms/js/view/almacen/reportecartaporte.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/reportecartaporte.html", onComplete);
}

function rutas() {
    var state = {
        func: "_rutas"
    };
    pushState(state);

    _rutas();
}

function _rutas() {
    if (!validaPermiso("rutas"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initFacturar();
        };
        loadScript("/wms/js/view/almacen/rutas.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/rutas.html", onComplete);
}

function cartaPorte() {
    var state = {
        func: "_cartaPorte"
    };
    pushState(state);

    _cartaPorte();
}

function _cartaPorte() {
    if (!validaPermiso("cartaPorte"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCaptura();
        };
        loadScript("/wms/js/view/almacen/cartaporte.js", onCompleteII);
    };

    loadMainPage("/wms/view/almacen/cartaporte.html", onComplete);
}

function capturaInventario() {
    var state = {
        func: "_capturaInventario"
    };
    pushState(state);

    _capturaInventario();
}

function _capturaInventario() {
    if (!validaPermiso("capturaInventario"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initCaptura();
        };
        loadScript("/wms/js/view/inventarios/inventarios.js", onCompleteII);
    };

    loadMainPage("/wms/view/inventarios/inventarios.html", onComplete);
}

function analisisInventarios() {
    var state = {
        func: "_analisisInventarios"
    };
    pushState(state);

    _analisisInventarios();
}

function _analisisInventarios() {
    if (!validaPermiso("analisisInventarios"))
        return false;

    function onComplete() {
        function onCompleteII() {
            initAnalisisInventario();
        };
        loadScript("/wms/js/view/inventarios/analisis.js", onCompleteII);
    };

    loadMainPage("/wms/view/inventarios/analisis.html", onComplete);
}

//
//
//

function validaPermiso(permiso) {
    $("#navbarTogglerHeader").collapse("hide");

    if (!usuario.permisos.includes(permiso)) {
        error("Usted no tiene permiso para esta opci&oacute;n \"<b>"+permiso+"</b>\".");
        return false;
    }

    return true;
}

function cerrarSesion() {
    var aceptar = function() {
        var data = {
            id: "IniciaSesion",
            cerrar: "OK"
        };
        var done = function(response) {
            top.location = "/wms";
        };
        var fail = function(err) {
            error("Error al Cerrar la Sesi&oacute;n.<br><br><b>("+err.status+") "+err.statusText+"</b>");
        };
        mvc(data, done, fail);
    };

    pregunta("&iquest;Realmente desea cerrar la sesi&oacute;n?", aceptar);
}

function ejecutaReporte(reporte, params) {
    $modalDialogBusquedas.modal("hide");
    dowload("/wms/Reporteador?reporte="+reporte+"&"+params);
}

function ejecutaReporteExcel(reporte, params) {
    $modalDialogBusquedas.modal("hide");
    download("/wms/ExportadorExcel?export="+reporte+"&"+params);
}

function abreOpciones(titulo, opciones) {
    var $div = $("<div></div>");

    $.each(opciones, function(n, elem) {
        $div.append("<div class=\"reporte\"><button type=\"button\" class=\"btn btn-outline-secondary reporte-button\" "
            +"onclick=\""+elem.funcion+"\">"+elem.nombre+"</button></div>");
    });

    var $buttonAceptar = $("<button type=\"button\" class=\"btn btn-outline-success\">Aceptar</button>");
    $buttonAceptar.click(function() {
        $modalDialogBusquedas.modal("hide");
    });
    var $buttonCancelar = $("<button type=\"button\" class=\"btn btn-outline-secondary\">Cancelar</button>");
    $buttonCancelar.click(function() {
        $modalDialogBusquedas.modal("hide");
    });

    $("#modalDialogBusquedasLabel").html("Reportes");
    $("#modalDialogBusquedasDocument").addClass("modal-md");
    $("#modalDialogBusquedasHeader").removeClass();
    $("#modalDialogBusquedasHeader").addClass("modal-header bg-info text-white");
    $("#modalDialogBusquedasBody").empty();
    $("#modalDialogBusquedasBody").append($div);
    $("#modalDialogBusquedasFooter").empty();
    $("#modalDialogBusquedasFooter").append($buttonCancelar);
    $("#modalDialogBusquedasFooter").append($buttonAceptar);
    $modalDialogBusquedas.modal();
}

//
//
//

function pushState(state) {
    //console.log("pushState");
    //console.log(state);
    window.history.pushState(state, null, "");
}

function renderState(state) {
    //console.log("renderState");
    //console.log(state);
    if (!state)
        return;
    if (!state.func)
        return;
    if (!state.data) {
        var fn = window[state.func];
        if (typeof fn==="function")
            fn.apply(null, null);
    } else {
        if (state.func==="_certificacionSurtidoCaptura") {
            var rowdata = state.data.rowdata;
            _certificacionSurtidoCaptura(rowdata);
        }
    }
}

