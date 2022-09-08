package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.math.BigDecimal;
import java.util.Date;

public class InventarioConteoUbicacionDAO implements DatabaseRecord {
    public Integer flduinventario = 0;
    public String compania = "";
    public int flinventario = 0;
    public int fldinventario = 0;
    public String codigo = "";
    public String descripcion = "";
    public BigDecimal costo = BigDecimal.ZERO;
    public BigDecimal existencia = BigDecimal.ZERO;
    public BigDecimal existencia1 = BigDecimal.ZERO;
    public BigDecimal existencia2 = BigDecimal.ZERO;
    public BigDecimal existencia3 = BigDecimal.ZERO;
    public BigDecimal existenciac = BigDecimal.ZERO;
    public String ubicacion = "";
    public String lote = "";
    public Date fecaducidad = new Date(0);

    public InventarioConteoUbicacionDAO() {
    }

    public InventarioConteoUbicacionDAO(int flduinventario) {
        this.flduinventario = flduinventario;
    }

    @Override
    public String getTable() {
        return "InventarioConteoUbicacion";
    }

    @Override
    public String getOrder() {
        return "flduinventario";
    }

    @Override
    public String getWhere() {
        return "flduinventario = "+flduinventario;
    }
}
