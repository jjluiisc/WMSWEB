package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELPedidoDetalleDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
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

    public ASPELPedidoDetalleDAO() {
    }

    public ASPELPedidoDetalleDAO(String CVE_DOC, Integer NUM_PAR) {
        this.CVE_DOC = CVE_DOC;
        this.NUM_PAR = NUM_PAR;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.PAR_FACTP"+empresa;
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
