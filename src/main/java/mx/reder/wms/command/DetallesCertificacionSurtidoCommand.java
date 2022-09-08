package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.business.DetallesCertificacionSurtidoBusiness;
import mx.reder.wms.to.DetallesCertificacionSurtidoResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class DetallesCertificacionSurtidoCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(DetallesCertificacionSurtidoCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String flsurtido = request.getParameter("flsurtido");

            //
            // Detalles Certificacion
            //
            DetallesCertificacionSurtidoResponse responseCertificacionSurtido;

            ds.beginTransaction();

            try {
                DetallesCertificacionSurtidoBusiness detalles = new DetallesCertificacionSurtidoBusiness();
                detalles.setDatabaseServices(ds);
                responseCertificacionSurtido = detalles.detallesCertificacion(compania, usuario, flsurtido);

                ds.commit();
            } catch(Exception e) {
                ds.rollback();
                throw e;
            }

            try (PrintWriter out = response.getWriter()) {
                out.write("{");
                out.write("\"certificacion\": ");
                JSON.writeObject(out, responseCertificacionSurtido.ordenSurtidoPedidoDAO);
                out.write(", \"lotes\": ");
                JSON.writeArrayOfObjects(out, responseCertificacionSurtido.lotes);
                out.write(", \"detalles\": ");
                JSON.writeArrayOfObjects(out, responseCertificacionSurtido.detalles);
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
