package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class EntidadFederativaDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String entidadfederativa = "";
    public String pais = "";
    public String nombre = "";
    public String estado = "";

    public EntidadFederativaDAO() {
    }

    public EntidadFederativaDAO(String entidadfederativa) {
        this.entidadfederativa = entidadfederativa;
    }

    @Override
    public String getTable() {
        return "EntidadFederativa";
    }

    @Override
    public String getOrder() {
        return "entidadfederativa";
    }

    @Override
    public String getWhere() {
        return "entidadfederativa = '"+entidadfederativa+"'";
    }

    @Override
    public String getWhereFirst() {
        return "1 = 1";
    }

    @Override
    public String getWhereNext() {
        return "entidadfederativa > '"+entidadfederativa+"'";
    }

    @Override
    public String getWherePrev() {
        return "entidadfederativa < '"+entidadfederativa+"'";
    }

    @Override
    public String getWhereLast() {
        return "1 = 1";
    }

    @Override
    public String getOrderFirst() {
        return "entidadfederativa";
    }

    @Override
    public String getOrderLast() {
        return "entidadfederativa DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        entidadfederativa = values[0];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        entidadfederativa = values[0];
    }

    @Override
    public String toString() {
        return entidadfederativa;
    }

    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.entidadfederativa = (String)json.get("entidadfederativa");
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

        this.entidadfederativa = (String)json.get("entidadfederativa");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
