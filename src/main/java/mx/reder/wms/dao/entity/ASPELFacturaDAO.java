package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELFacturaDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String TIP_DOC = null;
    public String CVE_DOC = null;
    public String CVE_CLPV = null;
    public String STATUS = null;
    public Integer DAT_MOSTR = null;
    public String CVE_VEND = null;
    public String CVE_PEDI = null;
    public Date FECHA_DOC = null;
    public Date FECHA_ENT = null;
    public Date FECHA_VEN = null;
    public Date FECHA_CANCELA = null;
    public Double CAN_TOT = null;
    public Double IMP_TOT1 = null;
    public Double IMP_TOT2 = null;
    public Double IMP_TOT3 = null;
    public Double IMP_TOT4 = null;
    public Double DES_TOT = null;
    public Double DES_FIN = null;
    public Double COM_TOT = null;
    public String CONDICION = null;
    public Integer CVE_OBS = null;
    public Integer NUM_ALMA = null;
    public String ACT_CXC = null;
    public String ACT_COI = null;
    public String ENLAZADO = null;
    public String TIP_DOC_E = null;
    public Integer NUM_MONED = null;
    public Double TIPCAMB = null;
    public Integer NUM_PAGOS = null;
    public Date FECHAELAB = null;
    public Double PRIMERPAGO = null;
    public String RFC = null;
    public Integer CTLPOL = null;
    public String ESCFD = null;
    public Integer AUTORIZA = null;
    public String SERIE = null;
    public Integer FOLIO = null;
    public String AUTOANIO = null;
    public Integer DAT_ENVIO = null;
    public String CONTADO = null;
    public Integer CVE_BITA = null;
    public String BLOQ = null;
    public String FORMAENVIO = null;
    public Double DES_FIN_PORC = null;
    public Double DES_TOT_PORC = null;
    public Double IMPORTE = null;
    public Double COM_TOT_PORC = null;
    public String METODODEPAGO = null;
    public String NUMCTAPAGO = null;
    public String TIP_DOC_ANT = null;
    public String DOC_ANT = null;
    public String TIP_DOC_SIG = null;
    public String DOC_SIG = null;
    public String UUID = null;
    public Date VERSION_SINC = null;
    public String FORMADEPAGOSAT = null;
    public String USO_CFDI = null;

    public ASPELFacturaDAO() {
    }

    public ASPELFacturaDAO(String CVE_DOC) {
        this.CVE_DOC = CVE_DOC;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.FACTF"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_DOC";
    }

    @Override
    public String getWhere() {
        return "CVE_DOC = '"+CVE_DOC+"'";
    }

    @Override
    public String toString() {
        return TIP_DOC+";"+CVE_DOC;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
