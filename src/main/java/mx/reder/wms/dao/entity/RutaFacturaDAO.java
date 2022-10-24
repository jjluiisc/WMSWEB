package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.util.Date;

/**
 *
 * @author joelbecerramiranda
 */
public class RutaFacturaDAO implements DatabaseRecord, java.io.Serializable {
    public String compania = "";
    public int flsurtido = 0;
    public int idruta = 0;
    public int parada = 0;
    public String status = "";
    public Date fechastatus = new Date(0);
    public String usuario = "";
    public String serie = "";
    public String factura = null;
    public Date fechafacturacion = null;
    public String mensaje = null;

    public RutaFacturaDAO() {
    }

    public RutaFacturaDAO(String compania, int flsurtido) {
        this.compania = compania;
        this.flsurtido = flsurtido;
    }

    @Override
    public String getTable() {
        return "RutaFactura";
    }

    @Override
    public String getOrder() {
        return "compania, flsurtido";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido;
    }

    @Override
    public String toString() {
        return compania+";"+flsurtido;
    }
}
