package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELCuentaPorCobrarDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_CLIE = null;
    public String REFER = null;
    public Integer NUM_CPTO = null;
    public Integer NUM_CARGO = null;
    public Integer CVE_OBS = null;
    public String NO_FACTURA = null;
    public String DOCTO = null;
    public Double IMPORTE = null;
    public Date FECHA_APLI = null;
    public Date FECHA_VENC = null;
    public String AFEC_COI = null;
    public String STRCVEVEND = null;
    public Integer NUM_MONED = null;
    public Double TCAMBIO = null;
    public Double IMPMON_EXT = null;
    public Date FECHAELAB = null;
    public Integer CTLPOL = null;
    public String CVE_FOLIO = null;
    public String TIPO_MOV = null;
    public Integer CVE_BITA = null;
    public Integer SIGNO = null;
    public Integer CVE_AUT = null;
    public Integer USUARIO = null;
    public String ENTREGADA = null;
    public Date FECHA_ENTREGA = null;
    public String STATUS = null;
    public String REF_SIST = null;
    public String UUID = null;
    public Date VERSION_SINC = null;

    public ASPELCuentaPorCobrarDAO() {
    }

    public ASPELCuentaPorCobrarDAO(String CVE_CLIE, String REFER, Integer NUM_CPTO, Integer NUM_CARGO) {
        this.CVE_CLIE = CVE_CLIE;
        this.REFER = REFER;
        this.NUM_CPTO = NUM_CPTO;
        this.NUM_CARGO = NUM_CARGO;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.CUEN_M"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_CLIE, REFER, NUM_CPTO, NUM_CARGO";
    }

    @Override
    public String getWhere() {
        return "CVE_CLIE = '"+CVE_CLIE+"' AND REFER = '"+REFER+"' AND NUM_CPTO = "+NUM_CPTO+" AND NUM_CARGO = "+NUM_CARGO;
    }

    @Override
    public String toString() {
        return CVE_CLIE+";"+REFER+";"+NUM_CPTO+";"+NUM_CARGO;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
