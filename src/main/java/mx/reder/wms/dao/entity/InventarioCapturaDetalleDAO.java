package mx.reder.wms.dao.entity;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import java.math.BigDecimal;
import java.util.Date;
import mx.reder.wms.business.InventarioCapturaBussines;
import mx.reder.wms.util.Constantes;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class InventarioCapturaDetalleDAO implements DatabaseRecord, DatabaseRecordABC {
    public Integer fldcapturainventario = 0;
    public String compania = "";
    public int flcapturainventario = 0;
    public int flinventario = 0;
    public String status = "";
    public String codigo = "";
    public String descripcion = "";
    public String ubicacion = "";
    public String lote = "";
    public Date fecaducidad = new Date(0);
    public BigDecimal cantidad = BigDecimal.ZERO;

    public InventarioCapturaDetalleDAO() {
    }

    public InventarioCapturaDetalleDAO(int fldcapturainventario) {
        this.fldcapturainventario = fldcapturainventario;
    }

    @Override
    public String getTable() {
        return "InventarioCapturaDetalle";
    }

    @Override
    public String getOrder() {
        return "fldcapturainventario";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flcapturainventario = "+flcapturainventario+" AND fldcapturainventario = "+fldcapturainventario;
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"' AND flcapturainventario = "+flcapturainventario+"";
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND flcapturainventario = "+flcapturainventario+" AND fldcapturainventario > "+fldcapturainventario;
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND flcapturainventario = "+flcapturainventario+" AND fldcapturainventario < "+fldcapturainventario;
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"' AND flcapturainventario = "+flcapturainventario+"";
    }

    @Override
    public String getOrderFirst() {
        return "fldcapturainventario";
    }

    @Override
    public String getOrderLast() {
        return "fldcapturainventario DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        fldcapturainventario = Numero.getIntFromString(values[0]);
    }

    @Override
    public void setValues(String[] values) throws Exception {
        fldcapturainventario = Numero.getIntFromString(values[0]);
    }

    @Override
    public String toString() {
        return fldcapturainventario+";"+compania+";"+flcapturainventario;
    }

    public void agrega(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        JSON.fromJson(json, this);
        this.status = Constantes.ESTADO_PENDIENTE;

        InventarioCapturaBussines inventarioCapturaBussines = new InventarioCapturaBussines();
        InventarioCapturaDetalleDAO inventarioCapturaDetalleDAO = inventarioCapturaBussines.agregaInventarioCapturaDetalle(ds, this);

        Reflector.copyAllFields(inventarioCapturaDetalleDAO, this);
    }

    public void elimina(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        JSON.fromJson(json, this);

        InventarioCapturaBussines inventarioCapturaBussines = new InventarioCapturaBussines();
        inventarioCapturaBussines.borraInventarioCapturaDetalle(ds, this);
    }
}
