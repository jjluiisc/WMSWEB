package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.collection.OrdenesSurtidoPedidoCollection;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class ListaOrdenesSurtidoPedidoCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(ListaOrdenesSurtidoPedidoCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");

            String where = "osp.compania = '"+compania+"' AND osp.status = 'PE' "
                    +"AND osp.equipo IN (SELECT equipo FROM Surtidor WHERE surtidor = '"+usuario+"')";
            OrdenesSurtidoPedidoCollection ordenesSurtidoPedidoCollection = new OrdenesSurtidoPedidoCollection();
            ArrayList ordenesSurtidoPedido = ds.collection(new OrdenesSurtidoPedidoCollection(),
                    ordenesSurtidoPedidoCollection.getSQL(where)+" ORDER BY osp.fechasurtido");

            try (PrintWriter out = response.getWriter()) {
                JSON.writeArrayOfObjects(out, ordenesSurtidoPedido);
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
