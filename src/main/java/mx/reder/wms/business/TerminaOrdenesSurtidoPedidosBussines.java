package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseRecordEntity;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Fecha;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDetalleDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoContenedorDAO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author joelbecerramiranda
 */
public class TerminaOrdenesSurtidoPedidosBussines {
    static Logger log = Logger.getLogger(TerminaOrdenesSurtidoPedidosBussines.class.getName());

    private DatabaseServices ds;

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }

    public OrdenSurtidoPedidoDAO termina(String compania, String usuario, String folioentrada, JSONArray arraydetalles, JSONArray arraycontenedores, String msg) throws Exception {
        //Si CAMPLIB10 = C, se certifica de forma manual 
        String EsCertifica = "0";
        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = compania;
        ordenSurtidoPedidoDAO.flsurtido = Numero.getIntFromString(folioentrada);
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

        if (ordenSurtidoPedidoDAO.status.compareTo(Constantes.ESTADO_SURTIENDO)!=0)
            throw new WebException("El estado de esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"] ["+ordenSurtidoPedidoDAO.status+"] no es SU.");

        DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
        Connection connectionAspel = databaseDataSourceAspel.getConnection();
        DatabaseServices dsAspel = new DatabaseServices(connectionAspel);
        
        StringBuilder sqlClaves = new StringBuilder();
        sqlClaves.append("SELECT CAMPLIB10 FROM REDER20.dbo.CLIE_CLIB"+compania+"  WHERE CVE_CLIE =  '"+ ordenSurtidoPedidoDAO.cliente +"'");
        ArrayList<DatabaseRecordEntity> arrayClaves = dsAspel.collection(sqlClaves.toString());
        
        for(DatabaseRecordEntity record : arrayClaves) {
            if(record.get("CAMPLIB10")!= null)
                EsCertifica = record.getString("CAMPLIB10");            
        }
        
        connectionAspel.close();
        databaseDataSourceAspel.close();
        
        //
        //
        //
        ordenSurtidoPedidoDAO.surtidas = 0.0d;

        for (int indx=0; indx<arraydetalles.size(); indx++) {
            JSONObject json = (JSONObject)arraydetalles.get(indx);

            OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
            ordenSurtidoPedidoDetalleDAO.compania = compania;
            ordenSurtidoPedidoDetalleDAO.flsurtido = Numero.getIntFromString(json.get("flsurtido").toString());
            ordenSurtidoPedidoDetalleDAO.partida = Numero.getIntFromString(json.get("partida").toString());

            if (!ds.exists(ordenSurtidoPedidoDetalleDAO))
                throw new WebException("No existe este Detalle de Orden de Surtido de Pedido ["+ordenSurtidoPedidoDetalleDAO+"].");

            String codigo = json.get("codigo").toString();
            if (ordenSurtidoPedidoDetalleDAO.codigo.compareTo(codigo)!=0)
                throw new WebException("Este Detalle de Orden de Surtido de Pedido ["+ordenSurtidoPedidoDetalleDAO+"] ["
                        +ordenSurtidoPedidoDetalleDAO.codigo+"] no tiene el mismo codigo ["+codigo+"].");

            ordenSurtidoPedidoDetalleDAO.surtidas = Numero.getIntFromString(json.get("surtidas").toString());
            ordenSurtidoPedidoDAO.surtidas += ordenSurtidoPedidoDetalleDAO.surtidas;

            ds.update(ordenSurtidoPedidoDetalleDAO, new String[] {"surtidas"});
        }

        ordenSurtidoPedidoDAO.surtidor = usuario;

        ds.update(ordenSurtidoPedidoDAO, new String[] {"surtidor", "surtidas"});

        // Se borran todas los contenedores, nos quedamos solamente con las de la captura
        ds.delete(new OrdenSurtidoPedidoContenedorDAO(), ordenSurtidoPedidoDAO.getWhere());
        
        for (int indx=0; indx<arraycontenedores.size(); indx++) {
            JSONObject json = (JSONObject)arraycontenedores.get(indx);

            int flsurtido = Numero.getIntFromString(json.get("flsurtido").toString());
            String contenedor = json.get("contenedor").toString();

            int countContenedor = ds.count(new OrdenSurtidoPedidoContenedorDAO(), "compania = '"
                    +compania+"' AND flsurtido != "+flsurtido+" AND contenedor = '"+contenedor+"'");
            if (countContenedor>0)
                throw new WebException("Este Contenedor ["+contenedor+"] ya esta asignado en otro Pedido.");

            OrdenSurtidoPedidoContenedorDAO ordenSurtidoPedidoContenedorDAO = new OrdenSurtidoPedidoContenedorDAO();
            ordenSurtidoPedidoContenedorDAO.compania = compania;
            ordenSurtidoPedidoContenedorDAO.flsurtido = flsurtido;
            ordenSurtidoPedidoContenedorDAO.partida = Numero.getIntFromString(json.get("partida").toString());
            ordenSurtidoPedidoContenedorDAO.idcontenedor = Numero.getIntFromString(json.get("idcontenedor").toString());
            ordenSurtidoPedidoContenedorDAO.codigo = json.get("codigo").toString();
            ordenSurtidoPedidoContenedorDAO.contenedor = contenedor;
            ordenSurtidoPedidoContenedorDAO.lote = json.get("lote").toString();
            ordenSurtidoPedidoContenedorDAO.fecaducidad = Fecha.getFechaHora(json.get("fecaducidad").toString());
            ordenSurtidoPedidoContenedorDAO.surtidas = Numero.getIntFromString(json.get("surtidas").toString());

            ds.insert(ordenSurtidoPedidoContenedorDAO);
        }
        //
        //
        //
        log.debug("EsCertifica01 :" + EsCertifica);
        if(EsCertifica.equals("C")){
            ordenSurtidoPedidoDAO.status = Constantes.ESTADO_CERTIFICANDO;
        }else{
            ordenSurtidoPedidoDAO.status = Constantes.ESTADO_TERMINASURTIDO;
        }
        ordenSurtidoPedidoDAO.fechastatus = new Date();
        ordenSurtidoPedidoDAO.fechatermino = new Date();

        ds.update(ordenSurtidoPedidoDAO, new String[] {"status", "fechastatus", "fechatermino"});

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoBitacoraDAO.id = null;
        log.debug("EsCertifica02 :" + EsCertifica);
        if(EsCertifica.equals("C")){            
            ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_CERTIFICANDO;
            msg = "Este pedido pasa a [certificacion]";
        }else{
            ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_TERMINASURTIDO;
            msg = "Este pedido pasa a [ruta]";
        }
        ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO.usuario = usuario;

        ds.insert(ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoDAO.setMsg(msg);

        return ordenSurtidoPedidoDAO;
    }
}
