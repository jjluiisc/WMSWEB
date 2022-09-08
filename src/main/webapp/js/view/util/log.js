
function initLog() {
    obtenLog();
    notify_info("Listo.");
}

function obtenLog() {
    var data = {
        id: "VerLog",
        compania: usuario.compania,
        usuario: usuario.usuario
    };

    var onAceptar = function() {
    };
    var onFail = function(err) {
        var msg = "Error al cancelar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
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
        var $ta = $("#datos [name=log]");
        $ta.val(response.msg);
        $ta.scrollTop($ta[0].scrollHeight);

        notify_info("Listo.");
    };

    notify_secondary("Obtengo Log ...");
    mvc(data, onComplete, onFail, onError);
}
