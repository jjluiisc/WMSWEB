package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELEnlaceLotePedimentoDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public Integer E_LTPD = null;
    public Integer REG_LTPD = null;
    public Double CANTIDAD = null;
    public Double PXRS = null;

    public ASPELEnlaceLotePedimentoDAO() {
    }

    public ASPELEnlaceLotePedimentoDAO(Integer E_LTPD) {
        this.E_LTPD = E_LTPD;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.ENLACE_LTPD"+empresa;
    }

    @Override
    public String getOrder() {
        return "E_LTPD";
    }

    @Override
    public String getWhere() {
        return "E_LTPD = "+E_LTPD;
    }

    @Override
    public String toString() {
        return E_LTPD+";";
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
