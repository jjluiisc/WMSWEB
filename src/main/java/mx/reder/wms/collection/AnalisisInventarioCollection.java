package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.math.BigDecimal;

/**
 *
 * @author joelbecerram
 */
public class AnalisisInventarioCollection implements DatabaseRecordASPEL, CollectionRecord {
    protected String empresa = "";
    public int fldinventario = 0;
    public int flinventario = 0;
    public String codigo = "";
    public String descripcion = "";
    public BigDecimal costo = BigDecimal.ZERO;
    public BigDecimal existencia = BigDecimal.ZERO;
    public BigDecimal existenciac = BigDecimal.ZERO;
    public BigDecimal diferencia = BigDecimal.ZERO;
    public BigDecimal valor = BigDecimal.ZERO;
    public String status = "";
    public String terminal = "";
    public String laboratorio = "";

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        if(empresa.isEmpty()){
            setEmpresa("01");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ic.fldinventario, ic.flinventario, ic.codigo, ic.descripcion, ic.costo, ic.existencia, ic.existenciac, ")
            .append("ROUND((ic.existenciac - ic.existencia), 2) AS diferencia, ROUND(((ic.existenciac - ic.existencia) * ic.costo), 4) AS valor, ")
            .append("ic.status, ic.terminal, ISNULL(ic.laboratorio,'') AS laboratorio ")
            .append("FROM InventarioConteo ic ")
            .append("WHERE ").append(where).append(" ")
            .append("ORDER BY codigo");
        return sql.toString();
    }

    @Override
    public String getWhere() {
        return "flinventario = "+flinventario;
    }
    
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
