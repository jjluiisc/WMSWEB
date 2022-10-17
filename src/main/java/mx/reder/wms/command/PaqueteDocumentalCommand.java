package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.business.PaqueteDocumentalBusiness;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.to.MensajeTO;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class PaqueteDocumentalCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(PaqueteDocumentalCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String ruta = request.getParameter("ruta");

            MensajeTO mensajeTO = new MensajeTO();

            DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
            Connection connectionAspel = databaseDataSourceAspel.getConnection();
            DatabaseServices dsAspel = new DatabaseServices(connectionAspel);

            File filePD;

            try {
                PaqueteDocumentalBusiness paquete = new PaqueteDocumentalBusiness();
                paquete.setDatabaseServices(ds);
                paquete.setDatabaseAspelServices(dsAspel);
                filePD = paquete.paqueteDocumental(compania, usuario, ruta);

                mensajeTO.msg = "OK";
                mensajeTO.wrn = filePD.getAbsolutePath();

            } catch(Exception e) {
                log.error(e.getMessage(), e);

                throw e;
            } finally {
                connectionAspel.close();
                databaseDataSourceAspel.close();
            }

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
