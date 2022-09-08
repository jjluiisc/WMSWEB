package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.Date;
import mx.reder.wms.business.InventarioBussines;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class InventarioDAO implements DatabaseRecord, DatabaseRecordABC {
    public String compania = "";
    public int flinventario = 0;
    public String descripcion = "";
    public Date feinicio = null;
    public Date fetermino = null;
    public String status = "";
    public String fase = "";
    public Date fefase = null;
    public Date feprimerconteo = null;
    public Date fesegundoconteo = null;
    public Date fetercerconteo = null;

    public InventarioDAO() {
    }

    public InventarioDAO(String compania, int flinventario) {
        this.compania = compania;
        this.flinventario = flinventario;
    }

    @Override
    public String getTable() {
        return "Inventario";
    }

    @Override
    public String getOrder() {
        return "compania, flinventario";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flinventario = "+flinventario;
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND flinventario > "+flinventario;
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND flinventario < "+flinventario;
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getOrderFirst() {
        return "flinventario";
    }

    @Override
    public String getOrderLast() {
        return "flinventario DESC";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        compania = values[0];
        flinventario = Numero.getIntFromString(values[1]);
    }

    @Override
    public void setValues(String[] values) throws Exception {
        compania = values[0];
        flinventario = Numero.getIntFromString(values[1]);
    }

    @Override
    public String toString() {
        return compania+";"+flinventario;
    }

    public void busca(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");

        InventarioBussines inventarioBussines = new InventarioBussines();

        InventarioDAO inventarioDAO = inventarioBussines.buscaInventario(ds, compania);
        if (inventarioDAO==null)
            throw new WebException("No existe un Inventario [PE] en esta compania.");

        Reflector.copyAllFields(inventarioDAO, this);
    }

    public void crea(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.descripcion = (String)json.get("descripcion");

        InventarioBussines inventarioBussines = new InventarioBussines();
        InventarioDAO inventarioDAO = inventarioBussines.creaInventario(ds, compania, descripcion);

        Reflector.copyAllFields(inventarioDAO, this);
    }

    public void carga(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flinventario = Integer.parseInt(json.get("flinventario").toString());
        boolean existe = ds.exists(this);

        if (!existe)
            throw new WebException("Este Inventario ["+this+"] no existe.");

        String almacen = (String)json.get("almacen");
        String laboratorio = (String)json.get("laboratorio");
        String productos = (String)json.get("productos");

        InventarioBussines inventarioBussines = new InventarioBussines();
        inventarioBussines.cargaInventario(ds, this, almacen, laboratorio, productos);
    }

    public void activaPrimero(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flinventario = Integer.parseInt(json.get("flinventario").toString());
        boolean existe = ds.exists(this);

        if (!existe)
            throw new WebException("Este Inventario ["+this+"] no existe.");

        InventarioBussines inventarioBussines = new InventarioBussines();
        inventarioBussines.activaPrimerConteo(ds, this);
    }

    public void activaSegundo(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flinventario = Integer.parseInt(json.get("flinventario").toString());
        boolean existe = ds.exists(this);

        if (!existe)
            throw new WebException("Este Inventario ["+this+"] no existe.");

        InventarioBussines inventarioBussines = new InventarioBussines();
        inventarioBussines.activaSegundoConteo(ds, this);
    }

    public void activaTercero(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flinventario = Integer.parseInt(json.get("flinventario").toString());
        boolean existe = ds.exists(this);

        if (!existe)
            throw new WebException("Este Inventario ["+this+"] no existe.");

        InventarioBussines inventarioBussines = new InventarioBussines();
        inventarioBussines.activaTercerConteo(ds, this);
    }

    public void afecta(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flinventario = Integer.parseInt(json.get("flinventario").toString());
        String usuario = (String)json.get("usuario");
        boolean existe = ds.exists(this);

        if (!existe)
            throw new WebException("Este Inventario ["+this+"] no existe.");

        InventarioBussines inventarioBussines = new InventarioBussines();
        inventarioBussines.afectaInventario(ds, this, usuario);
    }

    public void finaliza(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.flinventario = Integer.parseInt(json.get("flinventario").toString());
        String usuario = (String)json.get("usuario");
        boolean existe = ds.exists(this);

        if (!existe)
            throw new WebException("Este Inventario ["+this+"] no existe.");

        InventarioBussines inventarioBussines = new InventarioBussines();
        inventarioBussines.finalizaInventario(ds, this, usuario);
    }
}
