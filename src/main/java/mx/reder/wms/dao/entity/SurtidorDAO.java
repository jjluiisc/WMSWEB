package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SurtidorDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String compania = "";
    public String surtidor = "";
    public String password = "";
    public String equipo = "";
    public String estado = "";
    public String nombre = "";

    public SurtidorDAO() {
    }

    public SurtidorDAO(String compania, String surtidor) {
        this.compania = compania;
        this.surtidor = surtidor;
    }

    @Override
    public String getTable() {
        return "Surtidor";
    }

    @Override
    public String getOrder() {
        return "compania, surtidor";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND surtidor = '"+surtidor+"'";
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"' AND surtidor = '"+surtidor+"'";
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND surtidor > '"+surtidor+"'";
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND surtidor < '"+surtidor+"'";
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"' AND surtidor = '"+surtidor+"'";
    }

    @Override
    public String getOrderFirst() {
        return "compania, surtidor";
    }

    @Override
    public String getOrderLast() {
        return "compania, surtidor DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        compania = values[0];
        surtidor = values[1];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        compania = values[0];
        surtidor = values[1];
    }

    @Override
    public String toString() {
        return compania+";"+surtidor;
    }

    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.surtidor = (String)json.get("surtidor");
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

        this.compania = (String)json.get("compania");
        this.surtidor = (String)json.get("surtidor");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
