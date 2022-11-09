package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CodigoPostalSATDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String codigopostal = "";
    public String estado = "";
    public String municipio = "";
    public String localidad = "";

    public CodigoPostalSATDAO() {
    }

    public CodigoPostalSATDAO(String codigopostal) {
        this.codigopostal = codigopostal;
    }

    @Override
    public String getTable() {
        return "CodigoPostalSAT";
    }

    @Override
    public String getOrder() {
        return "codigopostal";
    }

    @Override
    public String getWhere() {
        return "codigopostal = '"+codigopostal+"'";
    }

    @Override
    public String getWhereFirst() {
        return "1 = 1";
    }

    @Override
    public String getWhereNext() {
        return "codigopostal > '"+codigopostal+"'";
    }

    @Override
    public String getWherePrev() {
        return "codigopostal < '"+codigopostal+"'";
    }

    @Override
    public String getWhereLast() {
        return "1 = 1";
    }

    @Override
    public String getOrderFirst() {
        return "codigopostal";
    }

    @Override
    public String getOrderLast() {
        return "codigopostal DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        codigopostal = values[0];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        codigopostal = values[0];
    }

    @Override
    public String toString() {
        return codigopostal;
    }

    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

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

        this.codigopostal = (String)json.get("codigopostal");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
