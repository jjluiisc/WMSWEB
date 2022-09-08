package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELVendedorDAO implements DatabaseRecord, DatabaseRecordABC, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_VEND = "";
    public String STATUS = "";
    public String NOMBRE = "";
    public Double COMI = 0.0;
    public String CLASIFIC = "";
    public String CORREOE = "";
    public String UUID = "";
    public Date VERSION_SINC = new Date(0);

    public ASPELVendedorDAO() {
    }

    public ASPELVendedorDAO(String CVE_VEND) {
        this.CVE_VEND = CVE_VEND;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.VEND"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_VEND";
    }

    @Override
    public String getWhere() {
        return "CVE_VEND = '"+CVE_VEND+"'";
    }

    @Override
    public String toString() {
        return CVE_VEND+";"+STATUS+";"+NOMBRE;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    @Override
    public String getWhereFirst() {
        return "CVE_VEND = '"+CVE_VEND+"'";
    }

    @Override
    public String getWhereNext() {
        return "CVE_VEND > '"+CVE_VEND+"'";
    }

    @Override
    public String getWherePrev() {
        return "CVE_VEND < '"+CVE_VEND+"'";
    }

    @Override
    public String getWhereLast() {
        return "CVE_VEND = '"+CVE_VEND+"'";
    }

    @Override
    public String getOrderFirst() {
        return "CVE_VEND";
    }

    @Override
    public String getOrderLast() {
        return "CVE_VEND";
    }

    @Override
    public void setKey(String[] values) throws Exception {
        CVE_VEND = values[0];
    }

    @Override
    public void setValues(String[] values) throws Exception {
        CVE_VEND = values[0];
    }
}
