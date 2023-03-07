
function busquedaLotesProductosASPEL(compania, texto, onComplete, onError) {
    var parametros = {
        ASPELcoleccion: "mx.reder.wms.collection.ASPELLotesProductosCollection",
        compania: compania,
        where: "(i.DESCR LIKE '%"+texto+"%' OR l.CVE_ART LIKE '%"+texto+"%')",
        order: "DESCR",
        height: 370,
        title: "Productos",
        datafields: [
            {name: "CVE_ART", type: "string"},
            {name: "LOTE", type: "string"},
            {name: "PEDIMENTO", type: "string"},
            {name: "CVE_ALM", type: "number"},
            {name: "FCHCADUC", type: "date"},
            {name: "FCHADUANA", type: "date"},
            {name: "FCHULTMOV", type: "date"},
            {name: "NOM_ADUAN", type: "string"},
            {name: "CANTIDAD", type: "cantidad"},
            {name: "REG_LTPD", type: "number"},
            {name: "CVE_OBS", type: "number"},
            {name: "CIUDAD", type: "string"},
            {name: "FRONTERA", type: "string"},
            {name: "FEC_PROD_LT", type: "date"},
            {name: "GLN", type: "string"},
            {name: "STATUS", type: "string"},
            {name: "PEDIMENTOSAT", type: "string"},
            {name: "DESCR", type: "string"}
        ],
        columns: [
            {text: "Codigo", datafield: "CVE_ART", width: "20%"},
            {text: "Descripcion", datafield: "DESCR", width: "50%"},
            {text: "Lote", datafield: "LOTE", width: "15%"},
            {text: "F.Caducidad", datafield: "FCHCADUC", width: "15%", cellsformat: "d/M/yyyy"},
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedaProductos(compania, texto, onComplete, onError) {
    var parametros = {
        registro: "mx.reder.wms.dao.entity.ProductoDAO",
        where: "compania = '"+compania+"' AND (descripcion LIKE '%"+texto+"%' OR linea LIKE '%"+texto+"%')",
        order: "descripcion",
        height: 370,
        title: "Productos",
        datafields: [
            {name: "compania", type: "string"},
            {name: "codigo", type: "string"},
            {name: "descripcion", type: "string"},
            {name: "unidadmedida", type: "string"},
            {name: "linea", type: "number"},
            {name: "categoria", type: "number"},
            {name: "marca", type: "string"},
            {name: "existencia", type: "number"},
            {name: "costo", type: "number"},
            {name: "modificacion", type: "string"}
        ],
        columns: [
            {text: "Codigo", datafield: "codigo", width: "30%"},
            {text: "Descripcion", datafield: "descripcion", width: "70%"}
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedaClavesAlternas(compania, texto, onComplete, onError) {
    var parametros = {
        registro: "mx.reder.wms.dao.entity.ClaveAlternaDAO",
        where: "compania = '"+compania+"' AND (clave LIKE '%"+texto+"%' OR codigo LIKE '%"+texto+"%')",
        order: "clave",
        height: 370,
        title: "Claves Alternas",
        datafields: [
            {name: "compania", type: "string"},
            {name: "clave", type: "string"},
            {name: "codigo", type: "string"},
        ],
        columns: [
            {text: "Clave", datafield: "clave", width: "30%"},
            {text: "Codigo", datafield: "codigo", width: "70%"}
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedaSurtidores(compania, texto, onComplete, onError) {
    var parametros = {
        registro: "mx.reder.wms.dao.entity.SurtidorDAO",
        where: "compania = '"+compania+"' AND (surtidor LIKE '%"+texto+"%' OR nombre LIKE '%"+texto+"%')",
        order: "nombre",
        height: 370,
        title: "Surtidores",
        datafields: [
            {name: "compania", type: "string"},
            {name: "surtidor", type: "string"},
            {name: "password", type: "string"},
            {name: "equipo", type: "string"},
            {name: "estado", type: "string"},
            {name: "nombre", type: "string"}
        ],
        columns: [
            {text: "Surtidor", datafield: "surtidor", width: "30%"},
            {text: "Nombre", datafield: "nombre", width: "70%"}
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedaPerfiles(texto, onComplete, onError) {
    var parametros = {
        registro: "mx.reder.wms.dao.entity.PerfilDAO",
        where: "(descripcion LIKE '%"+texto+"%' OR perfil LIKE '%"+texto+"%')",
        order: "descripcion",
        height: 370,
        title: "Perfiles",
        datafields: [
            {name: "perfil", type: "string"},
            {name: "descripcion", type: "string"}
        ],
        columns: [
            {text: "Perfil", datafield: "perfil", width: "30%"},
            {text: "Nombre", datafield: "descripcion", width: "70%"}
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedaMotivosCancelacion(texto, onComplete, onError) {
    var parametros = {
        registro: "mx.reder.wms.dao.entity.MotivoCancelacionDAO",
        where: "(descripcion LIKE '%"+texto+"%' OR motivocancelacion LIKE '%"+texto+"%')",
        order: "descripcion",
        height: 370,
        title: "Motivo Cancelacion",
        datafields: [
            {name: "motivocancelacion", type: "string"},
            {name: "descripcion", type: "string"}
        ],
        columns: [
            {text: "Motivo Cancelacion", datafield: "motivocancelacion", width: "30%"},
            {text: "Nombre", datafield: "descripcion", width: "70%"}
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedaUsuarios(texto, onComplete, onError) {
    var parametros = {
        registro: "mx.reder.wms.dao.entity.UsuarioDAO",
        where: "(nombre LIKE '%"+texto+"%' OR usuario LIKE '%"+texto+"%')",
        order: "nombre",
        height: 370,
        title: "Usuarios",
        datafields: [
            {name: "usuario", type: "string"},
            {name: "password", type: "string"},
            {name: "perfil", type: "string"},
            {name: "estado", type: "string"},
            {name: "nombre", type: "string"},
            {name: "email", type: "string"},
            {name: "vendedor", type: "string"}
        ],
        columns: [
            {text: "Usuario", datafield: "usuario", width: "30%"},
            {text: "Nombre", datafield: "nombre", width: "60%"},
            {text: "Perfil", datafield: "perfil", width: "10%"}
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedaDirecciones(texto, onComplete, onError) {
    var parametros = {
        coleccion: "mx.reder.wms.collection.DireccionesCollection",
        where: texto,
        order: "calle",
        height: 370,
        title: "Direcciones",
        datafields: [
            {name: "direccion", type: "string"},
            {name: "calle", type: "string"},
            {name: "noexterior", type: "string"},
            {name: "nointerior", type: "string"},
            {name: "colonia", type: "string"},
            {name: "dscolonia", type: "string"},
            {name: "poblacion", type: "string"},
            {name: "dspoblacion", type: "string"},
            {name: "entidadfederativa", type: "string"},
            {name: "dsentidadfederativa", type: "string"},
            {name: "pais", type: "string"},
            {name: "dspais", type: "string"},
            {name: "codigopostal", type: "string"}
        ],
        columns: [
            {text: "Calle", datafield: "calle", width: "20%"},
            {text: "No.Ext.", datafield: "noexterior", width: "5%"},
            {text: "No.Int.", datafield: "nointerior", width: "5%"},
            {text: "Colonia", datafield: "dscolonia", width: "20%"},
            {text: "Poblacion", datafield: "dspoblacion", width: "20%"},
            {text: "Entidad Federativa", datafield: "dsentidadfederativa", width: "20%"},
            {text: "C.P.", datafield: "codigopostal", width: "10%"}
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedaColonias(texto, onComplete, onError) {
    var parametros = {
        coleccion: "mx.reder.wms.collection.ColoniasCollection",
        where: "(c.nombre LIKE '%"+texto+"%' OR c.codigopostal LIKE '%"+texto+"%')",
        order: "nombre",
        height: 370,
        title: "Colonias",
        datafields: [
            {name: "colonia", type: "string"},
            {name: "dscolonia", type: "string"},
            {name: "poblacion", type: "string"},
            {name: "dspoblacion", type: "string"},
            {name: "entidadfederativa", type: "string"},
            {name: "dsentidadfederativa", type: "string"},
            {name: "pais", type: "string"},
            {name: "dspais", type: "string"},
            {name: "nombre", type: "string"},
            {name: "codigopostal", type: "string"}
        ],
        columns: [
            {text: "Colonia", datafield: "dscolonia", width: "40%"},
            {text: "Poblacion", datafield: "dspoblacion", width: "30%"},
            {text: "Entidad Federativa", datafield: "dsentidadfederativa", width: "20%"},
            {text: "C.P.", datafield: "codigopostal", width: "10%"}
        ]
    };

    busquedas(parametros, onComplete, onError);
}

function busquedas(parametros, onComplete, onError, onCancelar) {
    var onFailII = function(err) {
        error("Error al obtener la lista de registros ["+parametros.registro+";"+parametros.where+"].<br><br><b>("+
            err.status+") "+err.statusText+"</b>");
    };
    var onErrorII = function(response) {
        if (onError) {
            onError(response);
        } else {
            notify_error(response.mensaje);
        }
    };
    var onCompleteII = function(response) {
        if (response.length===1) {
            var selectedRowData = response[0];

            if (onComplete)
                onComplete(selectedRowData);

            return;
        }

        var selectedRowData;

        var aceptar = function() {
            var $grid = $("#grid_search");
            var selectedrowindex = $grid.jqxGrid("getselectedrowindex");
            if (selectedrowindex===-1) {
                notify_error("Debe de seleccionar un registro.", $notify);
                return;
            }

            selectedRowData = $grid.jqxGrid("getrowdata", selectedrowindex);
            $grid.jqxGrid("clearselection");
            $grid.jqxGrid({handlekeyboardnavigation: null});

            $modalDialogBusquedas.modal("hide");
        };

        var source = {
            localdata: response,
            datafields: parametros.datafields,
            datatype: "array"
        };
        var dataAdapter = new $.jqx.dataAdapter(source);
        var $grid = $("<div id=\"grid_search\"></div>");
        $grid.jqxGrid({
            width: "100%",
            height: parametros.height - 120,
            altrows: true,
            sortable: true,
            source: dataAdapter,
            columns: parametros.columns,
            handlekeyboardnavigation: function(event) {
                var key = event.charCode ? event.charCode :
                    event.keyCode ? event.keyCode : 0;
                if (key===13) {
                    aceptar();
                    return true;
                }
                return false;
            }
        });

        var $buttonAceptar = $("<button type=\"button\" class=\"btn btn-outline-success\">Aceptar</button>");
        $buttonAceptar.click(function() {
            aceptar();
        });
        var $buttonCancelar = $("<button type=\"button\" class=\"btn btn-outline-secondary\">Cancelar</button>");
        $buttonCancelar.click(function() {
            notify_info("Listo.");
            selectedRowData = null;
            $modalDialogBusquedas.modal("hide");
        });

        var $content = $("<hr><div class=\"space\"></div><div id=\"busquedas-notificacion\">Listo.</div>");

        var onReady = function() {
            $modalDialogBusquedas.focus();

            var $grid = $("#grid_search");
            $grid.jqxGrid("selectrow", 0);
            $grid.jqxGrid("focus");
        };

        $("#modalDialogBusquedasLabel").html(parametros.title);
        $("#modalDialogBusquedasDocument").addClass("modal-lg");
        $("#modalDialogBusquedasHeader").removeClass();
        $("#modalDialogBusquedasHeader").addClass("modal-header bg-info text-white");
        $("#modalDialogBusquedasBody").empty();
        $("#modalDialogBusquedasBody").append($grid);
        $("#modalDialogBusquedasBody").append($content);
        $("#modalDialogBusquedasFooter").empty();
        $("#modalDialogBusquedasFooter").append($buttonCancelar);
        $("#modalDialogBusquedasFooter").append($buttonAceptar);

        var $notify = $("#busquedas-notificacion");
        notify_info("Listo.", $notify);

        if (response.length===0)
            notify_warning("No se encontro ning&uacute;n registro.", $notify);

        $modalDialogBusquedas.unbind("shown.bs.modal");
        $modalDialogBusquedas.on("shown.bs.modal", function() {
            onReady();
        });
        $modalDialogBusquedas.unbind("hidden.bs.modal");
        $modalDialogBusquedas.on("hidden.bs.modal", function() {
            if (selectedRowData) {
                if (onComplete)
                    onComplete(selectedRowData);
            } else {
                if (onCancelar)
                    onCancelar();
            }
        });

        $modalDialogBusquedas.modal({
            backdrop: "static"
        });
    };

    notify_secondary("Obteniendo registros ...");
    if (parametros.ASPELcoleccion)
        ASPELcoleccion(parametros.ASPELcoleccion, parametros.compania, parametros.where, onCompleteII, onErrorII, onFailII);
    else if (parametros.coleccion)
        coleccion(parametros.coleccion, parametros.where, onCompleteII, onErrorII, onFailII);
    else
        lista(parametros.registro, parametros.where, parametros.order, onCompleteII, onErrorII, onFailII);
}

function buscarCompania(compania, onComplete, onAceptar) {
    var onAceptarII = function() {
        if (onAceptar)
            onAceptar();
    };
    var onFailII = function(err) {
        var mensaje = "Error al buscar el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        notify_error(mensaje);
    };
    var onErrorII = function(response) {
        if (response.exception.indexOf("WebException")!==-1) {
            precaucion(response.mensaje, onAceptarII);
            notify_warning(response.mensaje);
        } else {
            error(response.exception, onAceptarII);
            notify_error(response.exception);
        }
    };
    var onCompleteII = function(response) {
        if (onComplete)
            onComplete(response);
    };
    notify_secondary("Buscando el registro ...");
    registro("mx.reder.wms.dao.entity.CompaniaDAO", compania,
        onCompleteII, onErrorII, onFailII);
}

function buscarUsuario(_usuario, onComplete, onAceptar) {
    var onAceptarII = function() {
        if (onAceptar)
            onAceptar();
    };
    var onFailII = function(err) {
        var mensaje = "Error al buscar el registro.<br><br><b>("+err.status+") "+err.statusText+"</b>";
        notify_error(mensaje);
    };
    var onErrorII = function(response) {
        if (response.exception.indexOf("WebException")!==-1) {
            precaucion(response.mensaje, onAceptarII);
            notify_warning(response.mensaje);
        } else {
            error(response.exception, onAceptarII);
            notify_error(response.exception);
        }
    };
    var onCompleteII = function(response) {
        if (onComplete)
            onComplete(response);
    };
    notify_secondary("Buscando el registro ...");
    registro("mx.reder.wms.dao.entity.UsuarioDAO", _usuario,
        onCompleteII, onErrorII, onFailII);
}

function autoBusquedaCodigo($codigo, onComplete, onError) {
    var data = function(value, ondata) {
        value = value.toUpperCase();
        var onAceptar = function(err) {
            $codigo.select();
            $codigo.focus();
        };
        var onFailII = function(err) {
            error("Error al obtener la lista de registros.<br><br><b>("+
                err.status+") "+err.statusText+"</b>");
        };
        var onErrorII = function(response) {
            if (onError) {
                onError(response);
            } else {
                if (response.exception.indexOf("WebException")!==-1) {
                    precaucion(response.mensaje, onAceptar);
                    notify_warning(response.mensaje);
                } else {
                    error(response.exception, onAceptar);
                    notify_error(response.exception);
                }
            }
        };
        var onCompleteIII = function(response) {
            ondata(response);
        };
        var onCompleteII = function(response) {
            if (response.length===0) {
                var data = {
                    id: "BusquedaProductos",
                    valor: value
                };
                mvc(data, onCompleteIII, onFailII, onErrorII);
                return;
            }
            ondata(response);
        };
        var data = {
            id: "Productos",
            valor: value
        };
        mvc(data, onCompleteII, onFailII, onErrorII);
    };
    var result = function(record, value) {
        if (!record.descripcion)
            record.descripcion = "";
        return record.descripcion.includes(value)
            || record.codigo.includes(value);
    };
    var render = function(record, value) {
        return "<table class=\"full_width\"><tr>"
                +"<td class=\"half_width f_size_medium\">"+replaceAll(record.codigo, value, "<strong>"+value+"</strong>")+"</td>"
                +"<td class=\"half_width t_align_r f_size_large\">Existencia: <b>"+formatNumber(record.existencia)+"</b></td>"
                +"</tr></table>"
                +"<span class=\"f_size_large\">"+replaceAllIgnoreCase(record.descripcion, value, "<strong>"+value+"</strong>")+"</span>";
    };
    var click = function(input, record) {
        onComplete(record);
    };
    // 99 para que nunca inicie la autobusqueda, solamente hasta el ENTER
    autocomplete(99, $codigo[0], data, result, render, click);

    $codigo.focus();
}

function autoBusquedaSurtidor($surtidor, onComplete, onError) {
    var data = function(value, ondata) {
        value = value.toUpperCase();
        var onAceptar = function(err) {
            $surtidor.select();
            $surtidor.focus();
        };
        var onFailII = function(err) {
            error("Error al obtener la lista de registros.<br><br><b>("+
                err.status+") "+err.statusText+"</b>");
        };
        var onErrorII = function(response) {
            if (onError) {
                onError(response);
            } else {
                if (response.exception.indexOf("WebException")!==-1) {
                    precaucion(response.mensaje, onAceptar);
                    notify_warning(response.mensaje);
                } else {
                    error(response.exception, onAceptar);
                    notify_error(response.exception);
                }
            }
        };
        var onCompleteII = function(response) {
            ondata(response);
        };
        var where = "compania = '"+usuario.compania+"' AND (surtidor LIKE '%"+value+"%' OR nombre LIKE '%"+value+"%')";
        lista("mx.reder.wms.dao.entity.SurtidorDAO", where, "nombre",
            onCompleteII, onErrorII, onFailII);
    };
    var result = function(record, value) {
        return (record.nombre && record.nombre.includes(value))
            || record.surtidor.includes(value);
    };
    var render = function(record, value) {
        var html = "<table class=\"full_width\"><tr>"
                +"<td class=\"half_width f_size_large\">"+record.surtidor+"</td>"
                +"<td class=\"half_width t_align_r f_size_large\">"+record.equipo+"</td>"
                +"</tr></table>"
                +"<span class=\"f_size_medium\">"+record.nombre+"</span>";
        return replaceAll(html, value, "<strong>"+value+"</strong>");
    };
    var click = function(input, record) {
        onComplete(record);
    };
    autocomplete(3, $surtidor[0], data, result, render, click);

    $surtidor.focus();
}

function autoBusquedaTipoFiguraTransporte($figuratransporte, onComplete, onError) {
    var data = function(value, ondata) {
        value = value.toUpperCase();
        var onAceptar = function(err) {
            $figuratransporte.select();
            $figuratransporte.focus();
        };
        var onFailII = function(err) {
            error("Error al obtener la lista de registros.<br><br><b>("+
                err.status+") "+err.statusText+"</b>");
        };
        var onErrorII = function(response) {
            if (onError) {
                onError(response);
            } else {
                if (response.exception.indexOf("WebException")!==-1) {
                    precaucion(response.mensaje, onAceptar);
                    notify_warning(response.mensaje);
                } else {
                    error(response.exception, onAceptar);
                    notify_error(response.exception);
                }
            }
        };
        var onCompleteII = function(response) {
            ondata(response);
        };
        var data = {
            id: "TipoFigurasTransporte",
            compania: usuario.compania,
            valor: value
        };
        mvc(data, onCompleteII, onFailII, onErrorII);
    };
    var result = function(record, value) {
        return record.clave.includes(value)
            || record.licencia.includes(value)
            || record.rfc.includes(value)
            || record.nombre.includes(value);
    };
    var render = function(record, value) {
        return "<table class=\"full_width\"><tr>"
                +"<td class=\"half_width f_size_medium\">"+replaceAll(record.clave, value, "<strong>"+value+"</strong>")+"</td>"
                +"<td class=\"half_width f_size_medium\">"+replaceAll(record.licencia, value, "<strong>"+value+"</strong>")+"</td>"
                +"</tr></table>"
                +"<table class=\"full_width\"><tr>"
                +"<td class=\"half_width f_size_medium\">"+replaceAll(record.rfc, value, "<strong>"+value+"</strong>")+"</td>"
                +"<td class=\"half_width f_size_medium\">"+replaceAll(record.tipofigura, value, "<strong>"+value+"</strong>")+"</td>"
                +"</tr></table>"
                +"<span class=\"f_size_large\">"+replaceAllIgnoreCase(record.nombre, value, "<strong>"+value+"</strong>")+"</span>";
    };
    var click = function(input, record) {
        onComplete(record);
    };
    autocomplete(3, $figuratransporte[0], data, result, render, click);

    $figuratransporte.focus();
}

function autoBusquedaAutotransporte($autotransporte, onComplete, onError) {
    var data = function(value, ondata) {
        value = value.toUpperCase();
        var onAceptar = function(err) {
            $autotransporte.select();
            $autotransporte.focus();
        };
        var onFailII = function(err) {
            error("Error al obtener la lista de registros.<br><br><b>("+
                err.status+") "+err.statusText+"</b>");
        };
        var onErrorII = function(response) {
            if (onError) {
                onError(response);
            } else {
                if (response.exception.indexOf("WebException")!==-1) {
                    precaucion(response.mensaje, onAceptar);
                    notify_warning(response.mensaje);
                } else {
                    error(response.exception, onAceptar);
                    notify_error(response.exception);
                }
            }
        };
        var onCompleteII = function(response) {
            ondata(response);
        };
        var data = {
            id: "Autotransportes",
            compania: usuario.compania,
            valor: value
        };
        mvc(data, onCompleteII, onFailII, onErrorII);
    };
    var result = function(record, value) {
        return record.clave.includes(value)
            || record.placa.includes(value)
            || record.numeropermiso.includes(value);
    };
    var render = function(record, value) {
        return "<table class=\"full_width\"><tr>"
                +"<td class=\"half_width f_size_medium\">"+replaceAll(record.clave, value, "<strong>"+value+"</strong>")+"</td>"
                +"<td class=\"half_width f_size_medium\">"+replaceAll(record.placa, value, "<strong>"+value+"</strong>")+"</td>"
                +"</tr></table>"
                +"<span class=\"f_size_large\">"+replaceAllIgnoreCase(record.numeropermiso, value, "<strong>"+value+"</strong>")+"</span>";
    };
    var click = function(input, record) {
        onComplete(record);
    };
    autocomplete(3, $autotransporte[0], data, result, render, click);

    $autotransporte.focus();
}

function seleccionaTerminal(onSelected, onCanceled) {

    var onComplete = function(response) {
        var $content = $(response);

        var selectedRowData;

        var aceptar = function () {
            var $form = $("#buscar-terminal-datos");
            var pass = validaDatosForma($form);
            if (!pass) {
                notify_error("Hay errores con los datos, favor de corregirlos.", $notify);
                return;
            }

            selectedRowData = getValoresForma("#buscar-terminal-datos");
            $modalDialog3.modal("hide");
        };

        var $buttonAceptar = $("<button type=\"button\" class=\"btn btn-outline-success\">Aceptar</button>");
        $buttonAceptar.click(function() {
            aceptar();
        });
        var $buttonCancelar = $("<button type=\"button\" class=\"btn btn-outline-secondary\">Cancelar</button>");
        $buttonCancelar.click(function() {
            selectedRowData = null;
            $modalDialog3.modal("hide");
        });

        $("#modalDialog3Label").html("Selecci&oacute;n de Terminal");
        $("#modalDialog3Header").removeClass();
        $("#modalDialog3Header").addClass("modal-header bg-info text-white");
        $("#modalDialog3Body").empty();
        $("#modalDialog3Body").append($content);
        $("#modalDialog3Footer").empty();
        $("#modalDialog3Footer").append($buttonCancelar);
        $("#modalDialog3Footer").append($buttonAceptar);

        var $input = $("#buscar-terminal-datos input[name=terminal]");
        var $notify = $("#buscar-terminal-notificacion");

        $input.keypress(function(e) {
            if (e.keyCode===13) {
                aceptar();
                return false;
            }
        });

        notify_info("Listo.", $notify);

        $modalDialog3.unbind("shown.bs.modal");
        $modalDialog3.on("shown.bs.modal", function() {
            $input.focus();
        });
        $modalDialog3.unbind("hidden.bs.modal");
        $modalDialog3.on("hidden.bs.modal", function() {
            if (selectedRowData) {
                if (onSelected)
                    onSelected(selectedRowData);
            } else {
                if (onCanceled)
                    onCanceled();
            }
        });

        $modalDialog3.modal({
            backdrop: "static"
        });
    };

    loadPage("/wms/view/inventarios/terminal-seleccion.html", onComplete);
}

function seleccionaEquipoSurtido(compania, onSelected, onCanceled) {

    var onComplete = function(response) {
        var $content = $(response);

        var registro;
        var aceptar = function () {
            var $form = $("#buscar-surtidor-datos");
            var pass = validaDatosForma($form);
            if (!pass) {
                notify_error("Hay errores con los datos, favor de corregirlos.", $notify);
                return;
            }

            registro = getValoresForma("#buscar-surtidor-datos");
            $modalDialog3.modal("hide");
        };

        var $buttonAceptar = $("<button type=\"button\" class=\"btn btn-outline-success\">Aceptar</button>");
        $buttonAceptar.click(function() {
            aceptar();
        });
        var $buttonCancelar = $("<button type=\"button\" class=\"btn btn-outline-secondary\">Cancelar</button>");
        $buttonCancelar.click(function() {
            registro = null;
            $modalDialog3.modal("hide");
        });

        $("#modalDialog3Label").html("Selecci&oacute;n de Equipo de Surtido");
        $("#modalDialog3Document").addClass("modal-lg");
        $("#modalDialog3Header").removeClass();
        $("#modalDialog3Header").addClass("modal-header bg-info text-white");
        $("#modalDialog3Body").empty();
        $("#modalDialog3Body").append($content);
        $("#modalDialog3Footer").empty();
        $("#modalDialog3Footer").append($buttonCancelar);
        $("#modalDialog3Footer").append($buttonAceptar);

        var $select = $("#buscar-surtidor-datos [name=equipo]");
        var $notify = $("#buscar-surtidor-notificacion");

        var onFailII = function(err) {
            var msg = "Error al buscar los registros.<br><br><b>("+err.status+") "+err.statusText+"</b>";
            notify_error(msg, $notify);
        };
        var onErrorII = function(response) {
            if (response.exception.indexOf("WebException")!==-1) {
                notify_warning(response.mensaje, $notify);
            } else {
                notify_error(response.exception, $notify);
            }
        };
        var onCompleteII = function(response) {
            for (var i=0; i<response.length; i++) {
                $select.append($("<option></option>").attr("value",
                    response[i].equipo).text(response[i].equipo));
            }
            notify_info("Listo.", $notify);
        };

        notify_secondary("Buscando los Equipos de Surtido ...", $notify);
        coleccion("mx.reder.wms.collection.EquiposSurtidoCollection", "s.compania = '"+compania+"'",
            onCompleteII, onErrorII, onFailII);

        $modalDialog3.unbind("shown.bs.modal");
        $modalDialog3.on("shown.bs.modal", function() {
            $select.focus();
        });
        $modalDialog3.unbind("hidden.bs.modal");
        $modalDialog3.on("hidden.bs.modal", function() {
            if (registro) {
                if (onSelected)
                    onSelected(registro);
            } else {
                if (onCanceled)
                    onCanceled();
            }
        });

        $modalDialog3.modal({
            backdrop: "static"
        });
    };

    loadPage("/wms/view/almacen/surtidor-seleccion.html", onComplete);
}

function seleccionaLoteSurtido(records, onComplete, onCancelar) {
    var selectedRowData;

    if (records.length===1) {
        selectedRowData = records[0];
        if (onComplete)
            onComplete(selectedRowData);
        return;
    }

    var aceptar = function() {
        var $grid = $("#grid_seleccion_lotes");
        var selectedrowindex = $grid.jqxGrid("getselectedrowindex");
        if (selectedrowindex===-1) {
            notify_error("Debe de seleccionar un registro.", $notify);
            return;
        }

        selectedRowData = $grid.jqxGrid("getrowdata", selectedrowindex);
        $grid.jqxGrid("clearselection");
        $grid.jqxGrid({handlekeyboardnavigation: null});

        $modalDialogBusquedas.modal("hide");
    };

    var source = {
        localdata: records,
        datafields: [
            {name: "compania", type: "string"},
            {name: "flsurtido", type: "number"},
            {name: "partida", type: "number"},
            {name: "idlote", type: "number"},
            {name: "codigo", type: "string"},
            {name: "descripcion", type: "string"},
            {name: "lote", type: "string"},
            {name: "fecaducidad", type: "date"},
            {name: "surtidas", type: "number"}
        ],
        datatype: "array"
    };
    var dataAdapter = new $.jqx.dataAdapter(source);
    var $grid = $("<div id=\"grid_seleccion_lotes\"></div>");
    $grid.jqxGrid({
        width: "100%",
        height: 200,
        altrows: true,
        sortable: true,
        source: dataAdapter,
        columns: [
            {text: "Partida", datafield: "partida", width: "5%"},
            {text: "ID", datafield: "idlote", width: "5%"},
            {text: "Codigo", datafield: "codigo", width: "10%"},
            {text: "Descripcion", datafield: "descripcion", width: "40%"},
            {text: "Lote", datafield: "lote", width: "15%"},
            {text: "Caducidad", datafield: "fecaducidad", width: "15%", cellsformat: "d/M/yyyy"},
            {text: "Surtidas", datafield: "surtidas", width: "10%", cellsformat: "f0", cellsalign: "right"}
        ],
        handlekeyboardnavigation: function(event) {
            var key = event.charCode ? event.charCode :
                event.keyCode ? event.keyCode : 0;
            if (key===13) {
                aceptar();
                return true;
            }
            return false;
        }
    });

    var $buttonAceptar = $("<button type=\"button\" class=\"btn btn-outline-success\">Aceptar</button>");
    $buttonAceptar.click(function() {
        aceptar();
    });
    var $buttonCancelar = $("<button type=\"button\" class=\"btn btn-outline-secondary\">Cancelar</button>");
    $buttonCancelar.click(function() {
        notify_info("Listo.");
        selectedRowData = null;
        $modalDialogBusquedas.modal("hide");
    });

    var $content = $("<hr><div class=\"space\"></div><div id=\"seleccion-lotes-notificacion\">Listo.</div>");

    var onReady = function() {
        $modalDialogBusquedas.focus();

        var $grid = $("#grid_seleccion_lotes");
        $grid.jqxGrid("selectrow", 0);
        $grid.jqxGrid("focus");
    };

    $("#modalDialogBusquedasLabel").html("Lotes Surtidos");
    $("#modalDialogBusquedasDocument").addClass("modal-lg");
    $("#modalDialogBusquedasHeader").removeClass();
    $("#modalDialogBusquedasHeader").addClass("modal-header bg-info text-white");
    $("#modalDialogBusquedasBody").empty();
    $("#modalDialogBusquedasBody").append($grid);
    $("#modalDialogBusquedasBody").append($content);
    $("#modalDialogBusquedasFooter").empty();
    $("#modalDialogBusquedasFooter").append($buttonCancelar);
    $("#modalDialogBusquedasFooter").append($buttonAceptar);

    var $notify = $("#seleccion-lotes-notificacion");
    notify_info("Listo.", $notify);

    $modalDialogBusquedas.unbind("shown.bs.modal");
    $modalDialogBusquedas.on("shown.bs.modal", function() {
        onReady();
    });
    $modalDialogBusquedas.unbind("hidden.bs.modal");
    $modalDialogBusquedas.on("hidden.bs.modal", function() {
        if (selectedRowData) {
            if (onComplete)
                onComplete(selectedRowData);
        } else {
            if (onCancelar)
                onCancelar();
        }
    });

    $modalDialogBusquedas.modal({
        backdrop: "static"
    });
}
