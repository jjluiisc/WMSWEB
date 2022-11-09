package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ConfigAutotransporteSATDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String clave = "";
    public String descripcion = "";
    public String ejes = "";
    public String llantas = "";
    public String remolque = "";

    public ConfigAutotransporteSATDAO() {
    }

    public ConfigAutotransporteSATDAO(String clave) {
        this.clave = clave;
    }

    @Override
    public String getTable() {
        return "ConfigAutotransporteSAT";
    }

    @Override
    public String getOrder() {
        return "clave";
    }

    @Override
    public String getWhere() {
        return "clave = '"+clave+"'";
    }

    @Override
    public String getWhereFirst() {
        return "1 = 1";
    }

    @Override
    public String getWhereNext() {
        return "clave > '"+clave+"'";
    }

    @Override
    public String getWherePrev() {
        return "clave < '"+clave+"'";
    }

    @Override
    public String getWhereLast() {
        return "1 = 1";
    }

    @Override
    public String getOrderFirst() {
        return "clave";
    }

    @Override
    public String getOrderLast() {
        return "clave DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        clave = values[0];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        clave = values[0];
    }

    @Override
    public String toString() {
        return clave;
    }

    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.clave = (String)json.get("clave");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.update(this);
        } else {
            ds.insert(this);
        }
    }

    public void delete(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.clave = (String)json.get("clave");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
