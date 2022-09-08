<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!doctype html>

<%@page import="java.lang.reflect.Field"%>
<%@page import="mx.reder.wms.to.UsuarioTO" %>

<%
    UsuarioTO usuario = (UsuarioTO)session.getAttribute("usuario");
    boolean loggeado = usuario!=null;
    if (usuario==null)
        usuario = new UsuarioTO();
%>

<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 60);
%>

<html lang="en">
    <head>
        <!-- Required meta tags -->
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <meta name="description" content="">
        <meta name="author" content="">
        <link rel="icon" href="/wms/img/favicon.ico">

        <!-- Bootstrap CSS -->
        <link rel="stylesheet" type="text/css" href="/wms/css/bootstrap/bootstrap.css">
        <link rel="stylesheet" type="text/css" href="/wms/css/bootstrap/font-awesome.min.css">
        <link rel="stylesheet" type="text/css" href="/wms/css/jqx/jqx.base.css">
        <link rel="stylesheet" type="text/css" href="/wms/css/jqx/jqx.arctic.css">
        <link rel="stylesheet" type="text/css" href="/wms/css/wms.css">

        <title>REDER - WMS</title>

        <script type="text/javascript">
            var index = "index.jsp";
            var loggeado = <%=loggeado%>;
            var usuario = {
            <%
    Field[] fields = usuario.getClass().getDeclaredFields();
    for (Field field : fields) {
        Object value = field.get(usuario);
        String comillas = value instanceof String ? "\"" : "";
        out.write("                " + field.getName() + ": " + comillas + value + comillas + ",\n");
    }
            %>
            };
        </script>
    </head>

    <body>

        <%@include file="/view/content/header.html"%>
        <div style="height: 80px;"></div>
        <div class="container" id="mainContent">
        </div>
        <div style="height: 80px;"></div>
        <%@include file="/view/content/footer.html"%>

        <div id="modalDialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modalDialogLabel" aria-hidden="true">
            <div class="modal-dialog" id="modalDialogDocument" role="document">
                <div class="modal-content">
                    <div class="modal-header" id="modalDialogHeader">
                        <h5 class="modal-title" id="modalDialogLabel">Modal title</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body" id="modalDialogBody">
                    </div>
                    <div class="modal-footer" id="modalDialogFooter">
                    </div>
                </div>
            </div>
        </div>
        <div id="modalDialog2" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modalDialogLabel" aria-hidden="true">
            <div class="modal-dialog" id="modalDialogDocument2" role="document">
                <div class="modal-content">
                    <div class="modal-header" id="modalDialogHeader2">
                        <h5 class="modal-title" id="modalDialogLabel2">Modal title</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body" id="modalDialogBody2">
                    </div>
                    <div class="modal-footer" id="modalDialogFooter2">
                    </div>
                </div>
            </div>
        </div>
        <div id="modalDialog3" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modalDialog3Label" aria-hidden="true">
            <div class="modal-dialog" id="modalDialog3Document" role="document">
                <div class="modal-content">
                    <div class="modal-header" id="modalDialog3Header">
                        <h5 class="modal-title" id="modalDialog3Label">Modal title</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body" id="modalDialog3Body">
                    </div>
                    <div class="modal-footer" id="modalDialog3Footer">
                    </div>
                </div>
            </div>
        </div>
        <div id="modalDialogBusquedas" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modalDialogBusquedasLabel" aria-hidden="true">
            <div class="modal-dialog" id="modalDialogBusquedasDocument" role="document">
                <div class="modal-content">
                    <div class="modal-header" id="modalDialogBusquedasHeader">
                        <h5 class="modal-title" id="modalDialogBusquedasLabel">Modal title</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body" id="modalDialogBusquedasBody">
                    </div>
                    <div class="modal-footer" id="modalDialogBusquedasFooter">
                    </div>
                </div>
            </div>
        </div>

        <script src="/wms/js/jquery/jquery-3.5.1.js"></script>
        <script src="/wms/js/bootstrap/bootstrap.js"></script>
        <script src="/wms/js/jqx/jqxcore.js"></script>
        <script src="/wms/js/jqx/jqxdata.js"></script>
        <script src="/wms/js/jqx/jqxscrollbar.js"></script>
        <script src="/wms/js/jqx/jqxbuttons.js"></script>
        <script src="/wms/js/jqx/jqxmenu.js"></script>
        <script src="/wms/js/jqx/jqxgrid.js"></script>
        <script src="/wms/js/jqx/jqxgrid.sort.js"></script>
        <script src="/wms/js/jqx/jqxgrid.selection.js"></script>
        <script src="/wms/js/jqx/jqxgrid.edit.js"></script>
        <script src="/wms/js/jqx/jqxgrid.columnsresize.js"></script>
        <script src="/wms/js/jqx/jqxgrid.aggregates.js"></script>
        <script src="/wms/js/jqx/jqxgrid.grouping.js"></script>
        <script src="/wms/js/jquery/jquery.fileDownload.js"></script>
        <script src="/wms/js/chart.js"></script>
        <script src="/wms/js/dom.js"></script>
        <script src="/wms/js/hashmap.js"></script>
        <script src="/wms/js/wms.js"></script>
        <script src="/wms/js/busquedas.js"></script>
        <script src="/wms/js/view/catalogo.js"></script>
        <script src="/wms/js/view/maestrodetalle.js"></script>
        <script>
            if (!loggeado) {
                login();
            } else {
                main();
            }
            window.history.replaceState(null, null, "");
            window.onpopstate = function (event) {
                if (event.state) {
                    renderState(event.state);
                }
            };
        </script>
    </body>
</html>