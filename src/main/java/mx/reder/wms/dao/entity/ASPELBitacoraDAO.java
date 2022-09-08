package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELBitacoraDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public Integer CVE_BITA = 0;
    public String CVE_CLIE = "";
    public String CVE_CAMPANIA = "";
    public String CVE_ACTIVIDAD = "";
    public Date FECHAHORA = new Date(0);
    public Integer CVE_USUARIO = 0;
    public String OBSERVACIONES = "";
    public String STATUS = "";
    public String NOM_USUARIO = "";

    public ASPELBitacoraDAO() {
    }

    public ASPELBitacoraDAO(Integer CVE_BITA) {
        this.CVE_BITA = CVE_BITA;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.BITA"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_BITA";
    }

    @Override
    public String getWhere() {
        return "CVE_BITA = "+CVE_BITA;
    }

    @Override
    public String toString() {
        return CVE_BITA+";"+CVE_ACTIVIDAD;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
