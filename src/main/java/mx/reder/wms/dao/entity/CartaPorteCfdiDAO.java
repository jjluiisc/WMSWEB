package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.util.Date;

/**
 *
 * @author joelbecerramiranda
 */
public class CartaPorteCfdiDAO implements DatabaseRecord, java.io.Serializable {
    public Integer id = 0;
    public String compania = "";
    public String status = "";
    public Date fechastatus = new Date(0);
    public String nocertificado = "";
    public String uuid = "";
    public Date fechatimbre = new Date(0);
    public String rfcemisor = "";
    public String rfcreceptor = "";
    public double total = 0.0;
    public String xml = "";
    public String cadenaoriginal = "";
    public String qr = "";
    public byte[] pdf = null;
    public Date fechacancelacion = null;
    public String acusecancelacion = null;

    public CartaPorteCfdiDAO() {
    }

    public CartaPorteCfdiDAO(int id) {
        this.id = id;
    }

    @Override
    public String getTable() {
        return "CartaPorteCfdi";
    }

    @Override
    public String getOrder() {
        return "id";
    }

    @Override
    public String getWhere() {
        return "id = "+id;
    }

    @Override
    public String toString() {
        return id+";"+compania;
    }
}
