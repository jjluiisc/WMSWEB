package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.math.BigDecimal;

public class InventarioDetalleDAO implements DatabaseRecord {
    public Integer fldinventario = 0;
    public String compania = "";
    public int flinventario = 0;
    public String codigo = "";
    public String descripcion = "";
    public BigDecimal costo = BigDecimal.ZERO;
    public String unidadmedida = "";
    public BigDecimal existencia = BigDecimal.ZERO;
    public String ubicacion = "";
    public String laboratorio = "";

    public InventarioDetalleDAO() {
    }

    public InventarioDetalleDAO(int fldinventario) {
        this.fldinventario = fldinventario;
    }

    @Override
    public String getTable() {
        return "InventarioDetalle";
    }

    @Override
    public String getOrder() {
        return "fldinventario";
    }

    @Override
    public String getWhere() {
        return "fldinventario = "+fldinventario;
    }

    @Override
    public String toString() {
        return fldinventario+";"+compania+";"+flinventario;
    }
}
