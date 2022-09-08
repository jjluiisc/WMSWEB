package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELLineaDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_LIN = null;
    public String DESC_LIN = null;
    public String ESUNGPO = null;
    public String CUENTA_COI = null;
    public String STATUS = null;

    public ASPELLineaDAO() {
    }

    public ASPELLineaDAO(String CVE_LIN) {
        this.CVE_LIN = CVE_LIN;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.CLIN"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_LIN";
    }

    @Override
    public String getWhere() {
        return "CVE_LIN = '"+CVE_LIN+"'";
    }

    @Override
    public String toString() {
        return CVE_LIN+";"+DESC_LIN;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
