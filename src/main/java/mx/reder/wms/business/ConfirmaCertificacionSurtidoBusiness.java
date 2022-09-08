package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.Date;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.to.ConfirmacionSurtidoResponse;
import mx.reder.wms.to.ContenedorSurtidoTO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class ConfirmaCertificacionSurtidoBusiness {
    static Logger log = Logger.getLogger(ConfirmaCertificacionSurtidoBusiness.class);

    private DatabaseServices ds;

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }

    public ConfirmacionSurtidoResponse confirmaCertificacion(String compania, String usuario, String flsurtido) throws Exception {
        ConfirmacionSurtidoResponse response = new ConfirmacionSurtidoResponse();
        response.ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        response.ordenSurtidoPedidoDAO.compania = compania;
        response.ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(flsurtido);
        if (!ds.exists(response.ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+response.ordenSurtidoPedidoDAO+"]");

        if (response.ordenSurtidoPedidoDAO.status.compareTo("TS")!=0&&response.ordenSurtidoPedidoDAO.status.compareTo("CE")!=0)
            throw new WebException("El estado de la Orden de Surtido del Pedido ["+response.ordenSurtidoPedidoDAO.status+"] no es TS o CE.");

        response.certificadas = ds.select(new OrdenSurtidoPedidoCertificaDAO(), response.ordenSurtidoPedidoDAO.getWhere());
        response.contenedores = ds.collection(new ContenedorSurtidoTO(), "SELECT DISTINCT contenedor "
                +"FROM OrdenSurtidoPedidoCertifica WHERE compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND certificadas > 0");

        //
        response.ordenSurtidoPedidoDAO.fechaconfirmada = new Date();
        response.ordenSurtidoPedidoDAO.status = Constantes.ESTADO_CONFIRMADO;
        response.ordenSurtidoPedidoDAO.fechastatus = new Date();
        response.ordenSurtidoPedidoDAO.usuario = usuario;

        ds.update(response.ordenSurtidoPedidoDAO, new String[] {"fechaconfirmada", "status", "fechastatus", "usuario"});

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(response.ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoBitacoraDAO.id = null;
        ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO.usuario = usuario;
        ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_CONFIRMADO;

        ds.insert(ordenSurtidoPedidoBitacoraDAO);

        //
        return response;
    }
}
