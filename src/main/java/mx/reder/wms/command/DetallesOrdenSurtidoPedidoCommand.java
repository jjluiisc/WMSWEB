package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDetalleDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoContenedorDAO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class DetallesOrdenSurtidoPedidoCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(DetallesOrdenSurtidoPedidoCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String flsurtido = request.getParameter("flsurtido");

            OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
            ordenSurtidoPedidoDAO.compania = compania;
            ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(flsurtido);
            if (!ds.exists(ordenSurtidoPedidoDAO))
                throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

            if (ordenSurtidoPedidoDAO.status.compareTo(Constantes.ESTADO_PENDIENTE)!=0)
                throw new WebException("El estado de esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"] ["+ordenSurtidoPedidoDAO.status+"] no es PE.");

            ArrayList<OrdenSurtidoPedidoDetalleDAO> detalles = ds.select(new OrdenSurtidoPedidoDetalleDAO(), ordenSurtidoPedidoDAO.getWhere());
            ArrayList<OrdenSurtidoPedidoContenedorDAO> contenedores = ds.select(new OrdenSurtidoPedidoContenedorDAO(), ordenSurtidoPedidoDAO.getWhere());

            //
            // Detalles Orden de Surtido
            //
            ds.beginTransaction();

            try {
                ordenSurtidoPedidoDAO.surtidor = usuario;
                ordenSurtidoPedidoDAO.fechainicio = new Date();

                ds.update(ordenSurtidoPedidoDAO, new String[] {"surtidor", "fechainicio"});

                ordenSurtidoPedidoDAO.status = Constantes.ESTADO_SURTIENDO;
                ordenSurtidoPedidoDAO.fechastatus = new Date();

                ds.update(ordenSurtidoPedidoDAO, new String[] {"status", "fechastatus"});

                OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
                Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
                ordenSurtidoPedidoBitacoraDAO.id = null;
                ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
                ordenSurtidoPedidoBitacoraDAO.usuario = usuario;
                ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_SURTIENDO;

                ds.insert(ordenSurtidoPedidoBitacoraDAO);

                ds.commit();
            } catch(Exception e) {
                ds.rollback();
                log.error(e.getMessage(), e);
                throw e;
            }

            try (PrintWriter out = response.getWriter()) {
                out.write("{");
                out.write("\"ordensurtido\": ");
                JSON.writeObject(out, ordenSurtidoPedidoDAO);
                out.write(", \"detalles\": ");
                JSON.writeArrayOfObjects(out, detalles);
                out.write(", \"contenedores\": ");
                JSON.writeArrayOfObjects(out, contenedores);
                out.write("}");
            }

        } catch(WebException e) {
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
            e.printStackTrace();

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
