package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.util.Date;

/**
 *
 * @author joelbecerramiranda
 */
public class RutaDAO implements DatabaseRecord, java.io.Serializable {
    public Integer id = 0;
    public String compania = "";
    public String ruta = "";
    public String status = "";
    public Date fechastatus = new Date(0);
    public String usuario = "";
    public Date fechacreacion = null;
    public Date fechafacturada = null;
    public Date fechapaquetedocumental = null;
    public Date fechacierre = null;

    public RutaDAO() {
    }

    public RutaDAO(int id) {
        this.id = id;
    }

    @Override
    public String getTable() {
        return "Ruta";
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
        return id+";"+compania+";"+ruta;
    }
}
