package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.dao.entity.RutaDAO;
import mx.reder.wms.to.MensajeTO;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class CierraRutaCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(CierraRutaCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String ruta = request.getParameter("ruta");

            RutaDAO rutaDAO = (RutaDAO)ds.first(new RutaDAO(), "compania = '"+compania+"' AND ruta = '"+ruta+"' AND status = 'FA' AND fechacierre IS NULL", "id DESC");
            if (rutaDAO==null)
                throw new WebException("No encontre esta Ruta ["+compania+";"+ruta+"] con estado FA y fechacierre IS NULL");

            MensajeTO mensajeTO = new MensajeTO();

            try {
                rutaDAO.status = "CE";
                rutaDAO.fechacierre = new Date();
                ds.update(rutaDAO, new String[] {"status", "fechacierre"});

                mensajeTO.msg = "OK";

            } catch(Exception e) {
                log.error(e.getMessage(), e);

                throw e;
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
