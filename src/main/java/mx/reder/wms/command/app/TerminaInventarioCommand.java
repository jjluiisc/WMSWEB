package mx.reder.wms.command.app;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Fecha;
import com.atcloud.util.Numero;
import java.util.Date;
import mx.reder.wms.business.InventarioBussines;
import mx.reder.wms.business.InventarioCapturaBussines;
import mx.reder.wms.dao.GenericDAO;
import mx.reder.wms.dao.entity.CompaniaDAO;
import mx.reder.wms.dao.entity.InventarioCapturaDAO;
import mx.reder.wms.dao.entity.InventarioCapturaDetalleDAO;
import mx.reder.wms.dao.entity.InventarioDAO;
import mx.reder.wms.to.MensajeTO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author jbecerra
 */
public class TerminaInventarioCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(TerminaInventarioCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String folio = request.getParameter("folio");
            String terminal = request.getParameter("terminal");
            String usuario = request.getParameter("usuario");
            String detalles = request.getParameter("detalles");

            //
            //
            //
            CompaniaDAO companiaDAO = new CompaniaDAO(compania);
            if (!ds.exists(companiaDAO))
                throw new WebException("No existe esta Compania ["+companiaDAO+"].");

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
                inventarioCapturaDAO.terminal = terminal==null ? "" : terminal;
                inventarioCapturaDAO.fase = inventarioDAO.fase;

                ds.insert(inventarioCapturaDAO);

                //
                // Detalles
                //
                for (int i=0; i<array.size(); i++) {
                    JSONObject detalle = (JSONObject)array.get(i);

                    InventarioCapturaDetalleDAO inventarioCapturaDetalleDAO = new InventarioCapturaDetalleDAO();
                    inventarioCapturaDetalleDAO.fldcapturainventario = null;
                    inventarioCapturaDetalleDAO.compania = compania;
                    inventarioCapturaDetalleDAO.flcapturainventario = inventarioCapturaDAO.flcapturainventario;
                    inventarioCapturaDetalleDAO.flinventario = inventarioCapturaDAO.flinventario;
                    inventarioCapturaDetalleDAO.status = Constantes.ESTADO_PENDIENTE;
                    inventarioCapturaDetalleDAO.codigo = (String)detalle.get("codigo");
                    inventarioCapturaDetalleDAO.descripcion = (String)detalle.get("descripcion");
                    inventarioCapturaDetalleDAO.ubicacion = (String)detalle.get("ubicacion");
                    inventarioCapturaDetalleDAO.lote = (String)detalle.get("lote");
                    inventarioCapturaDetalleDAO.fecaducidad = Fecha.getFecha((String)detalle.get("fecaducidad"));
                    inventarioCapturaDetalleDAO.cantidad = Numero.getBigDecimal(Double.parseDouble(detalle.get("cantidad").toString()));

                    ds.insert(inventarioCapturaDetalleDAO);
                }

                //
                //
                //
                InventarioCapturaBussines inventarioCapturaBussines = new InventarioCapturaBussines();
                inventarioCapturaBussines.terminaInventarioCaptura(ds, inventarioCapturaDAO);

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
