package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELClienteCamposLibresDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_CLIE = "";
    public String CAMPLIB1 = "";
    public String CAMPLIB2 = "";
    public String CAMPLIB3 = "";
    public Integer CAMPLIB4 = 0;
    public Double CAMPLIB5 = 0.0;
    public Double CAMPLIB6 = 0.0;
    public String CAMPLIB7 = "";
    public String CAMPLIB8 = "";
    public String CAMPLIB9 = "";

    public ASPELClienteCamposLibresDAO() {
    }

    public ASPELClienteCamposLibresDAO(String CVE_CLIE) {
        this.CVE_CLIE = CVE_CLIE;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.CLIE_CLIB"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_CLIE";
    }

    @Override
    public String getWhere() {
        return "CVE_CLIE = '"+CVE_CLIE+"'";
    }

    @Override
    public String toString() {
        return CVE_CLIE;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
