package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.Date;
import mx.reder.wms.business.InventarioCapturaBussines;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class InventarioCapturaDAO implements DatabaseRecord, DatabaseRecordABC {
    public String compania = "";
    public int flcapturainventario = 0;
    public int flinventario = 0;
    public Date fecreacion = null;
    public String status = "";
    public Date femodificacion = null;
    public Date fetermino = null;
    public String usuario = "";
    public String terminal = "";
    public String fase = "";

    public InventarioCapturaDAO() {
    }

    public InventarioCapturaDAO(String compania, int flcapturainventario) {
        this.compania = compania;
        this.flcapturainventario = flcapturainventario;
    }

    @Override
    public String getTable() {
        return "InventarioCaptura";
    }

    @Override
    public String getOrder() {
        return "compania, flcapturainventario";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flcapturainventario = "+flcapturainventario;
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND flcapturainventario > "+flcapturainventario;
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND flcapturainventario < "+flcapturainventario;
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getOrderFirst() {
        return "compania, flcapturainventario";
    }

    @Override
    public String getOrderLast() {
        return "compania, flcapturainventario DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        compania = values[0];
        flcapturainventario = Numero.getIntFromString(values[1]);
    }

    @Override
    public void setValues(String[] values) throws Exception {
        compania = values[0];
        flcapturainventario = Numero.getIntFromString(values[1]);
    }

    @Override
    public String toString() {
        return compania+";"+flcapturainventario;
    }

    public void busca(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flinventario = Integer.parseInt(json.get("flinventario").toString());

        InventarioCapturaBussines inventarioCapturaBussines = new InventarioCapturaBussines();

        InventarioCapturaDAO inventarioCapturaDAO = inventarioCapturaBussines.buscaInventarioCaptura(ds, compania, flinventario);
        if (inventarioCapturaDAO==null)
            throw new WebException("No existe una Captura de Inventario [PE] en esta compania.");

        Reflector.copyAllFields(inventarioCapturaDAO, this);
    }

    public void crea(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flinventario = Integer.parseInt(json.get("flinventario").toString());
        this.usuario = (String)json.get("usuario");
        this.terminal = (String)json.get("terminal");

        InventarioCapturaBussines inventarioCapturaBussines = new InventarioCapturaBussines();
        InventarioCapturaDAO inventarioCapturaDAO = inventarioCapturaBussines.creaInventarioCaptura(ds, compania, flinventario, usuario, terminal);

        Reflector.copyAllFields(inventarioCapturaDAO, this);
    }

    public void borra(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flcapturainventario = Integer.parseInt(json.get("flcapturainventario").toString());
        boolean existe = ds.exists(this);

        if (!existe)
            throw new WebException("Esta Captura de Inventario ["+this+"] no existe.");

        this.usuario = (String)json.get("usuario");

        InventarioCapturaBussines inventarioCapturaBussines = new InventarioCapturaBussines();
        inventarioCapturaBussines.borraInventarioCaptura(ds, this);
    }

    public void termina(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flcapturainventario = Integer.parseInt(json.get("flcapturainventario").toString());
        boolean existe = ds.exists(this);

        if (!existe)
            throw new WebException("Esta Captura de Inventario ["+this+"] no existe.");

        this.usuario = (String)json.get("usuario");

        InventarioCapturaBussines inventarioCapturaBussines = new InventarioCapturaBussines();
        inventarioCapturaBussines.terminaInventarioCaptura(ds, this);
    }
}
