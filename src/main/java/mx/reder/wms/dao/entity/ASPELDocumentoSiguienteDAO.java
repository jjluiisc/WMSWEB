package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELDocumentoSiguienteDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String TIP_DOC = null;
    public String CVE_DOC = null;
    public String ANT_SIG = null;
    public String TIP_DOC_E = null;
    public String CVE_DOC_E = null;
    public Integer PARTIDA = null;
    public Integer PART_E = null;
    public Double CANT_E = null;

    public ASPELDocumentoSiguienteDAO() {
    }

    public ASPELDocumentoSiguienteDAO(String TIP_DOC, String CVE_DOC, String ANT_SIG, String TIP_DOC_E, String CVE_DOC_E,
            Integer PARTIDA, Integer PART_E) {
        this.TIP_DOC = TIP_DOC;
        this.CVE_DOC = CVE_DOC;
        this.ANT_SIG = ANT_SIG;
        this.TIP_DOC_E = TIP_DOC_E;
        this.CVE_DOC_E = CVE_DOC_E;
        this.PARTIDA = PARTIDA;
        this.PART_E = PART_E;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.DOCTOSIGF"+empresa;
    }

    @Override
    public String getOrder() {
        return "TIP_DOC, CVE_DOC, ANT_SIG, TIP_DOC_E, CVE_DOC_E, PARTIDA, PART_E";
    }

    @Override
    public String getWhere() {
        return "TIP_DOC = '"+TIP_DOC+"' AND CVE_DOC = '"+CVE_DOC+"' AND ANT_SIG = '"+ANT_SIG+"' "
                +"AND TIP_DOC_E = '"+TIP_DOC_E+"' AND CVE_DOC_E = '"+CVE_DOC_E+"' "
                +"AND PARTIDA = "+PARTIDA+" AND PART_E = "+PART_E;
    }

    @Override
    public String toString() {
        return TIP_DOC+";"+CVE_DOC+";"+ANT_SIG+";"+TIP_DOC_E+";"+CVE_DOC_E+";"+PARTIDA+";"+PART_E;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
