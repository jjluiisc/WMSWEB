/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author Luis
 */
public class MotivoCancelacionDAO implements DatabaseRecord, DatabaseRecordABC, java.io.Serializable {
    public String motivocancelacion = "";
    public String descripcion = "";

    public MotivoCancelacionDAO() {
    }

    public MotivoCancelacionDAO(String motivocancelacion) {
        this.motivocancelacion = motivocancelacion;
    }

    @Override
    public String getTable() {
        return "MotivoCancelacion";
    }

    @Override
    public String getOrder() {
        return "motivocancelacion";
    }

    @Override
    public String getWhere() {
        return "motivocancelacion = '"+motivocancelacion+"'";
    }

    @Override
    public String getWhereFirst() {
        return "1 = 1";
    }

    @Override
    public String getWhereNext() {
        return "motivocancelacion > '"+motivocancelacion+"'";
    }

    @Override
    public String getWherePrev() {
        return "motivocancelacion < '"+motivocancelacion+"'";
    }

    @Override
    public String getWhereLast() {
        return "1 = 1";
    }

    @Override
    public String getOrderFirst() {
        return "motivocancelacion";
    }

    @Override
    public String getOrderLast() {
        return "motivocancelacion DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        motivocancelacion = values[0];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        motivocancelacion = values[0];
    }

    @Override
    public String toString() {
        return motivocancelacion+";"+descripcion;
    }


    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.motivocancelacion = (String)json.get("motivocancelacion");
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

        this.motivocancelacion = (String)json.get("motivocancelacion");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}

