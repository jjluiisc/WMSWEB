package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author joelbecerram
 */
public class ASPELProductosLaboratorioCollection implements DatabaseRecordASPEL, CollectionRecord {
    protected String empresa = "";
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
    public String laboratorio = "";

    public ASPELProductosLaboratorioCollection() {
    }

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        return "SELECT '"+empresa+"' AS compania, i.CVE_ART AS codigo, i.DESCR AS descripcion, i.UNI_MED AS unidadmedida, "
            +"CONVERT(INT,0) AS linea, CONVERT(INT,0) AS  categoria, '' AS marca, CONVERT(INT,0) AS capa, "
	    +"CONVERT(DECIMAL(18,6),0.000000) AS existencia, CONVERT(DECIMAL(18,6),0.000000) AS costo, "
	    +"GETDATE() AS modificacion, ISNULL(CAMPLIB11,'') AS laboratorio "
            +"FROM INVE"+empresa+" i "
            +"LEFT OUTER JOIN INVE_CLIB"+empresa+" l ON i.CVE_ART = l.CVE_PROD " 
            +"LEFT OUTER JOIN MULT"+empresa+" a ON i.CVE_ART = a.CVE_ART "
            +"WHERE "+where;
    }

    @Override
    public String getWhere() {
        return "i.CVE_ART = '"+codigo+"'";
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
