package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ColoniaSATDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String colonia = "";
    public String codigopostal = "";
    public String descripcion = "";

    public ColoniaSATDAO() {
    }

    public ColoniaSATDAO(String colonia, String codigopostal) {
        this.colonia = colonia;
        this.codigopostal = codigopostal;
    }

    @Override
    public String getTable() {
        return "ColoniaSAT";
    }

    @Override
    public String getOrder() {
        return "colonia, codigopostal";
    }

    @Override
    public String getWhere() {
        return "colonia = '"+colonia+"' AND codigopostal = '"+codigopostal+"'";
    }

    @Override
    public String getWhereFirst() {
        return "colonia = '"+codigopostal+"'";
    }

    @Override
    public String getWhereNext() {
        return "colonia = '"+codigopostal+"' AND codigopostal > '"+codigopostal+"'";
    }

    @Override
    public String getWherePrev() {
        return "colonia = '"+codigopostal+"' AND codigopostal < '"+codigopostal+"'";
    }

    @Override
    public String getWhereLast() {
        return "colonia = '"+codigopostal+"'";
    }

    @Override
    public String getOrderFirst() {
        return "colonia, codigopostal";
    }

    @Override
    public String getOrderLast() {
        return "colonia, codigopostal DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        colonia = values[0];
        codigopostal = values[1];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        colonia = values[0];
        codigopostal = values[1];
    }

    @Override
    public String toString() {
        return colonia+";"+codigopostal;
    }

    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.colonia = (String)json.get("colonia");
        this.codigopostal = (String)json.get("codigopostal");
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

        this.colonia = (String)json.get("colonia");
        this.codigopostal = (String)json.get("codigopostal");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
