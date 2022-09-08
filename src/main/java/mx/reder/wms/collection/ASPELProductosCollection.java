package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

/**
 *
 * @author joelbecerram
 */
public class ASPELProductosCollection implements DatabaseRecordASPEL, CollectionRecord {
    protected String empresa = "";
    public String CVE_ART = null;
    public String DESCR = null;
    public String LIN_PROD = null;
    public String CON_SERIE = null;
    public String UNI_MED = null;
    public Double UNI_EMP = null;
    public String CTRL_ALM = null;
    public Integer TIEM_SURT = null;
    public Double STOCK_MIN = null;
    public Double STOCK_MAX = null;
    public String TIP_COSTEO = null;
    public Integer NUM_MON = null;
    public Date FCH_ULTCOM = null;
    public Double COMP_X_REC = null;
    public Date FCH_ULTVTA = null;
    public Double PEND_SURT = null;
    public Double EXIST = null;
    public Double COSTO_PROM = null;
    public Double ULT_COSTO = null;
    public Integer CVE_OBS = null;
    public String TIPO_ELE = null;
    public String UNI_ALT = null;
    public Double FAC_CONV = null;
    public Double APART = null;
    public String CON_LOTE = null;
    public String CON_PEDIMENTO = null;
    public Double PESO = null;
    public Double VOLUMEN = null;
    public Integer CVE_ESQIMPU = null;
    public Integer CVE_BITA = null;
    public Double VTAS_ANL_C = null;
    public Double VTAS_ANL_M = null;
    public Double COMP_ANL_C = null;
    public Double COMP_ANL_M = null;
    public String PREFIJO = null;
    public String TALLA = null;
    public String COLOR = null;
    public String CUENT_CONT = null;
    public String CVE_IMAGEN = null;
    public String BLK_CST_EXT = null;
    public String STATUS = null;
    public String MAN_IEPS = null;
    public Integer APL_MAN_IMP = null;
    public Double CUOTA_IEPS = null;
    public String APL_MAN_IEPS = null;
    public String UUID = null;
    public Date VERSION_SINC = null;
    public Date VERSION_SINC_FECHA_IMG = null;
    public String CVE_PRODSERV = null;
    public String CVE_UNIDAD = null;

    public ASPELProductosCollection() {
    }

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        return "SELECT i.* "
            +"FROM REDER20.dbo.INVE"+empresa+" i "
            +"WHERE "+where;
    }

    @Override
    public String getWhere() {
        return "i.CVE_ART = '"+CVE_ART+"'";
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
