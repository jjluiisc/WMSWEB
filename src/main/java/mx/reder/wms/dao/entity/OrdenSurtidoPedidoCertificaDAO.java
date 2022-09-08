package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Fecha;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.Date;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author joelbecerramiranda
 */
public class OrdenSurtidoPedidoCertificaDAO implements DatabaseRecord, DatabaseRecordABC {
    static Logger log = Logger.getLogger(OrdenSurtidoPedidoCertificaDAO.class.getName());

    public String compania = "";
    public int flsurtido = 0;
    public int partida = 0;
    public int idlote = 0;
    public int idcontenedor = 0;
    public String codigo = "";
    public String descripcion = "";
    public String contenedor = "";
    public String lote = "";
    public Date fecaducidad = new Date(0);
    public double certificadas = 0.0;

    public OrdenSurtidoPedidoCertificaDAO() {
    }

    public OrdenSurtidoPedidoCertificaDAO(String compania, int flsurtido, int partida, int idlote, int idcontenedor) {
        this.compania = compania;
        this.flsurtido = flsurtido;
        this.partida = partida;
        this.idlote = idlote;
        this.idcontenedor = idcontenedor;
    }

    @Override
    public String getTable() {
        return "OrdenSurtidoPedidoCertifica";
    }

    @Override
    public String getOrder() {
        return "compania, flsurtido, partida, idlote, idcontenedor";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote+" AND idcontenedor = "+idcontenedor;
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote;
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote+" AND idcontenedor > "+idcontenedor;
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote+" AND idcontenedor < "+idcontenedor;
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idlote = "+idlote;
    }

    @Override
    public String getOrderFirst() {
        return "compania, flsurtido, partida, idlote, idcontenedor";
    }

    @Override
    public String getOrderLast() {
        return "compania, flsurtido, partida, idlote, idcontenedor DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        this.compania = values[0];
        this.flsurtido = Numero.getIntFromString(values[1]);
        this.partida = Numero.getIntFromString(values[2]);
        this.idlote = Numero.getIntFromString(values[3]);
        this.idcontenedor = Numero.getIntFromString(values[4]);
    }

    @Override
    public void setValues(String[] values) throws Exception {
        this.compania = values[0];
        this.flsurtido = Numero.getIntFromString(values[1]);
        this.partida = Numero.getIntFromString(values[2]);
        this.idlote = Numero.getIntFromString(values[3]);
        this.idcontenedor = Numero.getIntFromString(values[4]);
    }

    @Override
    public String toString() {
        return compania+";"+flsurtido+";"+partida+";"+idlote+";"+idcontenedor;
    }

    public void borrar(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flsurtido = Numero.getIntFromString(json.get("flsurtido").toString());
        this.partida = Numero.getIntFromString(json.get("partida").toString());
        this.idlote = Numero.getIntFromString(json.get("idlote").toString());
        this.idcontenedor = Numero.getIntFromString(json.get("idcontenedor").toString());

        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = compania;
        ordenSurtidoPedidoDAO.flsurtido = this.flsurtido;
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

        OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
        ordenSurtidoPedidoDetalleDAO.compania = compania;
        ordenSurtidoPedidoDetalleDAO.flsurtido = this.flsurtido;
        ordenSurtidoPedidoDetalleDAO.partida = this.partida;
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe este Detalle de Orden de Surtido de Pedido ["+ordenSurtidoPedidoDetalleDAO+"]");

        ds.delete(this);

        // Piezas Certificadas
        Double piezasCertificadas = (Double)ds.aggregate(this, "SUM", "certificadas", "compania = '"+this.compania+"' AND flsurtido = "+this.flsurtido);

        ordenSurtidoPedidoDAO.certificadas = piezasCertificadas==null ? 0.0d : piezasCertificadas;
        ds.update(ordenSurtidoPedidoDAO, new String[] {"certificadas"});

        piezasCertificadas = (Double)ds.aggregate(this, "SUM", "certificadas", "compania = '"+this.compania+"' AND flsurtido = "+this.flsurtido+" AND partida = "+this.partida);

        ordenSurtidoPedidoDetalleDAO.certificadas = piezasCertificadas==null ? 0.0d : piezasCertificadas;
        ds.update(ordenSurtidoPedidoDetalleDAO, new String[] {"certificadas"});
    }

    public void certificadas(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flsurtido = Numero.getIntFromString(json.get("flsurtido").toString());
        this.partida = Numero.getIntFromString(json.get("partida").toString());
        this.idlote = Numero.getIntFromString(json.get("idlote").toString());
        this.codigo = (String)json.get("codigo");
        this.descripcion = (String)json.get("descripcion");
        this.contenedor = (String)json.get("contenedor");
        this.lote = (String)json.get("lote");
        this.fecaducidad = Fecha.getFecha((String)json.get("fecaducidad"));
        this.certificadas = Numero.getDoubleFromString(json.get("certificadas").toString());

        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = compania;
        ordenSurtidoPedidoDAO.flsurtido = this.flsurtido;
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"].");

        OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
        ordenSurtidoPedidoDetalleDAO.compania = compania;
        ordenSurtidoPedidoDetalleDAO.flsurtido = this.flsurtido;
        ordenSurtidoPedidoDetalleDAO.partida = this.partida;
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe este Detalle de Orden de Surtido de Pedido ["+ordenSurtidoPedidoDetalleDAO+"]");

        
        OrdenSurtidoPedidoLotesDAO ordenSurtidoPedidoLotesDAO = (OrdenSurtidoPedidoLotesDAO)ds.first(new OrdenSurtidoPedidoLotesDAO(),
                "compania = '"+this.compania+"' AND flsurtido = "+this.flsurtido+" AND partida = "+this.partida+" AND idlote = "+this.idlote
                        +" AND codigo = '"+this.codigo+"' AND lote = '"+this.lote+"'");
        if (ordenSurtidoPedidoLotesDAO==null)
            throw new WebException("No existe la Orden Surtido Pedido Lote de este Producto ["+this.codigo+" "+this.descripcion
                    +"] en esta Orden de Surtido de Pedido ["+this.compania+";"+this.flsurtido+";"+this.partida+";"+this.lote+"].");

        // Busco las piezas ya certificadas
        Double yaCertificadas = (Double)ds.aggregate(new OrdenSurtidoPedidoCertificaDAO(), "SUM", "certificadas",
                "compania = '"+this.compania+"' AND flsurtido = "+this.flsurtido+" AND partida = "+this.partida+" AND idlote = "+this.idlote
                        +" AND codigo = '"+this.codigo+"' AND lote = '"+this.lote+"'");
        if (yaCertificadas==null)
            yaCertificadas = 0.0d;
        log.debug("yaCertificadas: "+yaCertificadas);

        double certificadasFinal = yaCertificadas + this.certificadas;
        certificadasFinal = Numero.redondea(certificadasFinal);
        log.debug("certificadasFinal: "+certificadasFinal);

        if (certificadasFinal > ordenSurtidoPedidoLotesDAO.surtidas)
            throw new WebException("Cantidad Certificada final ["+certificadasFinal+"] mayor a la Cantidad Surtidas ["+ordenSurtidoPedidoLotesDAO.surtidas+"].");

        // Ahora busco la Certificacion existente con el mismo Contenedor - Lote
        OrdenSurtidoPedidoCertificaDAO ordenSurtidoPedidoCertificaDAO = (OrdenSurtidoPedidoCertificaDAO)ds.first(new OrdenSurtidoPedidoCertificaDAO(),
                "compania = '"+this.compania+"' AND flsurtido = "+this.flsurtido+" AND partida = "+this.partida+" AND idlote = "+this.idlote
                        +" AND codigo = '"+this.codigo+"' AND contenedor = '"+this.contenedor+"' AND lote = '"+this.lote+"'");
        log.debug(Reflector.toStringAllFields(ordenSurtidoPedidoCertificaDAO));

        if (ordenSurtidoPedidoCertificaDAO==null) {
            this.idcontenedor = 1;

            // Si ya habia piezas certificadas, son de otro contenedor - lote, asi que incremento el idcontenedor
            if (yaCertificadas > 0.0)
                this.idcontenedor ++;

            ds.insert(this);

        } else {
            // Existe este contenedor - lote, solo aumento las certificadas
            ordenSurtidoPedidoCertificaDAO.certificadas = Numero.redondea(ordenSurtidoPedidoCertificaDAO.certificadas + this.certificadas);

            ds.update(ordenSurtidoPedidoCertificaDAO, new String[]{"certificadas"});

            // Recupera los valores para el response
            this.idlote = ordenSurtidoPedidoCertificaDAO.idlote;
            this.idcontenedor = ordenSurtidoPedidoCertificaDAO.idcontenedor;
            this.fecaducidad = ordenSurtidoPedidoCertificaDAO.fecaducidad;
            this.certificadas = ordenSurtidoPedidoCertificaDAO.certificadas;
        }

        // Piezas Certificadas
        Double piezasCertificadas = (Double)ds.aggregate(this, "SUM", "certificadas", "compania = '"+this.compania+"' AND flsurtido = "+this.flsurtido);

        ordenSurtidoPedidoDAO.certificadas = piezasCertificadas==null ? 0.0d : piezasCertificadas;
        ds.update(ordenSurtidoPedidoDAO, new String[] {"certificadas"});

        piezasCertificadas = (Double)ds.aggregate(this, "SUM", "certificadas", "compania = '"+this.compania+"' AND flsurtido = "+this.flsurtido+" AND partida = "+this.partida);

        ordenSurtidoPedidoDetalleDAO.certificadas = piezasCertificadas==null ? 0.0d : piezasCertificadas;
        ds.update(ordenSurtidoPedidoDetalleDAO, new String[] {"certificadas"});
    }
}
