package mx.reder.wms.command.app;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseRecordEntity;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import java.util.ArrayList;
import mx.reder.wms.to.OrdenSurtidoTicketTO;
import org.apache.log4j.Logger;

/**
 *
 * @author jbecerra
 */
public class OrdenSurtidoTicketCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(OrdenSurtidoTicketCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String flsurtido = request.getParameter("flsurtido");

            //
            //
            //
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT OSP.compania, (SELECT TOP 1 C.razonsocial FROM Compania C WHERE C.compania  = '"+compania+"') AS razonsocialcompania, ")
            .append("OSP.flsurtido, OSPC.contenedor, OSP.pedido, OSP.cliente, OSP.nombrecliente, OSP.ruta  FROM OrdenSurtidoPedido OSP ")
            .append("LEFT OUTER JOIN OrdenSurtidoPedidoCertifica OSPC ON OSP.compania = OSPC.compania AND OSP.flsurtido = OSPC.flsurtido ")
            .append("WHERE 1 = 1 AND ").append("OSPC.compania = '"+compania+"' AND OSPC.flsurtido = "+flsurtido).append(" ")
            .append("UNION ")
            .append("SELECT OSP.compania, (SELECT TOP 1 C.razonsocial FROM Compania C WHERE C.compania  = '"+compania+"') AS razonsocialcompania, ")
            .append("OSP.flsurtido, OSPC.contenedor, OSP.pedido, OSP.cliente, OSP.nombrecliente, OSP.ruta  FROM OrdenSurtidoPedido OSP  ")
            .append("LEFT OUTER JOIN OrdenSurtidoPedidoContenedor OSPC ON OSP.compania = OSPC.compania AND OSP.flsurtido = OSPC.flsurtido ")
            .append("WHERE 1 = 1 AND ").append("OSPC.compania = '"+compania+"' AND OSPC.flsurtido = "+flsurtido).append(" "); 
            
            OrdenSurtidoTicketTO ordenSurtidoTicketTO = new OrdenSurtidoTicketTO();
            ArrayList<DatabaseRecordEntity> arrordensurtido = ds.collection(sql.toString());
            for(DatabaseRecordEntity record : arrordensurtido) {
                ordenSurtidoTicketTO.compania = record.getString("compania");
                ordenSurtidoTicketTO.razonsocialcompania = record.getString("razonsocialcompania");
                ordenSurtidoTicketTO.flsurtido = record.getInt("flsurtido");
                ordenSurtidoTicketTO.contenedor = record.getString("contenedor");
                ordenSurtidoTicketTO.pedido = record.getString("pedido");
                ordenSurtidoTicketTO.cliente = record.getString("cliente");
                ordenSurtidoTicketTO.nombrecliente = record.getString("nombrecliente");
                ordenSurtidoTicketTO.ruta = record.getString("ruta");
            }

            try (PrintWriter out = response.getWriter()) {
                out.write("{");
                out.write("\"ordensurtido\": ");
                JSON.writeObject(out, ordenSurtidoTicketTO);
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
