package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

/**
 *
 * @author joelbecerram
 */
public class ASPELPedidosDetallesCollection implements DatabaseRecordASPEL, CollectionRecord {
    protected String empresa = "";
    public String CVE_DOC = "";
    public Integer NUM_PAR = 0;
    public String CVE_ART = "";
    public Double CANT = 0.0;
    public Double PXS = 0.0;
    public Double PREC = 0.0;
    public Double COST = 0.0;
    public Double IMPU1 = 0.0;
    public Double IMPU2 = 0.0;
    public Double IMPU3 = 0.0;
    public Double IMPU4 = 0.0;
    public Short IMP1APLA = 0;
    public Short IMP2APLA = 0;
    public Short IMP3APLA = 0;
    public Short IMP4APLA = 0;
    public Double TOTIMP1 = 0.0;
    public Double TOTIMP2 = 0.0;
    public Double TOTIMP3 = 0.0;
    public Double TOTIMP4 = 0.0;
    public Double DESC1 = 0.0;
    public Double DESC2 = 0.0;
    public Double DESC3 = 0.0;
    public Double COMI = 0.0;
    public Double APAR = 0.0;
    public String ACT_INV = "";
    public Integer NUM_ALM = 0;
    public String POLIT_APLI = "";
    public Double TIP_CAM = 0.0;
    public String UNI_VENTA = "";
    public String TIPO_PROD = "";
    public Integer CVE_OBS = 0;
    public Integer REG_SERIE = 0;
    public Integer E_LTPD = 0;
    public String TIPO_ELEM = "";
    public Integer NUM_MOV = 0;
    public Double TOT_PARTIDA = 0.0;
    public String IMPRIMIR = "";
    public String UUID = "";
    public Date VERSION_SINC = new Date(0);
    public String MAN_IEPS = "";
    public Integer APL_MAN_IMP = 0;
    public Double CUOTA_IEPS = 0.0;
    public String APL_MAN_IEPS = "";
    public Double MTO_PORC = 0.0;
    public Double MTO_CUOTA = 0.0;
    public Integer CVE_ESQ = 0;
    public String DESCR_ART = "";

    public String DESCR = "";
    public String LIN_PROD = "";
    public String CON_SERIE = "";
    public String UNI_MED = "";
    public Double UNI_EMP = 0.0;
    public String CTRL_ALM = "";

    public Double CAMPLIB6 = 0.0;
    public String CAMPLIB15 = "";
    public Double CAMPLIB5 = 0.0;

    public ASPELPedidosDetallesCollection() {
    }

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        return "SELECT pd.*, i.DESCR, i.LIN_PROD, i.CON_SERIE, i.UNI_MED, i.UNI_EMP, i.CTRL_ALM, il.CAMPLIB6, il.CAMPLIB15, il.CAMPLIB5 "
            +"FROM REDER20.dbo.PAR_FACTP"+empresa+" pd "
            +"LEFT JOIN REDER20.dbo.INVE"+empresa+" i ON i.CVE_ART  = pd.CVE_ART "
            +"LEFT JOIN REDER20.dbo.INVE_CLIB"+empresa+" il ON il.CVE_PROD  = i.CVE_ART "
            +"WHERE "+where;
    }

    @Override
    public String getWhere() {
        return "CVE_DOC = '"+CVE_DOC+"'";
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
