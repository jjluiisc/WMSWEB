package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELCFDIDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String TIPO_DOC = null;
    public String CVE_DOC = null;
    public String VERSION = null;
    public String UUID = null;
    public String NO_SERIE = null;
    public String FECHA_CERT = null;
    public String FECHA_CANCELA = null;
    public String XML_DOC = null;
    public String XML_DOC_CANCELA = null;
    public String DESGLOCEIMP1 = null;
    public String DESGLOCEIMP2 = null;
    public String DESGLOCEIMP3 = null;
    public String DESGLOCEIMP4 = null;
    public String MSJ_CANC = null;
    public String PENDIENTE = null;
    public Integer CVE_USUARIO = null;

    public ASPELCFDIDAO() {
    }

    public ASPELCFDIDAO(String TIPO_DOC, String CVE_DOC) {
        this.TIPO_DOC = TIPO_DOC;
        this.CVE_DOC = CVE_DOC;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.CFDI"+empresa;
    }

    @Override
    public String getOrder() {
        return "TIPO_DOC, CVE_DOC";
    }

    @Override
    public String getWhere() {
        return "TIPO_DOC = '"+TIPO_DOC+"' AND CVE_DOC = '"+CVE_DOC+"'";
    }

    @Override
    public String toString() {
        return TIPO_DOC+";"+CVE_DOC;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
