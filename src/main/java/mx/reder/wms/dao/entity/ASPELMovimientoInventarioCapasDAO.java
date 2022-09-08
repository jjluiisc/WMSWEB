package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELMovimientoInventarioCapasDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_ART = null;
    public Integer NUM_MOV = null;
    public Integer NUM_MOV_AFT = null;
    public Double CANT_AFT = null;
    public Double COSTO_AFT = null;

    public ASPELMovimientoInventarioCapasDAO() {
    }

    public ASPELMovimientoInventarioCapasDAO(String CVE_ART, Integer NUM_MOV, Integer NUM_MOV_AFT) {
        this.CVE_ART = CVE_ART;
        this.NUM_MOV = NUM_MOV;
        this.NUM_MOV_AFT = NUM_MOV_AFT;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.CAPAS_X_MOV"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_ART, NUM_MOV, NUM_MOV_AFT";
    }

    @Override
    public String getWhere() {
        return "CVE_ART = '"+CVE_ART+"' AND NUM_MOV = "+NUM_MOV+" AND NUM_MOV_AFT = "+NUM_MOV_AFT;
    }

    @Override
    public String toString() {
        return CVE_ART+";"+NUM_MOV+";"+NUM_MOV_AFT;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
