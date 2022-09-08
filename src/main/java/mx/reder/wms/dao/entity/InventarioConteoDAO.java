package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.math.BigDecimal;

public class InventarioConteoDAO implements DatabaseRecord {
    public int fldinventario = 0;
    public String compania = "";
    public int flinventario = 0;
    public String codigo = "";
    public String descripcion = "";
    public BigDecimal costo = BigDecimal.ZERO;
    public BigDecimal existencia = BigDecimal.ZERO;
    public BigDecimal existencia1 = BigDecimal.ZERO;
    public BigDecimal existencia2 = BigDecimal.ZERO;
    public BigDecimal existencia3 = BigDecimal.ZERO;
    public BigDecimal existenciac = BigDecimal.ZERO;
    public String status = "";
    public String terminal = "";
    public String laboratorio = "";

    public InventarioConteoDAO() {
    }

    public InventarioConteoDAO(int fldinventario) {
        this.fldinventario = fldinventario;
    }

    @Override
    public String getTable() {
        return "InventarioConteo";
    }

    @Override
    public String getOrder() {
        return "fldinventario";
    }

    @Override
    public String getWhere() {
        return "fldinventario = "+fldinventario;
    }
}
