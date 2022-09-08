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
import mx.reder.wms.business.CancelaOrdenesSurtidoPedidosBusiness;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.to.MensajeTO;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class CancelaSurtidoOrdenSurtidoPedidoCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(CancelaSurtidoOrdenSurtidoPedidoCommand.class);

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String flsurtido = request.getParameter("flsurtido");
            
            DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
            Connection connectionAspel = databaseDataSourceAspel.getConnection();
            DatabaseServices dsAspel = new DatabaseServices(connectionAspel);

            //
            // Cancela Pedidos
            //
            ds.beginTransaction();
            dsAspel.beginTransaction();

            try {
                CancelaOrdenesSurtidoPedidosBusiness cancelar = new CancelaOrdenesSurtidoPedidosBusiness();
                cancelar.setDatabaseServices(ds);
                cancelar.setDatabaseAspelServices(dsAspel);
                cancelar.cancelaSurtido(compania, usuario, flsurtido);

                ds.commit();
                dsAspel.commit();
            } catch(Exception e) {
                ds.rollback();
                dsAspel.rollback();
                throw e;
            }
            
            connectionAspel.close();
            databaseDataSourceAspel.close();

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
