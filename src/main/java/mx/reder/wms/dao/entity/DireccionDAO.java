package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import mx.reder.wms.cfdi.entity.DireccionCFD;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class DireccionDAO implements DatabaseRecord, DatabaseRecordABC, DireccionCFD, java.io.Serializable {
    public String direccion = "";
    public String calle = "";
    public String noexterior = "";
    public String nointerior = "";
    public String colonia = "";
    public String poblacion = "";
    public String entidadfederativa = "";
    public String pais = "";
    public String codigopostal = "";

    public DireccionDAO() {
    }

    public DireccionDAO(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String getTable() {
        return "Direccion";
    }

    @Override
    public String getOrder() {
        return "direccion";
    }

    @Override
    public String getWhere() {
        return "direccion = '"+direccion+"'";
    }

    @Override
    public String getWhereFirst() {
        return "1 = 1";
    }

    @Override
    public String getWhereNext() {
        return "direccion > '"+direccion+"'";
    }

    @Override
    public String getWherePrev() {
        return "direccion < '"+direccion+"'";
    }

    @Override
    public String getWhereLast() {
        return "1 = 1";
    }

    @Override
    public String getOrderFirst() {
        return "direccion";
    }

    @Override
    public String getOrderLast() {
        return "direccion DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        direccion = values[0];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        direccion = values[0];
    }

    @Override
    public String toString() {
        return direccion;
    }


    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.direccion = (String)json.get("direccion");
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

        this.direccion = (String)json.get("direccion");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }

    @Override
    public String getCalle() {
        return calle;
    }

    @Override
    public String getNoExterior() {
        return noexterior;
    }

    @Override
    public String getNoInterior() {
        return nointerior;
    }

    @Override
    public String getColonia() {
        return colonia;
    }

    @Override
    public String getMunicipio() {
        return poblacion;
    }

    @Override
    public String getEstado() {
        return entidadfederativa;
    }

    @Override
    public String getPais() {
        return pais;
    }

    @Override
    public String getCodigoPostal() {
        return codigopostal;
    }
}
