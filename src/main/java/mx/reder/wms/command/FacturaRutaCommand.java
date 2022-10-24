package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.business.FacturaRutaBusiness;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.to.FacturaRutaResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class FacturaRutaCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(FacturaRutaCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String ruta = request.getParameter("ruta");
            String flsurtido = request.getParameter("flsurtido");

            DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
            Connection connectionAspel = databaseDataSourceAspel.getConnection();
            DatabaseServices dsAspel = new DatabaseServices(connectionAspel);

            //
            // Detalles Confirmacion
            //
            FacturaRutaResponse responseFacturaRuta;

            try {
                //
                // No hay manejo transaccional aqui, la transaccion se controla en el business
                //
                FacturaRutaBusiness factura = new FacturaRutaBusiness();
                factura.setDatabaseServices(ds);
                factura.setDatabaseAspelServices(dsAspel);
                responseFacturaRuta = factura.facturaRuta(compania, usuario, ruta, flsurtido);
            } catch(Exception e) {
                log.error(e.getMessage(), e);

                throw e;
            } finally {
                connectionAspel.close();
                databaseDataSourceAspel.close();
            }

            try (PrintWriter out = response.getWriter()) {
                out.write("{");
                out.write("\"ruta\": ");
                JSON.writeObject(out, responseFacturaRuta.ruta);
                out.write(", \"facturas\": ");
                JSON.writeArrayOfObjects(out, responseFacturaRuta.facturas);
                out.write(", \"errores\": ");
                JSON.writeArrayOfObjects(out, responseFacturaRuta.errores);
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
