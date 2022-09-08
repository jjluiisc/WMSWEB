package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ClaveAlternaDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String compania = "";
    public String clave = "";
    public String codigo = "";

    public ClaveAlternaDAO() {
    }

    public ClaveAlternaDAO(String compania, String clave) {
        this.compania = compania;
        this.clave = clave;
    }

    @Override
    public String getTable() {
        return "ClaveAlterna";
    }

    @Override
    public String getOrder() {
        return "compania, clave";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND clave = '"+clave+"'";
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"' AND clave = '"+clave+"'";
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND clave > '"+clave+"'";
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND clave < '"+clave+"'";
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"' AND clave = '"+clave+"'";
    }

    @Override
    public String getOrderFirst() {
        return "compania, clave";
    }

    @Override
    public String getOrderLast() {
        return "compania, clave DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        compania = values[0];
        clave = values[1];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        compania = values[0];
        clave = values[1];
    }

    @Override
    public String toString() {
        return compania+";"+clave;
    }

    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
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

        this.compania = (String)json.get("compania");
        this.clave = (String)json.get("clave");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
