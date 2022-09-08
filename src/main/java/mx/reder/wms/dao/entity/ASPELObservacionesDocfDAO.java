package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELObservacionesDocfDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public Integer CVE_OBS = 0;
    public String STR_OBS = "";

    public ASPELObservacionesDocfDAO() {
    }

    public ASPELObservacionesDocfDAO(Integer CVE_OBS) {
        this.CVE_OBS = CVE_OBS;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.OBS_DOCF"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_OBS";
    }

    @Override
    public String getWhere() {
        return "CVE_OBS = "+CVE_OBS;
    }

    @Override
    public String toString() {
        return CVE_OBS+";"+STR_OBS;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
