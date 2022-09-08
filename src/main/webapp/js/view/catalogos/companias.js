
function initCatalogo() {
    var _catalogo;
    var catalogo = new catalogoABC();
    catalogo.registroCatalogo = "mx.reder.wms.dao.entity.CompaniaDAO";

    catalogo.initialize = function() {
        var $compania = $("#datos [name=compania]");
        $compania.val(usuario.compania);
        $compania.attr("disabled", true);

        var $direccion = $("#datos [name=direccion]");
        $direccion.attr("disabled", true);

        var $descripcion_direccion = $("#datos [name=descripcion_direccion]");
        $descripcion_direccion.attr("disabled", true);

        $("#btn-direccion").click(function() {
            _catalogo.modificaDireccion();
        });
        $("#btn-certificado").click(function() {
            _catalogo.certificadoSelloDigital();
        });

        notify_info("Listo.");
    };

    catalogo.buscarRegistrosValores = function() {
        var $compania = $("#datos [name=compania]");
        var compania = $compania.val();
        if (compania==="") {
            $compania.focus();
            return;
        }

        return compania+"|";
    };

    catalogo.buscarRegistrosBusqueda = function(complete, error) {
        buscarCompania(complete, error);
    };

    catalogo.buscarRegistrosOnAceptar = function() {
        var $compania = $("#datos [name=compania]");
        $compania.select();
        $compania.focus();
    };

    catalogo.buscarRegistrosOnComplete = function(response) {
        pasaValoresForma("#datos", response);

        $("#datos [name=prdescuentopp]").val(redondea(response.prdescuentopp * 100));
        $("#datos [name=prcomision]").val(redondea(response.prcomision * 100));
        $("#datos [name=prcostoempaque]").val(redondea(response.prcostoempaque * 100));

        _catalogo.buscarDireccion();

        notify_info("Listo.");
    };

    catalogo.getValoresRegistro = function() {
        var valores = getValoresForma("#datos");

        if (valores.prdescuentopp > 1.0)
            valores.prdescuentopp = redondea(valores.prdescuentopp / 100.0, 4);
        if (valores.prcomision > 1.0)
            valores.prcomision = redondea(valores.prcomision / 100.0, 4);
        if (valores.prcostoempaque > 1.0)
            valores.prcostoempaque = redondea(valores.prcostoempaque / 100.0, 4);

        return JSON.stringify(valores);
    };

    catalogo.guardarRegistroOnComplete = function(response) {
        notify_success("Registro guardado correctamente.");
    };

    catalogo.borrarRegistroConfirmadoOnComplete = function(response) {
        notify_success("Registro borrado correctamente.");

        _catalogo.limpiarRegistro();
    };

    catalogo.limpiarRegistroInit = function() {
        var $compania = $("#datos [name=compania]");
        $compania.focus();
    };

    catalogo.reportesRegistro = function() {
        /*var opciones = [
            {nombre: "Cat&aacute;logo de Productos", funcion: "ejecutaReporte('Productos','compania="+usuario.compania+"'); return false;"},
            {nombre: "Cat&aacute;logo de Productos con Existencia", funcion: "ejecutaReporte('Productos','compania="+usuario.compania+"&soloexistencia=true'); return false;"},
            {nombre: "Valuaci&oacute;n de Inventario", funcion: "ejecutaReporte('ValuacionInventario','compania="+usuario.compania+"'); return false;"},
            {nombre: "Movimientos de Inventario", funcion: "ejecutaReporte('MovimientosInventario','compania="+usuario.compania+"'); return false;"},
            {nombre: "Kardex", funcion: "ejecutaReporte('Kardex','compania="+usuario.compania+"'); return false;"},
            {nombre: "Cuenta por Cobrar", funcion: "ejecutaReporte('CuentaPorCobrar','compania="+usuario.compania+"'); return false;"},
            {nombre: "Cuenta por Pagar", funcion: "ejecutaReporte('CuentaPorPagar','compania="+usuario.compania+"'); return false;"}
        ];
        abreOpciones("Reportes de Compa&ntilde;ia", opciones);*/

        var onComplete = function() {
            var onCompleteII = function() {
                initReportes();
            };
            loadScript("/wms/js/view/catalogos/companias-reportes.js", onCompleteII);
        };
        loadMainPage("/wms/view/catalogos/companias-reportes.html", onComplete);
    };

    catalogo.certificadoSelloDigital = function() {
        var onComplete = function(response) {
            var $content = $(response);

            var aceptar = function() {
                //
                // El Certificado ya se agrego, salgo
                //
                var $nocertificado = $("#form-csd [name=nocertificado]");
                if ($nocertificado.val()!=="") {
                    $modalDialog3.modal("hide");
                    return;
                }
                
                var $form = $("#form-csd");
                var pass = validaDatosForma($form);
                if (!pass) {
                    notify_error("Hay errores con los datos, favor de corregirlos.", $notify);
                    return;
                }
                
                _catalogo.certificadoSelloDigitalValidado();
            };

            var $buttonAceptar = $("<button type=\"button\" class=\"btn btn-outline-success\">Aceptar</button>");
            $buttonAceptar.click(function() {
                aceptar();
            });
            var $buttonCancelar = $("<button type=\"button\" class=\"btn btn-outline-secondary\">Cancelar</button>");
            $buttonCancelar.click(function() {
                $modalDialog3.modal("hide");
            });

            $("#modalDialog3Label").html("Agregar CSD");
            $("#modalDialog3Document").addClass("modal-lg");
            $("#modalDialog3Header").removeClass();
            $("#modalDialog3Header").addClass("modal-header bg-info text-white");
            $("#modalDialog3Body").empty();
            $("#modalDialog3Body").append($content);
            $("#modalDialog3Footer").empty();
            $("#modalDialog3Footer").append($buttonCancelar);
            $("#modalDialog3Footer").append($buttonAceptar);

            var $compania = $("#form-csd [name=compania]");
            $compania.val($("#datos [name=compania]").val());
            
            var $razonsocial = $("#form-csd [name=razonsocial]");
            $razonsocial.val($("#datos [name=razonsocial]").val());
            
            var $notify = $("#csd-notificacion");
            notify_info("Listo.", $notify);

            $modalDialog3.unbind("shown.bs.modal");
            $modalDialog3.on("shown.bs.modal", function () {                
            });
            $modalDialog3.unbind("hidden.bs.modal");
            $modalDialog3.on("hidden.bs.modal", function() {
            });

            $modalDialog3.modal({
                keyboard: false,
                backdrop: "static"
            });
        };

        loadPage("/wms/view/catalogos/companias-csd.html", onComplete);        
    };

    catalogo.certificadoSelloDigitalValidado = function() {
        wm();

        var $notify = $("#csd-notificacion");
        
        $("#form-csd").submit(function (e) {
            var formObj = $(this);
            var formURL = formObj.attr("action");
            var formData = new FormData(this);
            $.ajax({
                url: formURL,
                type: "post",
                data: formData,
                mimeType: "multipart/form-data",
                contentType: false,
                cache: false,
                processData: false,
                success: function (data, textStatus, jqXHR) {
                    cwm();

                    var response = JSON.parse(data);
                    if (response.error) {
                        notify_error(response.mensaje, $notify);
                    } else {
                        $("#form-csd [name=nocertificado]").val(response.nocertificado);
                        $("#form-csd [name=fechainicial]").val(response.fechainicial);
                        $("#form-csd [name=fechafinal]").val(response.fechafinal);
                        
                        notify_success("Certificado de Sello Digital agregado correctamente.", $notify);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    cwm();
                    notify_error(errorThrown, $notify);
                }
            });
            e.preventDefault();
            //e.unbind();
        });
        $("#form-csd").submit();        
    };            
            
    var direccion = new direccionABC();
    catalogo.modificaDireccion = direccion.modificaDireccion;
    catalogo.buscarDireccion = direccion.buscarDireccion;
    catalogo.buscarColonia = direccion.buscarColonia;
    catalogo.pasaValoresDireccion = direccion.pasaValoresDireccion;

    catalogo.initCaptura();

    _catalogo = catalogo;

    var onComplete = function(response) {
        _catalogo.buscarRegistrosOnComplete(response);
    };
    buscarCompania(usuario.compania, onComplete);
}
