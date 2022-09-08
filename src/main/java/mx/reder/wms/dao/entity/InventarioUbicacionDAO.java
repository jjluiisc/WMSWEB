package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;

public class InventarioUbicacionDAO implements DatabaseRecord {
    public String compania = "";
    public int flinventario = 0;
    public String ubicacion = "";

    public InventarioUbicacionDAO() {
    }

    public InventarioUbicacionDAO(String compania, int flinventario, String ubicacion) {
        this.compania = compania;
        this.flinventario = flinventario;
        this.ubicacion = ubicacion;
    }

    @Override
    public String getTable() {
        return "InventarioUbicacion";
    }

    @Override
    public String getOrder() {
        return "compania, flinventario, ubicacion";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flinventario = "+flinventario+" AND ubicacion = '"+ubicacion+"'";
    }

    @Override
    public String toString() {
        return compania+";"+flinventario+";"+ubicacion;
    }
}
