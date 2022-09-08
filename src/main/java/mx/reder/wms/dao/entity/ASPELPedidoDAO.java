package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELPedidoDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String TIP_DOC = "";
    public String CVE_DOC = "";
    public String CVE_CLPV = "";
    public String STATUS = "";
    public Integer DAT_MOSTR = 0;
    public String CVE_VEND = "";
    public String CVE_PEDI = "";
    public Date FECHA_DOC = new Date(0);
    public Date FECHA_ENT = new Date(0);
    public Date FECHA_VEN = new Date(0);
    public Date FECHA_CANCELA = new Date(0);
    public Double CAN_TOT = 0.0;
    public Double IMP_TOT1 = 0.0;
    public Double IMP_TOT2 = 0.0;
    public Double IMP_TOT3 = 0.0;
    public Double IMP_TOT4 = 0.0;
    public Double DES_TOT = 0.0;
    public Double DES_FIN = 0.0;
    public Double COM_TOT = 0.0;
    public String CONDICION = "";
    public Integer CVE_OBS = 0;
    public Integer NUM_ALMA = 0;
    public String ACT_CXC = "";
    public String ACT_COI = "";
    public String ENLAZADO = "";
    public String TIP_DOC_E = "";
    public Integer NUM_MONED = 0;
    public Double TIPCAMB = 0.0;
    public Integer NUM_PAGOS = 0;
    public Date FECHAELAB = new Date(0);
    public Double PRIMERPAGO = 0.0;
    public String RFC = "";
    public Integer CTLPOL = 0;
    public String ESCFD = "";
    public Integer AUTORIZA = 0;
    public String SERIE = "";
    public Integer FOLIO = 0;
    public String AUTOANIO = "";
    public Integer DAT_ENVIO = 0;
    public String CONTADO = "";
    public Integer CVE_BITA = 0;
    public String BLOQ = "";
    public String FORMAENVIO = "";
    public Double DES_FIN_PORC = 0.0;
    public Double DES_TOT_PORC = 0.0;
    public Double IMPORTE = 0.0;
    public Double COM_TOT_PORC = 0.0;
    public String METODODEPAGO = "";
    public String NUMCTAPAGO = "";
    public String TIP_DOC_ANT = "";
    public String DOC_ANT = "";
    public String TIP_DOC_SIG = "";
    public String DOC_SIG = "";
    public String UUID = "";
    public Date VERSION_SINC = new Date(0);
    public String FORMADEPAGOSAT = "";
    public String USO_CFDI = "";

    public ASPELPedidoDAO() {
    }

    public ASPELPedidoDAO(String CVE_DOC) {
        this.CVE_DOC = CVE_DOC;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.FACTP"+empresa;
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
