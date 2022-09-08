

function initCatalogo() {
    var _catalogo;
    var catalogo = new catalogoABC();
    catalogo.registroCatalogo = "mx.reder.wms.dao.entity.SurtidorDAO";

    catalogo.initialize = function() {
        var $compania = $("#datos [name=compania]");
        $compania.val(usuario.compania);
        var onComplete = function(response) {
            var $razonsocial = $("#datos [name=razonsocial]");
            $razonsocial.val(response.razonsocial);
        };
        buscarCompania(usuario.compania, onComplete);    

        var $surtidor = $("#datos [name=surtidor]");
        $surtidor.keypress(function(e) {
            if (e.keyCode===13) {
                _catalogo.buscarRegistros();
            }
        });
        $surtidor.focus();
        notify_info("Listo.");
    };

    catalogo.buscarRegistrosValores = function() {
        var $surtidor = $("#datos [name=surtidor]");
        var surtidor = $surtidor.val();

        return usuario.compania+"|"+surtidor;
    };

    catalogo.buscarRegistrosBusqueda = function(complete, error) {
        var $surtidor = $("#datos [name=surtidor]");

        busquedaSurtidores(usuario.compania, $surtidor.val(), complete, error);
    };

    catalogo.buscarRegistrosOnAceptar = function() {
        var $surtidor = $("#datos [name=surtidor]");
        $surtidor.select();
        $surtidor.focus();
    };

    catalogo.guardarRegistroOnComplete = function(response) {
        notify_success("Registro guardado correctamente.");
    };

    catalogo.borrarRegistroConfirmadoOnComplete = function(response) {
        notify_success("Registro borrado correctamente.");

        _catalogo.limpiarRegistro();
    };

    catalogo.limpiarRegistroInit = function() {
        var $surtidor = $("#datos [name=surtidor]");
        $surtidor.focus();
    };

    catalogo.initCaptura();

    _catalogo = catalogo;
}
