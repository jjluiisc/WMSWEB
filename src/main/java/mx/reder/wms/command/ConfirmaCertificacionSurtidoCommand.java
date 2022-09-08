package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.business.ConfirmaCertificacionSurtidoBusiness;
import mx.reder.wms.to.ConfirmacionSurtidoResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class ConfirmaCertificacionSurtidoCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(ConfirmaCertificacionSurtidoCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String flsurtido = request.getParameter("flsurtido");

            //
            // Detalles Confirmacion
            //
            ConfirmacionSurtidoResponse responseConfirmacionSurtido;

            ds.beginTransaction();

            try {
                ConfirmaCertificacionSurtidoBusiness confirma = new ConfirmaCertificacionSurtidoBusiness();
                confirma.setDatabaseServices(ds);
                responseConfirmacionSurtido = confirma.confirmaCertificacion(compania, usuario, flsurtido);

                ds.commit();
            } catch(Exception e) {
                ds.rollback();
                throw e;
            }

            try (PrintWriter out = response.getWriter()) {
                out.write("{");
                out.write("\"ordensurtido\": ");
                JSON.writeObject(out, responseConfirmacionSurtido.ordenSurtidoPedidoDAO);
                out.write(", \"detalles\": ");
                JSON.writeArrayOfObjects(out, responseConfirmacionSurtido.certificadas);
                out.write(", \"contenedores\": ");
                JSON.writeArrayOfObjects(out, responseConfirmacionSurtido.contenedores);
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
