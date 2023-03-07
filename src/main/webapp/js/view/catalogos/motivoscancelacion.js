
function initCatalogo() {
    var _catalogo;
    var catalogo = new catalogoABC();
    catalogo.registroCatalogo = "mx.reder.wms.dao.entity.MotivoCancelacionDAO";

    catalogo.initialize = function() {
        var $motivocancelacion = $("#datos [name=motivocancelacion]");
        $motivocancelacion.keypress(function(e) {
            if (e.keyCode===13) {
                _catalogo.buscarRegistros();
            }
        });
        $motivocancelacion.focus();
        notify_info("Listo.");
    };

    catalogo.buscarRegistrosValores = function() {
        var $motivocancelacion = $("#datos [name=motivocancelacion]");
        var motivocancelacion = $motivocancelacion.val();

        return motivocancelacion;
    };

    catalogo.buscarRegistrosBusqueda = function(complete, error) {
        var $motivocancelacion = $("#datos [name=motivocancelacion]");

        busquedaMotivosCancelacion($motivocancelacion.val(), complete, error);
    };

    catalogo.buscarRegistrosOnAceptar = function() {
        var $motivocancelacion = $("#datos [name=motivocancelacion]");
        $motivocancelacion.select();
        $motivocancelacion.focus();
    };

    catalogo.guardarRegistroOnComplete = function(response) {
        notify_success("Registro guardado correctamente.");
    };

    catalogo.borrarRegistroConfirmadoOnComplete = function(response) {
        notify_success("Registro borrado correctamente.");

        _catalogo.limpiarRegistro();
    };

    catalogo.limpiarRegistroInit = function() {
        var $motivocancelacion = $("#datos [name=motivocancelacion]");
        $motivocancelacion.focus();
    };

    catalogo.initCaptura();

    _catalogo = catalogo;
}