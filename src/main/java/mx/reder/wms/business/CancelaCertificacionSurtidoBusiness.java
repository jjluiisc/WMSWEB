package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.Date;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoLotesDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDetalleDAO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class CancelaCertificacionSurtidoBusiness {
    static Logger log = Logger.getLogger(CancelaCertificacionSurtidoBusiness.class);

    private DatabaseServices ds;    

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }
    
    public void cancelaCertificacion(String compania, String usuario, String flsurtido) throws Exception {
        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = compania;
        ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(flsurtido);
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

        if (ordenSurtidoPedidoDAO.status.compareTo(Constantes.ESTADO_CERTIFICANDO)!=0)
            throw new WebException("El estado de esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"] ["+ordenSurtidoPedidoDAO.status+"] no es CE.");

        //
        ordenSurtidoPedidoDAO.certificadas = 0.0d;

        ds.update(ordenSurtidoPedidoDAO, new String[] {"certificadas"});

        OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
        ordenSurtidoPedidoDetalleDAO.compania = ordenSurtidoPedidoDAO.compania;
        ordenSurtidoPedidoDetalleDAO.flsurtido = ordenSurtidoPedidoDAO.flsurtido;
        ordenSurtidoPedidoDetalleDAO.certificadas = 0.0d;

        ds.update(ordenSurtidoPedidoDetalleDAO, new String[] {"certificadas"}, ordenSurtidoPedidoDAO.getWhere());

        // Lo regreso al estado anterior
        ordenSurtidoPedidoDAO.status = Constantes.ESTADO_TERMINASURTIDO;
        ordenSurtidoPedidoDAO.fechastatus = new Date();
        ordenSurtidoPedidoDAO.usuario = usuario;

        ds.update(ordenSurtidoPedidoDAO, new String[] {"status", "fechastatus", "usuario"});

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoBitacoraDAO.id = null;
        ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_TERMINASURTIDO;
        ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO.usuario = usuario;

        ds.insert(ordenSurtidoPedidoBitacoraDAO);

        // Borro los detalles de certificacion
        ds.delete(new OrdenSurtidoPedidoLotesDAO(), ordenSurtidoPedidoDAO.getWhere());
        ds.delete(new OrdenSurtidoPedidoCertificaDAO(), ordenSurtidoPedidoDAO.getWhere());
    }
}
