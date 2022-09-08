package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.ArrayList;
import java.util.Date;
import mx.reder.wms.dao.GenericDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDetalleDAO;
import mx.reder.wms.dao.entity.PedidoBitacoraDAO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class CancelaOrdenesSurtidoPedidosBusiness {
    static Logger log = Logger.getLogger(CancelaOrdenesSurtidoPedidosBusiness.class);

    private DatabaseServices ds;
    private DatabaseServices dsAspel;

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }
    
    public void setDatabaseAspelServices(DatabaseServices dsAspel) {
        this.dsAspel = dsAspel;
    }

    public void cancelaPendiente(String compania, String usuario, String flsurtido) throws Exception {
        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = compania;
        ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(flsurtido);
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

        if (ordenSurtidoPedidoDAO.status.compareTo(Constantes.ESTADO_PENDIENTE)!=0)
            throw new WebException("El estado de esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"] ["+ordenSurtidoPedidoDAO.status+"] no es PE.");

        ordenSurtidoPedidoDAO.status = Constantes.ESTADO_CANCELADO;
        ordenSurtidoPedidoDAO.fechastatus = new Date();
        ordenSurtidoPedidoDAO.usuario = usuario;

        ds.update(ordenSurtidoPedidoDAO, new String[] {"status", "fechastatus", "usuario"});

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoBitacoraDAO.id = null;
        ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_CANCELADO;
        ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO.usuario = usuario;

        ds.insert(ordenSurtidoPedidoBitacoraDAO);

        String wherePedido = "compania = '"+compania+"' AND pedido = '"+ordenSurtidoPedidoDAO.pedido+"'";
        int count = ds.count(ordenSurtidoPedidoDAO, wherePedido);
        int countCA = ds.count(ordenSurtidoPedidoDAO, wherePedido+" AND status = 'CA'");

        if (count==countCA) {
            ASPELPedidoDAO aspelPedidoDAO = new ASPELPedidoDAO();
            aspelPedidoDAO.setEmpresa(compania);
            aspelPedidoDAO.CVE_DOC = ordenSurtidoPedidoDAO.pedido;
            boolean existeAspelPedidoDAO = false;
            existeAspelPedidoDAO = dsAspel.exists(aspelPedidoDAO);
            
            if (!existeAspelPedidoDAO)
                throw new WebException("Este Pedido no existe ["+aspelPedidoDAO+"]");

            //
            aspelPedidoDAO.STATUS = Constantes.ESTADO_ASPEL_ORIGINAL;

            ds.update(aspelPedidoDAO, new String[] {"STATUS"});

            PedidoBitacoraDAO pedidoBitacoraDAO = new PedidoBitacoraDAO();
            pedidoBitacoraDAO.id = null;
            pedidoBitacoraDAO.compania = compania;
            pedidoBitacoraDAO.pedido = aspelPedidoDAO.CVE_DOC;
            pedidoBitacoraDAO.status = Constantes.ESTADO_PENDIENTE;
            pedidoBitacoraDAO.fechabitacora = new Date();
            pedidoBitacoraDAO.usuario = usuario;

            ds.insert(pedidoBitacoraDAO);
        }
    }

    public void cancelaSurtido(String compania, String usuario, String flsurtido) throws Exception {
        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = compania;
        ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(flsurtido);
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

        if (ordenSurtidoPedidoDAO.status.compareTo(Constantes.ESTADO_SURTIENDO)!=0)
            throw new WebException("El estado de esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"] ["+ordenSurtidoPedidoDAO.status+"] no es SU.");

        //
        // Primero Cancelo esta Orden de Surtido
        //

        ordenSurtidoPedidoDAO.status = Constantes.ESTADO_CANCELADO;
        ordenSurtidoPedidoDAO.fechastatus = new Date();
        ordenSurtidoPedidoDAO.usuario = usuario;

        ds.update(ordenSurtidoPedidoDAO, new String[] {"status", "fechastatus", "usuario"});

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoBitacoraDAO.id = null;
        ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_CANCELADO;
        ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO.usuario = usuario;

        ds.insert(ordenSurtidoPedidoBitacoraDAO);

        ArrayList<OrdenSurtidoPedidoDetalleDAO> ordenesSurtidoPedidoDetalles = ds.select(new OrdenSurtidoPedidoDetalleDAO(), ordenSurtidoPedidoDAO.getWhere());

        //
        // Despues Genero una nueva Orden de Surtido
        //

        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO1 = new OrdenSurtidoPedidoDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoDAO1);

        ordenSurtidoPedidoDAO1.compania = ordenSurtidoPedidoDAO.compania;
        ordenSurtidoPedidoDAO1.flsurtido = GenericDAO.obtenerSiguienteFolio(ds, ordenSurtidoPedidoDAO.compania, Constantes.FOLIO_ORDENSURTIDO);
        ordenSurtidoPedidoDAO1.status = Constantes.ESTADO_PENDIENTE;
        ordenSurtidoPedidoDAO1.fechastatus = new Date();
        ordenSurtidoPedidoDAO1.usuario = usuario;
        ordenSurtidoPedidoDAO1.surtidor = "";
        ordenSurtidoPedidoDAO1.fechasurtido = new Date();
        ordenSurtidoPedidoDAO1.fechainicio = null;
        ordenSurtidoPedidoDAO1.fechatermino = null;
        ordenSurtidoPedidoDAO1.cantidad = 0.0;
        ordenSurtidoPedidoDAO1.surtidas = 0.0;
        ordenSurtidoPedidoDAO1.total = 0.0;
        ordenSurtidoPedidoDAO1.detalles = 0;

        ds.insert(ordenSurtidoPedidoDAO1);

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO1 = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO1, ordenSurtidoPedidoBitacoraDAO1);
        ordenSurtidoPedidoBitacoraDAO1.id = null;
        ordenSurtidoPedidoBitacoraDAO1.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO1.usuario = usuario;
        ordenSurtidoPedidoBitacoraDAO1.status = Constantes.ESTADO_PENDIENTE;

        ds.insert(ordenSurtidoPedidoBitacoraDAO1);

        int partida = 0;
        for(OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO : ordenesSurtidoPedidoDetalles) {
            OrdenSurtidoPedidoDetalleDAO ordenSurtidoDetalleDAO1 = new OrdenSurtidoPedidoDetalleDAO();
            Reflector.copyAllFields(ordenSurtidoPedidoDetalleDAO, ordenSurtidoDetalleDAO1);

            ordenSurtidoDetalleDAO1.compania = ordenSurtidoPedidoDAO1.compania;
            ordenSurtidoDetalleDAO1.flsurtido = ordenSurtidoPedidoDAO1.flsurtido;
            ordenSurtidoDetalleDAO1.partida = ++partida;
            ordenSurtidoDetalleDAO1.surtidas = 0.0;

            ds.insert(ordenSurtidoDetalleDAO1);

            ordenSurtidoPedidoDAO1.cantidad += ordenSurtidoDetalleDAO1.cantidad;
            ordenSurtidoPedidoDAO1.surtidas += ordenSurtidoDetalleDAO1.surtidas;
            ordenSurtidoPedidoDAO1.total += ordenSurtidoDetalleDAO1.total;
            ordenSurtidoPedidoDAO1.detalles ++;
        }

        ds.update(ordenSurtidoPedidoDAO1, new String[] {"cantidad", "surtidas", "total", "detalles"});

        //
        // Ultimo reviso el estado del Pedido
        //

        String wherePedido = "compania = '"+compania+"' AND pedido = '"+ordenSurtidoPedidoDAO.pedido+"'";
        int count = ds.count(ordenSurtidoPedidoDAO, wherePedido);
        int countPE = ds.count(ordenSurtidoPedidoDAO, wherePedido+" AND status IN ('CA','PE')");

        if (count==countPE) {
            ASPELPedidoDAO aspelPedidoDAO = new ASPELPedidoDAO();
            aspelPedidoDAO.setEmpresa(compania);
            aspelPedidoDAO.CVE_DOC = ordenSurtidoPedidoDAO.pedido;
            boolean existsAspelPedidoDAO = false;
            existsAspelPedidoDAO = dsAspel.exists(aspelPedidoDAO);
            if (!existsAspelPedidoDAO)
                throw new WebException("Este Pedido no existe ["+aspelPedidoDAO+"]");
            
            //
            aspelPedidoDAO.STATUS = Constantes.ESTADO_ASPEL_SURTIENDO;

            ds.update(aspelPedidoDAO, new String[] {"STATUS"});

            PedidoBitacoraDAO pedidoBitacoraDAO = new PedidoBitacoraDAO();
            pedidoBitacoraDAO.id = null;
            pedidoBitacoraDAO.compania = compania;
            pedidoBitacoraDAO.pedido = aspelPedidoDAO.CVE_DOC;
            pedidoBitacoraDAO.status = Constantes.ESTADO_PORSURTIR;
            pedidoBitacoraDAO.fechabitacora = new Date();
            pedidoBitacoraDAO.usuario = usuario;

            ds.insert(pedidoBitacoraDAO);
        }
    }

    public void cancelaTermino(String compania, String usuario, String flsurtido) throws Exception {
        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = compania;
        ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(flsurtido);
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

        if (ordenSurtidoPedidoDAO.status.compareTo(Constantes.ESTADO_TERMINASURTIDO)!=0)
            throw new WebException("El estado de esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"] ["+ordenSurtidoPedidoDAO.status+"] no es TS.");

        ordenSurtidoPedidoDAO.status = Constantes.ESTADO_CANCELADO;
        ordenSurtidoPedidoDAO.fechastatus = new Date();
        ordenSurtidoPedidoDAO.usuario = usuario;

        ds.update(ordenSurtidoPedidoDAO, new String[] {"status", "fechastatus", "usuario"});

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoBitacoraDAO.id = null;
        ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_CANCELADO;
        ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO.usuario = usuario;

        ds.insert(ordenSurtidoPedidoBitacoraDAO);

        String wherePedido = "compania = '"+compania+"' AND pedido = '"+ordenSurtidoPedidoDAO.pedido+"'";
        int count = ds.count(ordenSurtidoPedidoDAO, wherePedido);
        int countCA = ds.count(ordenSurtidoPedidoDAO, wherePedido+" AND status = 'CA'");

        if (count==countCA) {
            ASPELPedidoDAO aspelPedidoDAO = new ASPELPedidoDAO();
            aspelPedidoDAO.setEmpresa(compania);
            aspelPedidoDAO.CVE_DOC = ordenSurtidoPedidoDAO.pedido;
            boolean existsAspelPedidoDAO = false;
            existsAspelPedidoDAO = dsAspel.exists(aspelPedidoDAO);
            if (!existsAspelPedidoDAO)
                throw new WebException("Este Pedido no existe ["+aspelPedidoDAO+"]");
            
            //
            aspelPedidoDAO.STATUS = Constantes.ESTADO_ASPEL_ORIGINAL;

            dsAspel.update(aspelPedidoDAO, new String[] {"STATUS"});
            
            PedidoBitacoraDAO pedidoBitacoraDAO = new PedidoBitacoraDAO();
            pedidoBitacoraDAO.id = null;
            pedidoBitacoraDAO.compania = compania;
            pedidoBitacoraDAO.pedido = aspelPedidoDAO.CVE_DOC;
            pedidoBitacoraDAO.status = Constantes.ESTADO_PENDIENTE;
            pedidoBitacoraDAO.fechabitacora = new Date();
            pedidoBitacoraDAO.usuario = usuario;

            ds.insert(pedidoBitacoraDAO);
        }
    }
}
