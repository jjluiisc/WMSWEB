package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELProductoAlmacenDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_ART = null;
    public Integer CVE_ALM = null;
    public String STATUS = null;
    public String CTRL_ALM = null;
    public Double EXIST = null;
    public Double STOCK_MIN = null;
    public Double STOCK_MAX = null;
    public Double COMP_X_REC = null;
    public String UUID = null;
    public Date VERSION_SINC = null;
        
    public ASPELProductoAlmacenDAO() {
    }
    
    public ASPELProductoAlmacenDAO(String CVE_ART, Integer CVE_ALM) {
        this.CVE_ART = CVE_ART;
        this.CVE_ALM = CVE_ALM;
    }
    
    @Override
    public String getTable() {
        return "REDER20.dbo.MULT"+empresa;
    }
    
    @Override
    public String getOrder() {
        return "CVE_ART, CVE_ALM";
    }
    
    @Override
    public String getWhere() {
        return "CVE_ART = '"+CVE_ART+"' AND CVE_ALM = "+CVE_ALM;
    }
    
    @Override
    public String toString() {
        return CVE_ART+";"+CVE_ALM;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
