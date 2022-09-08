package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Fecha;
import com.atcloud.util.Numero;
import com.atcloud.web.WebException;
import java.util.Date;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author joelbecerramiranda
 */
public class OrdenSurtidoPedidoLotesDAO implements DatabaseRecord, DatabaseRecordABC {
    static Logger log = Logger.getLogger(OrdenSurtidoPedidoCertificaDAO.class.getName());
        
    public String compania = "";
    public int flsurtido = 0;
    public int partida = 0;
    public int idlote = 0;
    public String codigo = "";
    public String descripcion = "";
    public String lote = "";
    public Date fecaducidad = new Date(0);
    public double surtidas = 0.0;

    public OrdenSurtidoPedidoLotesDAO() {
    }

    public OrdenSurtidoPedidoLotesDAO(String compania, int flsurtido, int partida, int idlote) {
        this.compania = compania;
        this.flsurtido = flsurtido;
        this.partida = partida;
        this.idlote = idlote;
    }

    @Override
    public String getTable() {
        return "OrdenSurtidoPedidoLote";
    }

    @Override
    public String getOrder() {
        return "compania, flsurtido, partida, idlote";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote;
    }
    
    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote;
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote;
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote;
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote;
    }

    @Override
    public String getOrderFirst() {
        return "compania, flsurtido, partida, idlote ";
    }

    @Override
    public String getOrderLast() {
        return "compania, flsurtido, partida, idlote DESC";
    }
    
    @Override
    public void setKey(String[] values) throws Exception {
        this.compania = values[0];
        this.flsurtido = Numero.getIntFromString(values[1]);
        this.partida = Numero.getIntFromString(values[2]);
        this.idlote = Numero.getIntFromString(values[3]);
    }
    
    @Override
    public void setValues(String[] values) throws Exception {
        this.compania = values[0];
        this.flsurtido = Numero.getIntFromString(values[1]);
        this.partida = Numero.getIntFromString(values[2]);
        this.idlote = Numero.getIntFromString(values[3]);
    }

    @Override
    public String toString() {
        return compania+";"+flsurtido+";"+partida+";"+idlote;
    }
    
    public void cambioLote(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flsurtido = Numero.getIntFromString(json.get("flsurtido").toString());
        this.partida = Numero.getIntFromString(json.get("partida").toString());
        this.idlote = Numero.getIntFromString(json.get("idlote").toString());
        this.codigo = (String)json.get("codigo");
        this.descripcion = (String)json.get("descripcion");
        this.lote = (String)json.get("lote");
        this.fecaducidad = Fecha.getFecha((String)json.get("fecaducidad"));
        

        OrdenSurtidoPedidoLotesDAO ordenSurtidoPedidoLotesDAO = new OrdenSurtidoPedidoLotesDAO();
        ordenSurtidoPedidoLotesDAO.compania = compania;
        ordenSurtidoPedidoLotesDAO.flsurtido = this.flsurtido;
        ordenSurtidoPedidoLotesDAO.partida = this.partida;
        ordenSurtidoPedidoLotesDAO.idlote = this.idlote;
        if (!ds.exists(ordenSurtidoPedidoLotesDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido Lote ["+ordenSurtidoPedidoLotesDAO+"].");


        ordenSurtidoPedidoLotesDAO.lote = (String)json.get("lote");
        ordenSurtidoPedidoLotesDAO.fecaducidad = Fecha.getFecha((String)json.get("fecaducidad"));
        ds.update(ordenSurtidoPedidoLotesDAO, new String[] {"lote","fecaducidad"});
    }
}
