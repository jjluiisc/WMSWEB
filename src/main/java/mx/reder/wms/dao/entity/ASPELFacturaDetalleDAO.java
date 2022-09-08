package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELFacturaDetalleDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_DOC = null;
    public Integer NUM_PAR = null;
    public String CVE_ART = null;
    public Double CANT = null;
    public Double PXS = null;
    public Double PREC = null;
    public Double COST = null;
    public Double IMPU1 = null;
    public Double IMPU2 = null;
    public Double IMPU3 = null;
    public Double IMPU4 = null;
    public Short IMP1APLA = null;
    public Short IMP2APLA = null;
    public Short IMP3APLA = null;
    public Short IMP4APLA = null;
    public Double TOTIMP1 = null;
    public Double TOTIMP2 = null;
    public Double TOTIMP3 = null;
    public Double TOTIMP4 = null;
    public Double DESC1 = null;
    public Double DESC2 = null;
    public Double DESC3 = null;
    public Double COMI = null;
    public Double APAR = null;
    public String ACT_INV = null;
    public Integer NUM_ALM = null;
    public String POLIT_APLI = null;
    public Double TIP_CAM = null;
    public String UNI_VENTA = null;
    public String TIPO_PROD = null;
    public Integer CVE_OBS = null;
    public Integer REG_SERIE = null;
    public Integer E_LTPD = null;
    public String TIPO_ELEM = null;
    public Integer NUM_MOV = null;
    public Double TOT_PARTIDA = null;
    public String IMPRIMIR = null;
    public String MAN_IEPS = null;
    public Integer APL_MAN_IMP = null;
    public Double CUOTA_IEPS = null;
    public String APL_MAN_IEPS = null;
    public Double MTO_PORC = null;
    public Double MTO_CUOTA = null;
    public Integer CVE_ESQ = null;
    public String DESCR_ART = null;
    public String UUID = null;
    public Date VERSION_SINC = null;

    public ASPELFacturaDetalleDAO() {
    }

    public ASPELFacturaDetalleDAO(String CVE_DOC, Integer NUM_PAR) {
        this.CVE_DOC = CVE_DOC;
        this.NUM_PAR = NUM_PAR;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.PAR_FACTF"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_DOC, NUM_PAR";
    }

    @Override
    public String getWhere() {
        return "CVE_DOC = '"+CVE_DOC+"' AND NUM_PAR = "+NUM_PAR;
    }

    @Override
    public String toString() {
        return CVE_DOC+";"+NUM_PAR;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
