package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CompaniaDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String compania = "";
    public String razonsocial = "";
    public String nombre = "";
    public String direccion = "";
    public String rfc = "";
    public String telefono = "";
    public String regimenfiscal = "";

    public CompaniaDAO() {
    }

    public CompaniaDAO(String compania) {
        this.compania = compania;
    }

    @Override
    public String getTable() {
        return "Compania";
    }

    @Override
    public String getOrder() {
        return "compania";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getWhereNext() {
        return "compania > '"+compania+"'";
    }

    @Override
    public String getWherePrev() {
        return "compania < '"+compania+"'";
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getOrderFirst() {
        return "compania";
    }

    @Override
    public String getOrderLast() {
        return "compania DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        compania = values[0];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        compania = values[0];
    }

    @Override
    public String toString() {
        return compania;
    }

    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
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
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
