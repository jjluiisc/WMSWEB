package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.business.CancelaCertificacionSurtidoBusiness;
import mx.reder.wms.to.MensajeTO;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class CancelaCertificacionSurtidoCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(CancelaCertificacionSurtidoCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String flsurtido = request.getParameter("flsurtido");

            //
            // Cancela Certificacion
            //
            ds.beginTransaction();

            try {
                CancelaCertificacionSurtidoBusiness cancelar = new CancelaCertificacionSurtidoBusiness();
                cancelar.setDatabaseServices(ds);
                cancelar.cancelaCertificacion(compania, usuario, flsurtido);

                ds.commit();
            } catch(Exception e) {
                ds.rollback();
                throw e;
            }

            MensajeTO mensajeTO = new MensajeTO();
            mensajeTO.msg = "OK";

            try (PrintWriter out = response.getWriter()) {
                JSON.writeObject(out, mensajeTO);
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
