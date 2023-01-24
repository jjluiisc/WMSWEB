package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.business.CartaPorteBusiness;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.to.CartaPorteFacturaTO;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class CartaPorteFacturaCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(CartaPorteFacturaCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String idruta = request.getParameter("idruta");
            String factura = request.getParameter("factura");

            DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
            Connection connectionAspel = databaseDataSourceAspel.getConnection();
            DatabaseServices dsAspel = new DatabaseServices(connectionAspel);

            //
            // Detalles Confirmacion
            //
            ArrayList<CartaPorteFacturaTO> facturas = new ArrayList<>();

            try {
                CartaPorteBusiness bussines = new CartaPorteBusiness();
                bussines.setDatabaseServices(ds);
                bussines.setDatabaseAspelServices(dsAspel);

                facturas = bussines.cartaPorteFacturas(compania, usuario, idruta, factura);

            } catch(Exception e) {
                log.error(e.getMessage(), e);

                throw e;
            } finally {
                connectionAspel.close();
                databaseDataSourceAspel.close();
            }

            try (PrintWriter out = response.getWriter()) {
                JSON.writeArrayOfObjects(out, facturas);
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
