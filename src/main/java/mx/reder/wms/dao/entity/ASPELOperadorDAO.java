package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELOperadorDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_OPE = null;
    public String NOM_OPE = null;
    public String XML_OPE = null;
    public String TIPO_FIG = null;

    public ASPELOperadorDAO() {
    }

    public ASPELOperadorDAO(String CVE_OPE) {
        this.CVE_OPE = CVE_OPE;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.OPERADOR"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_OPE";
    }

    @Override
    public String getWhere() {
        return "CVE_OPE = '"+CVE_OPE+"'";
    }

    @Override
    public String toString() {
        return CVE_OPE+";"+TIPO_FIG;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEmpresa() {
        return empresa;
    }
}
