package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseRecordEntity;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.ArrayList;
import java.util.Date;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoLotesDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDetalleDAO;
import mx.reder.wms.to.DetallesCertificacionSurtidoResponse;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class DetallesCertificacionSurtidoBusiness {
    static Logger log = Logger.getLogger(DetallesCertificacionSurtidoBusiness.class);

    private DatabaseServices ds;

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }

    public DetallesCertificacionSurtidoResponse detallesCertificacion(String compania, String usuario, String flsurtido) throws Exception {
        DetallesCertificacionSurtidoResponse response = new DetallesCertificacionSurtidoResponse();
        response.ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        response.ordenSurtidoPedidoDAO.compania = compania;
        response.ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(flsurtido);
        if (!ds.exists(response.ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+response.ordenSurtidoPedidoDAO+"]");

        if (response.ordenSurtidoPedidoDAO.status.compareTo("TS")!=0&&response.ordenSurtidoPedidoDAO.status.compareTo("CE")!=0)
            throw new WebException("El estado del Pedido ["+response.ordenSurtidoPedidoDAO.status+"] no es TS o CE.");

        if (response.ordenSurtidoPedidoDAO.status.compareTo("CE")==0) {
            // Tratar de crear detalles
            generaCertificacionSurtidoUno(response);
            
            response.lotes = ds.select(new OrdenSurtidoPedidoLotesDAO(), response.ordenSurtidoPedidoDAO.getWhere());
            response.detalles = ds.select(new OrdenSurtidoPedidoCertificaDAO(), response.ordenSurtidoPedidoDAO.getWhere());

        } else if (response.ordenSurtidoPedidoDAO.status.compareTo("TS")==0) {
            //
            generaCertificacionSurtido(response);

            response.lotes = ds.select(new OrdenSurtidoPedidoLotesDAO(), response.ordenSurtidoPedidoDAO.getWhere());
            response.detalles = ds.select(new OrdenSurtidoPedidoCertificaDAO(), response.ordenSurtidoPedidoDAO.getWhere());

            //
            response.ordenSurtidoPedidoDAO.fechacertificando = new Date();
            response.ordenSurtidoPedidoDAO.status = Constantes.ESTADO_CERTIFICANDO;
            response.ordenSurtidoPedidoDAO.fechastatus = new Date();
            response.ordenSurtidoPedidoDAO.usuario = usuario;

            ds.update(response.ordenSurtidoPedidoDAO, new String[] {"fechacertificando", "status", "fechastatus", "usuario"});

            OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
            Reflector.copyAllFields(response.ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
            ordenSurtidoPedidoBitacoraDAO.id = null;
            ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
            ordenSurtidoPedidoBitacoraDAO.usuario = usuario;
            ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_CERTIFICANDO;

            ds.insert(ordenSurtidoPedidoBitacoraDAO);
        }

        return response;
    }

    private void generaCertificacionSurtido(DetallesCertificacionSurtidoResponse response) throws Exception {
        ArrayList<DatabaseRecordEntity> contenedores = ds.collection(
                "SELECT compania, flsurtido, partida, codigo, contenedor, lote, fecaducidad, SUM(surtidas) AS surtidas "
                        +"FROM OrdenSurtidoPedidoContenedor "
                        +"WHERE "+response.ordenSurtidoPedidoDAO.getWhere()
                        +"GROUP BY compania, flsurtido, partida, codigo, contenedor, lote, fecaducidad");

        int partida = -1;
        int idlote = 0;
        int idcontenedor = 0;
        for(DatabaseRecordEntity record : contenedores) {
            OrdenSurtidoPedidoCertificaDAO ordenSurtidoPedidoCertificaDAO = new OrdenSurtidoPedidoCertificaDAO();
            ordenSurtidoPedidoCertificaDAO.compania = response.ordenSurtidoPedidoDAO.compania;
            ordenSurtidoPedidoCertificaDAO.flsurtido = response.ordenSurtidoPedidoDAO.flsurtido;
            ordenSurtidoPedidoCertificaDAO.partida = record.getInt("partida");
            if (partida!=ordenSurtidoPedidoCertificaDAO.partida) {
                partida = ordenSurtidoPedidoCertificaDAO.partida;
                idlote = 0;
                idcontenedor = 0;
            }
            ordenSurtidoPedidoCertificaDAO.idlote = ++idlote;
            ordenSurtidoPedidoCertificaDAO.idcontenedor = ++idcontenedor;
            ordenSurtidoPedidoCertificaDAO.codigo = record.getString("codigo");
            ordenSurtidoPedidoCertificaDAO.contenedor = record.getString("contenedor");
            ordenSurtidoPedidoCertificaDAO.lote = record.getString("lote");
            ordenSurtidoPedidoCertificaDAO.fecaducidad = record.getDate("fecaducidad");
            ordenSurtidoPedidoCertificaDAO.certificadas = record.getDouble("surtidas");

            OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
            ordenSurtidoPedidoDetalleDAO.compania = ordenSurtidoPedidoCertificaDAO.compania;
            ordenSurtidoPedidoDetalleDAO.flsurtido = ordenSurtidoPedidoCertificaDAO.flsurtido;
            ordenSurtidoPedidoDetalleDAO.partida = ordenSurtidoPedidoCertificaDAO.partida;
            if (!ds.exists(ordenSurtidoPedidoDetalleDAO))
                throw new WebException("No existe este Detalle de Orden de Surtido de Pedido ["+ordenSurtidoPedidoDetalleDAO+"]");

            ordenSurtidoPedidoCertificaDAO.descripcion = ordenSurtidoPedidoDetalleDAO.descripcion;

            ds.insert(ordenSurtidoPedidoCertificaDAO);

            //
            ordenSurtidoPedidoDetalleDAO.certificadas += ordenSurtidoPedidoCertificaDAO.certificadas;

            ds.update(ordenSurtidoPedidoDetalleDAO, new String[] {"certificadas"});

            response.ordenSurtidoPedidoDAO.certificadas += ordenSurtidoPedidoCertificaDAO.certificadas;
        }

        ds.update(response.ordenSurtidoPedidoDAO, new String[] {"certificadas"});

        ArrayList<DatabaseRecordEntity> lotes = ds.collection(
                "SELECT compania, flsurtido, partida, codigo, lote, fecaducidad, SUM(surtidas) AS surtidas "
                        +"FROM OrdenSurtidoPedidoContenedor "
                        +"WHERE "+response.ordenSurtidoPedidoDAO.getWhere()
                        +"GROUP BY compania, flsurtido, partida, codigo, lote, fecaducidad");

        partida = -1;
        idlote = 0;
        for(DatabaseRecordEntity record : lotes) {
            OrdenSurtidoPedidoLotesDAO ordenSurtidoPedidoLotesDAO = new OrdenSurtidoPedidoLotesDAO();
            ordenSurtidoPedidoLotesDAO.compania = response.ordenSurtidoPedidoDAO.compania;
            ordenSurtidoPedidoLotesDAO.flsurtido = response.ordenSurtidoPedidoDAO.flsurtido;
            ordenSurtidoPedidoLotesDAO.partida = record.getInt("partida");
            if (partida!=ordenSurtidoPedidoLotesDAO.partida) {
                partida = ordenSurtidoPedidoLotesDAO.partida;
                idlote = 0;
            }
            ordenSurtidoPedidoLotesDAO.idlote = ++idlote;
            ordenSurtidoPedidoLotesDAO.codigo = record.getString("codigo");

            OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
            ordenSurtidoPedidoDetalleDAO.compania = ordenSurtidoPedidoLotesDAO.compania;
            ordenSurtidoPedidoDetalleDAO.flsurtido = ordenSurtidoPedidoLotesDAO.flsurtido;
            ordenSurtidoPedidoDetalleDAO.partida = ordenSurtidoPedidoLotesDAO.partida;
            if (!ds.exists(ordenSurtidoPedidoDetalleDAO))
                throw new WebException("No existe este Detalle de Orden de Surtido de Pedido ["+ordenSurtidoPedidoDetalleDAO+"]");

            ordenSurtidoPedidoLotesDAO.lote = record.getString("lote");
            ordenSurtidoPedidoLotesDAO.fecaducidad = record.getDate("fecaducidad");
            ordenSurtidoPedidoLotesDAO.surtidas = record.getDouble("surtidas");

            ordenSurtidoPedidoLotesDAO.descripcion = ordenSurtidoPedidoDetalleDAO.descripcion;

            ds.insert(ordenSurtidoPedidoLotesDAO);
        }
    }
    
    private void generaCertificacionSurtidoUno(DetallesCertificacionSurtidoResponse response) throws Exception {
        int partida = -1;
        int idlote = 0;
        
        ArrayList<DatabaseRecordEntity> lotes = ds.collection(
                "SELECT compania, flsurtido, partida, codigo, lote, fecaducidad, SUM(surtidas) AS surtidas "
                        +"FROM OrdenSurtidoPedidoContenedor "
                        +"WHERE "+response.ordenSurtidoPedidoDAO.getWhere()
                        +"GROUP BY compania, flsurtido, partida, codigo, lote, fecaducidad");

        partida = -1;
        idlote = 0;
        for(DatabaseRecordEntity record : lotes) {
            OrdenSurtidoPedidoLotesDAO ordenSurtidoPedidoLotesDAO = new OrdenSurtidoPedidoLotesDAO();
            ordenSurtidoPedidoLotesDAO.compania = response.ordenSurtidoPedidoDAO.compania;
            ordenSurtidoPedidoLotesDAO.flsurtido = response.ordenSurtidoPedidoDAO.flsurtido;
            ordenSurtidoPedidoLotesDAO.partida = record.getInt("partida");
            if (partida!=ordenSurtidoPedidoLotesDAO.partida) {
                partida = ordenSurtidoPedidoLotesDAO.partida;
                idlote = 0;
            }
            ordenSurtidoPedidoLotesDAO.idlote = ++idlote;
            ordenSurtidoPedidoLotesDAO.codigo = record.getString("codigo");

            OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
            ordenSurtidoPedidoDetalleDAO.compania = ordenSurtidoPedidoLotesDAO.compania;
            ordenSurtidoPedidoDetalleDAO.flsurtido = ordenSurtidoPedidoLotesDAO.flsurtido;
            ordenSurtidoPedidoDetalleDAO.partida = ordenSurtidoPedidoLotesDAO.partida;
            if (!ds.exists(ordenSurtidoPedidoDetalleDAO))
                throw new WebException("No existe este Detalle de Orden de Surtido de Pedido ["+ordenSurtidoPedidoDetalleDAO+"]");

            ordenSurtidoPedidoLotesDAO.lote = record.getString("lote");
            ordenSurtidoPedidoLotesDAO.fecaducidad = record.getDate("fecaducidad");
            ordenSurtidoPedidoLotesDAO.surtidas = record.getDouble("surtidas");

            ordenSurtidoPedidoLotesDAO.descripcion = ordenSurtidoPedidoDetalleDAO.descripcion;
            
            if(!ds.exists(ordenSurtidoPedidoLotesDAO))
                ds.insert(ordenSurtidoPedidoLotesDAO);
        }
    }
    
}
