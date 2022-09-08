package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.ServletUtilities;
import java.math.BigDecimal;
import java.util.Date;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ProductoDAO implements DatabaseRecord, DatabaseRecordABC {
    public String compania = "";
    public String codigo = "";
    public String descripcion = "";
    public String unidadmedida = "";
    public Integer linea = 0;
    public Integer categoria = 0;
    public String marca = "";
    public Integer capa = 0;
    public BigDecimal existencia = BigDecimal.ZERO;
    public BigDecimal costo = BigDecimal.ZERO;
    public Date modificacion = new Date(0);

    public ProductoDAO() {
    }

    public ProductoDAO(String compania, String codigo) {
        this.compania = compania;
        this.codigo = codigo;
    }

    @Override
    public String getTable() {
        return "Producto";
    }

    @Override
    public String getOrder() {
        return "compania, codigo";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND codigo = '"+codigo+"'";
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND codigo > '"+codigo+"'";
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND codigo < '"+codigo+"'";
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getOrderFirst() {
        return "compania, codigo";
    }

    @Override
    public String getOrderLast() {
        return "compania, codigo DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        compania = values[0];
        codigo = values[1];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        compania = values[0];
        codigo = values[1];
    }

    @Override
    public String toString() {
        return compania+";"+codigo+";"+descripcion;
    }

    public void save(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.codigo = (String)json.get("codigo");
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
        this.codigo = (String)json.get("codigo");
        boolean existe = ds.exists(this);

        ServletUtilities.fromJSON(this, json);

        if (existe) {
            ds.delete(this);
        }
    }
}
