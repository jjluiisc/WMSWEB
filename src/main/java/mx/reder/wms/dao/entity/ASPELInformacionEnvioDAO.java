package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELInformacionEnvioDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public Integer CVE_INFO = 0;
    public String CVE_CONS = "";
    public String NOMBRE = "";
    public String CALLE = "";
    public String NUMINT = "";
    public String NUMEXT = "";
    public String CRUZAMIENTOS = "";
    public String CRUZAMIENTOS2 = "";
    public String POB = "";
    public String CURP = "";
    public String REFERDIR = "";
    public String CVE_ZONA = "";
    public Integer CVE_OBS = 0;
    public String STRNOGUIA = "";
    public String STRMODOENV = "";
    public Date FECHA_ENV = new Date(0);
    public String NOMBRE_RECEP = "";
    public String NO_RECEP = "";
    public Date FECHA_RECEP = new Date(0);
    public String COLONIA = "";
    public String CODIGO = "";
    public String ESTADO = "";
    public String PAIS = "";
    public String MUNICIPIO = "";

    public ASPELInformacionEnvioDAO() {
    }

    public ASPELInformacionEnvioDAO(Integer CVE_INFO) {
        this.CVE_INFO = CVE_INFO;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.INFENVIO"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_INFO";
    }

    @Override
    public String getWhere() {
        return "CVE_INFO = "+CVE_INFO;
    }

    @Override
    public String toString() {
        return CVE_INFO+";"+CVE_CONS;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
