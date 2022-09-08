
function initLogin() {
    $("#navTop").hide();
    $("#navBottom").hide();

    buscaCompanias();
}

function buscaCompanias() {
    var $compania = $("#form-login [name=compania]");

    var onFail = function(err) {
        var mensaje = "Error al obtener la lista de companias.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        error(mensaje);
    };
    var onError = function(response) {
        error(response.mensaje);
    };
    var onComplete = function(response) {
        //$compania.empty();
        for (var i=0; i<response.length; i++) {
            $compania.append($("<option></option>").attr("value",
                response[i].compania).text(response[i].razonsocial+" ["+response[i].compania+"]"));
        }
        pasaValores();
    };

    var where = "1 = 1";
    var order = "razonsocial";
    lista("mx.reder.wms.dao.entity.CompaniaDAO", where, order,
        onComplete, onError, onFail);
}

function pasaValores() {
    var login = gcv("login");
    if (login) {
        var valores = JSON.parse(login);
        pasaValoresForma("#form-login", valores);
    }
}

function iniciaSesion() {
    var $form = $("#form-login");
    var pass = validaDatosForma($form);
    if (!pass)
        notify_error("Hay errores con los datos, favor de corregirlos.");

    var data = getValoresForma("#form-login");

    if (data.recordar==="1") {
        scv("login", JSON.stringify(data), 7);
    } else {
        rcv("login");
    }

    data["id"] = "IniciaSesion";

    var done = function(response) {
        var inicio = function() {
            top.location = "/wms";
        };
        mensaje("Bienvenido <b>"+response.nombre+"</b> a la aplicaci&oacute;n de REDER - WMS.", inicio);
    };
    var fail = function(err) {
        error("Error al Iniciar la Sesi&oacute;n.<br><br><b>("+err.status+") "+err.statusText+"</b>");
    };
    mvc(data, done, fail);
}
