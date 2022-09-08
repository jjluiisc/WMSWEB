package mx.reder.wms.command.app;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Numero;
import java.math.BigDecimal;
import mx.reder.wms.business.InventarioBussines;
import mx.reder.wms.business.InventarioCapturaBussines;
import mx.reder.wms.collection.AnalisisInventarioCollection;
import mx.reder.wms.dao.GenericDAO;
import mx.reder.wms.dao.entity.InventarioCapturaDAO;
import mx.reder.wms.dao.entity.InventarioCapturaDetalleDAO;
import mx.reder.wms.dao.entity.InventarioConteoUbicacionDAO;
import mx.reder.wms.dao.entity.InventarioDAO;
import mx.reder.wms.to.MensajeTO;
import mx.reder.wms.util.Constantes;
import java.util.Date;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author jbecerra
 */
public class TerminaAnalisisInventarioCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(TerminaAnalisisInventarioCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String terminal = request.getParameter("terminal");
            String usuario = request.getParameter("usuario");
            String analisis = request.getParameter("analisis");
            String detalles = request.getParameter("detalles");

            log.debug(analisis);
            JSONObject analisisInventario = (JSONObject)JSONValue.parse(analisis);

            log.debug(detalles);
            JSONArray array = (JSONArray)JSONValue.parse(detalles);

            //
            //
            //
            InventarioBussines inventarioBussines = new InventarioBussines();
            InventarioDAO inventarioDAO = inventarioBussines.buscaInventario(ds, compania);
            if (inventarioDAO==null)
                throw new WebException("No existe Status de Inventario de la Compania ["+compania+"].");

            if (inventarioDAO.fase==null)
                throw new WebException("No se a realizado la Carga del Inventario ["+inventarioDAO.status+"].");
            if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_1ERCONTEO)==0
                    ||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_2DOCONTEO)==0
                    ||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_3ERCONTEO)==0))
                throw new WebException("La fase del Inventario es incorrecta ["+inventarioDAO.fase+"].");

            AnalisisInventarioCollection analisisInventarioCollection = new AnalisisInventarioCollection();
            JSON.fromJson(analisisInventario, analisisInventarioCollection);

            if (analisisInventarioCollection.flinventario!=inventarioDAO.flinventario)
                throw new WebException("El folio de Inventario del Analisis de Inventario ["+analisisInventarioCollection.flinventario
                        +"] no es igual al folio del Inventario ["+inventarioDAO.flinventario+"].");

            try {
                ds.beginTransaction();

                int flcapturainventario = GenericDAO.obtenerSiguienteFolio(ds, compania, Constantes.FOLIO_CAPTURA_INVENTARIO);

                InventarioCapturaDAO inventarioCapturaDAO = new InventarioCapturaDAO();
                inventarioCapturaDAO.compania = compania;
                inventarioCapturaDAO.flcapturainventario = flcapturainventario;
                inventarioCapturaDAO.flinventario = inventarioDAO.flinventario;
                inventarioCapturaDAO.fecreacion = new Date();
                inventarioCapturaDAO.status = Constantes.ESTADO_PENDIENTE;
                inventarioCapturaDAO.femodificacion = null;
                inventarioCapturaDAO.fetermino = null;
                inventarioCapturaDAO.usuario = usuario;
                inventarioCapturaDAO.terminal = terminal;
                inventarioCapturaDAO.fase = inventarioDAO.fase;

                ds.insert(inventarioCapturaDAO);

                //
                // Detalles
                //
                for (int i=0; i<array.size(); i++) {
                    JSONObject detalle = (JSONObject)array.get(i);

                    InventarioConteoUbicacionDAO inventarioConteoUbicacionDAO0 = new InventarioConteoUbicacionDAO();
                    JSON.fromJson(detalle, inventarioConteoUbicacionDAO0);

                    InventarioConteoUbicacionDAO inventarioConteoUbicacionDAO1 = new InventarioConteoUbicacionDAO();
                    inventarioConteoUbicacionDAO1.flduinventario = inventarioConteoUbicacionDAO0.flduinventario;
                    if (!ds.exists(inventarioConteoUbicacionDAO1))
                        throw new WebException("Este Conteo de Inventario por Ubicacion ["+inventarioConteoUbicacionDAO1+"] no existe.");

                    if (inventarioConteoUbicacionDAO0.flinventario!=inventarioConteoUbicacionDAO1.flinventario)
                        throw new WebException("El folio de Inventario del Conteo de Inventario por Ubicacion ["+inventarioConteoUbicacionDAO0.flinventario
                                +"] no es igual al folio del Inventario ["+inventarioConteoUbicacionDAO1.flinventario+"].");
                    if (inventarioConteoUbicacionDAO0.fldinventario!=inventarioConteoUbicacionDAO1.fldinventario)
                        throw new WebException("El folio del Detalle de Inventario del Conteo de Inventario por Ubicacion ["+inventarioConteoUbicacionDAO0.fldinventario
                                +"] no es igual al folio del Detalle de Inventario ["+inventarioConteoUbicacionDAO1.fldinventario+"].");
                    if (inventarioConteoUbicacionDAO0.codigo.compareTo(inventarioConteoUbicacionDAO1.codigo)!=0)
                        throw new WebException("La Clave del Detalle del Conteo de Inventario por Ubicacion ["+inventarioConteoUbicacionDAO0.codigo
                                +"] no es igual a la Clave del Detalle de Inventario ["+inventarioConteoUbicacionDAO1.codigo+"].");
                    if (inventarioConteoUbicacionDAO0.ubicacion.compareTo(inventarioConteoUbicacionDAO1.ubicacion)!=0)
                        throw new WebException("La Ubicación del Detalle del Conteo de Inventario por Ubicacion ["+inventarioConteoUbicacionDAO0.ubicacion
                                +"] no es igual a la Ubicación del Detalle de Inventario ["+inventarioConteoUbicacionDAO1.ubicacion+"].");
                    if (inventarioConteoUbicacionDAO0.lote.compareTo(inventarioConteoUbicacionDAO1.lote)!=0)
                        throw new WebException("El Lote del Detalle del Conteo de Inventario por Ubicacion ["+inventarioConteoUbicacionDAO0.lote
                                +"] no es igual al Lote del Detalle de Inventario ["+inventarioConteoUbicacionDAO1.lote+"].");

                    //
                    // No se verifica porque pudo haber cambiado en algun otra captura de inventario,
                    //  de cualquier forma el ajuste se considera con la cantidad contada de la ubicacion
                    //
                    //if (inventarioConteoUbicacionDAO0.existenciac!=inventarioConteoUbicacionDAO1.existenciac)
                    //    throw new WebException("La Existencia del Detalle del Conteo de Inventario por Ubicacion ["+inventarioConteoUbicacionDAO0.existencia
                    //            +"] no es igual a la Existencia del Detalle de Inventario ["+inventarioConteoUbicacionDAO1.existencia+"].");

                    if (!detalle.containsKey("verificado"))
                        throw new WebException("El Conteo de Inventario por Ubicacion no trae el atributo de Verificado.");

                    BigDecimal verificado = Numero.getBigDecimal(Double.parseDouble(detalle.get("verificado").toString()));
                    BigDecimal ajuste = verificado.subtract(inventarioConteoUbicacionDAO0.existenciac);

                    InventarioCapturaDetalleDAO inventarioCapturaDetalleDAO = new InventarioCapturaDetalleDAO();
                    inventarioCapturaDetalleDAO.fldcapturainventario = null;
                    inventarioCapturaDetalleDAO.compania = compania;
                    inventarioCapturaDetalleDAO.flcapturainventario = inventarioCapturaDAO.flcapturainventario;
                    inventarioCapturaDetalleDAO.flinventario = inventarioCapturaDAO.flinventario;
                    inventarioCapturaDetalleDAO.status = Constantes.ESTADO_PENDIENTE;
                    inventarioCapturaDetalleDAO.codigo = inventarioConteoUbicacionDAO0.codigo;
                    inventarioCapturaDetalleDAO.descripcion = inventarioConteoUbicacionDAO0.descripcion;
                    inventarioCapturaDetalleDAO.ubicacion = inventarioConteoUbicacionDAO0.ubicacion;
                    inventarioCapturaDetalleDAO.lote = inventarioConteoUbicacionDAO0.lote;
                    inventarioCapturaDetalleDAO.fecaducidad = inventarioConteoUbicacionDAO0.fecaducidad;
                    inventarioCapturaDetalleDAO.cantidad = ajuste;

                    ds.insert(inventarioCapturaDetalleDAO);
                }

                //
                //
                //
                InventarioCapturaBussines inventarioCapturaBussines = new InventarioCapturaBussines();
                inventarioCapturaBussines.terminaInventarioCaptura(ds, inventarioCapturaDAO);

                //
                // Desactiva el analisis de este de inventario
                //
                ds.update("UPDATE InventarioConteo SET status = 'TE', terminal = NULL "
                        +"WHERE fldinventario = "+analisisInventarioCollection.fldinventario+" "
                        +"AND flinventario = "+analisisInventarioCollection.flinventario);

                ds.commit();
            } catch(Exception e) {
                ds.rollback();

                log.error(e.getMessage(), e);
                throw e;
            }
            //
            //
            //

            MensajeTO mensajeTO = new MensajeTO();
            mensajeTO.msg = "OK";

            PrintWriter out = response.getWriter();
            JSON.writeObject(out, mensajeTO);
            out.close();

        } catch(WebException e) {
            log.error(e.getMessage(), e);

            ErrorTO errorTO = new ErrorTO();
            errorTO.fromException(e);

            try {
                PrintWriter out = response.getWriter();
                JSON.writeObject(out, errorTO);
                out.close();

            } catch(Exception ex) {
                throw new WebException(ex.getMessage());
            }

        } catch(Exception e) {
            log.error(e.getMessage(), e);

            ErrorTO errorTO = new ErrorTO();
            errorTO.fromException(e);

            try {
                PrintWriter out = response.getWriter();
                JSON.writeObject(out, errorTO);
                out.close();

            } catch(Exception ex) {
                throw new WebException(ex.getMessage());
            }
        }
    }
}
