package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class LocalidadSATDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String localidad = "";
    public String estado = "";
    public String descripcion = "";

    public LocalidadSATDAO() {
    }

    public LocalidadSATDAO(String localidad, String estado) {
        this.localidad = localidad;
        this.estado = estado;
    }

    @Override
    public String getTable() {
        return "LocalidadSAT";
    }

    @Override
    public String getOrder() {
        return "localidad, estado";
    }

    @Override
    public String getWhere() {
        return "localidad = '"+localidad+"' AND estado = '"+estado+"'";
    }

    @Override
    public String getWhereFirst() {
        return "localidad = '"+estado+"'";
    }

    @Override
    public String getWhereNext() {
        return "localidad = '"+estado+"' AND estado > '"+estado+"'";
    }

    @Override
    public String getWherePrev() {
        return "localidad = '"+estado+"' AND estado < '"+estado+"'";
    }

    @Override
    public String getWhereLast() {
        return "localidad = '"+estado+"'";
    }

    @Override
    public String getOrderFirst() {
        return "localidad, estado";
    }

    @Override
    public String getOrderLast() {
        return "localidad, estado DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        localidad = values[0];
        estado = values[1];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        localidad = values[0];
        estado = values[1];
    }

    @Override
    public String toString() {
        return localidad;
    }


    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.localidad = (String)json.get("localidad");
        this.estado = (String)json.get("estado");
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

        this.localidad = (String)json.get("localidad");
        this.estado = (String)json.get("estado");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
