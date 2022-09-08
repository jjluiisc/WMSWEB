package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

public class ASPELControlDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public Integer ID_TABLA = 0;
    public Integer ULT_CVE = 0;
        
    public ASPELControlDAO() {
    }
    
    public ASPELControlDAO(Integer ID_TABLA) {
        this.ID_TABLA = ID_TABLA;
    }
    
    @Override
    public String getTable() {
        return "REDER20.dbo.TBLCONTROL"+empresa;
    }
    
    @Override
    public String getOrder() {
        return "ID_TABLA";
    }
    
    @Override
    public String getWhere() {
        return "ID_TABLA = "+ID_TABLA;
    }
    
    @Override
    public String toString() {
        return ID_TABLA+";"+ULT_CVE;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
