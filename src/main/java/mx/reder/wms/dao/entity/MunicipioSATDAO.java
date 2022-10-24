package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MunicipioSATDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String municipio = "";
    public String estado = "";
    public String descripcion = "";

    public MunicipioSATDAO() {
    }

    public MunicipioSATDAO(String municipio, String estado) {
        this.municipio = municipio;
        this.estado = estado;
    }

    @Override
    public String getTable() {
        return "MunicipioSAT";
    }

    @Override
    public String getOrder() {
        return "municipio, estado";
    }

    @Override
    public String getWhere() {
        return "municipio = '"+municipio+"' AND estado = '"+estado+"'";
    }

    @Override
    public String getWhereFirst() {
        return "municipio = '"+estado+"'";
    }

    @Override
    public String getWhereNext() {
        return "municipio = '"+estado+"' AND estado > '"+estado+"'";
    }

    @Override
    public String getWherePrev() {
        return "municipio = '"+estado+"' AND estado < '"+estado+"'";
    }

    @Override
    public String getWhereLast() {
        return "municipio = '"+estado+"'";
    }

    @Override
    public String getOrderFirst() {
        return "municipio, estado";
    }

    @Override
    public String getOrderLast() {
        return "municipio, estado DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        municipio = values[0];
        estado = values[1];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        municipio = values[0];
        estado = values[1];
    }

    @Override
    public String toString() {
        return municipio;
    }


    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.municipio = (String)json.get("municipio");
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

        this.municipio = (String)json.get("municipio");
        this.estado = (String)json.get("estado");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
