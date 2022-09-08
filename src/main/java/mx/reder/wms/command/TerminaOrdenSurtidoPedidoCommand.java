package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Numero;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.business.DetallesCertificacionSurtidoBusiness;
import mx.reder.wms.business.TerminaOrdenesSurtidoPedidosBussines;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.to.MensajeTO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 *
 * @author joelbecerramiranda
 */
public class TerminaOrdenSurtidoPedidoCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(TerminaOrdenSurtidoPedidoCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String flsurtido = request.getParameter("flsurtido");
            String detalles = request.getParameter("detalles");
            String contenedores = request.getParameter("contenedores");
            String msg = "";

            log.debug(detalles);
            JSONArray arraydetalles = (JSONArray)JSONValue.parse(detalles);

            log.debug(contenedores);
            JSONArray arraycontenedores = (JSONArray)JSONValue.parse(contenedores);

            OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
            ordenSurtidoPedidoDAO.compania = compania;
            ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(flsurtido);
            if (!ds.exists(ordenSurtidoPedidoDAO))
                throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

            //
            //
            //
            ds.beginTransaction();

            try {
                TerminaOrdenesSurtidoPedidosBussines termina = new TerminaOrdenesSurtidoPedidosBussines();
                termina.setDatabaseServices(ds);
                ordenSurtidoPedidoDAO = termina.termina(compania, usuario, flsurtido, arraydetalles, arraycontenedores, msg);
                
                if(ordenSurtidoPedidoDAO.status.equals(Constantes.ESTADO_TERMINASURTIDO)){
                    DetallesCertificacionSurtidoBusiness certifica = new DetallesCertificacionSurtidoBusiness();
                    certifica.setDatabaseServices(ds);
                    certifica.detallesCertificacion(compania, usuario, flsurtido);
                }

                ds.commit();
            } catch(Exception e) {
                ds.rollback();
                log.error(e.getMessage(), e);
                throw e;
            } finally {
                log.debug("Cerrando KCM.");
            }

            //try (PrintWriter out = response.getWriter()) {
            //    JSON.writeObject(out, ordenSurtidoPedidoDAO);
            //}
            MensajeTO mensajeTO = new MensajeTO();
            mensajeTO.msg = ordenSurtidoPedidoDAO.getMsg();
            
            log.debug("Cierre OrdenSurtido : " + mensajeTO.msg);
            
            try (PrintWriter out = response.getWriter()) {
                out.write("{");
                out.write("\"ordensurtido\": ");
                JSON.writeObject(out, ordenSurtidoPedidoDAO);
                out.write(", \"resmsg\": ");
                JSON.writeObject(out, mensajeTO);
                out.write("}");
            }

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
