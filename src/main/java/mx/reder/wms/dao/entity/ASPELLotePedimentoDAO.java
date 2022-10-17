package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELLotePedimentoDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_ART = null;
    public String LOTE = null;
    public String PEDIMENTO = null;
    public Integer CVE_ALM = null;
    public Date FCHCADUC = null;
    public Date FCHADUANA = null;
    public Date FCHULTMOV = null;
    public String NOM_ADUAN = null;
    public Double CANTIDAD = null;
    public Integer REG_LTPD = null;
    public Integer CVE_OBS = null;
    public String CIUDAD = null;
    public String FRONTERA = null;
    public Date FEC_PROD_LT = null;
    public String GLN = null;
    public String STATUS = null;
    public String PEDIMENTOSAT = null;

    public ASPELLotePedimentoDAO() {
    }

    public ASPELLotePedimentoDAO(Integer REG_LTPD) {
        this.REG_LTPD = REG_LTPD;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.LTPD"+empresa;
    }

    @Override
    public String getOrder() {
        return "REG_LTPD";
    }

    @Override
    public String getWhere() {
        return "REG_LTPD = "+REG_LTPD;
    }

    @Override
    public String toString() {
        return REG_LTPD+";";
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
