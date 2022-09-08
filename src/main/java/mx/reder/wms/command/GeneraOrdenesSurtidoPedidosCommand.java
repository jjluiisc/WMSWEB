package mx.reder.wms.command;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import java.sql.Connection;
import java.util.ArrayList;
import mx.reder.wms.business.GeneraOrdenesSurtidoBusiness;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author jbecerra
 */
public class GeneraOrdenesSurtidoPedidosCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(GeneraOrdenesSurtidoPedidosCommand.class);

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String registros = request.getParameter("registros");

            ArrayList surtidos = null;
            
            DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
            Connection connectionAspel = databaseDataSourceAspel.getConnection();
            DatabaseServices dsAspel = new DatabaseServices(connectionAspel);

            ds.beginTransaction();
            dsAspel.beginTransaction();
            //
            // Genera Ordenes Surtido
            //
            try {
                GeneraOrdenesSurtidoBusiness generator = new GeneraOrdenesSurtidoBusiness();
                generator.setDatabaseServices(ds);
                generator.setDatabaseAspelServices(dsAspel);
                generator.genera(registros, compania, usuario);
                surtidos = generator.getSurtidos();

                ds.commit();
                dsAspel.commit();
            } catch(Exception e) {
                ds.rollback();
                dsAspel.rollback();
                log.error(e.getMessage(), e);
                throw e;
            }
            
            connectionAspel.close();
            databaseDataSourceAspel.close();

            try (PrintWriter out = response.getWriter()) {
                JSON.writeArrayOfObjects(out, surtidos);
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