package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELMovimientoInventarioDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CVE_ART = null;
    public Integer ALMACEN = null;
    public Integer NUM_MOV = null;
    public Integer CVE_CPTO = null;
    public Date FECHA_DOCU = null;
    public String TIPO_DOC = null;
    public String REFER = null;
    public String CLAVE_CLPV = null;
    public String VEND = null;
    public Double CANT = null;
    public Double CANT_COST = null;
    public Double PRECIO = null;
    public Double COSTO = null;
    public String AFEC_COI = null;
    public Integer CVE_OBS = null;
    public Integer REG_SERIE = null;
    public String UNI_VENTA = null;
    public Integer E_LTPD = null;
    public Double EXIST_G = null;
    public Double EXISTENCIA = null;
    public String TIPO_PROD = null;
    public Double FACTOR_CON = null;
    public Date FECHAELAB = null;
    public Integer CTLPOL = null;
    public String CVE_FOLIO = null;
    public Integer SIGNO = null;
    public String COSTEADO = null;
    public Double COSTO_PROM_INI = null;
    public Double COSTO_PROM_FIN = null;
    public Double COSTO_PROM_GRAL = null;
    public String DESDE_INVE = null;
    public Integer MOV_ENLAZADO = null;

    public ASPELMovimientoInventarioDAO() {
    }

    public ASPELMovimientoInventarioDAO(String CVE_ART, Integer ALMACEN, Integer NUM_MOV, Integer CVE_CPTO) {
        this.CVE_ART = CVE_ART;
        this.ALMACEN = ALMACEN;
        this.NUM_MOV = NUM_MOV;
        this.CVE_CPTO = CVE_CPTO;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.MINVE"+empresa;
    }

    @Override
    public String getOrder() {
        return "CVE_ART, ALMACEN, NUM_MOV, CVE_CPTO";
    }

    @Override
    public String getWhere() {
        return "CVE_ART = '"+CVE_ART+"' AND ALMACEN = "+ALMACEN+" AND NUM_MOV = "+NUM_MOV+" AND CVE_CPTO = "+CVE_CPTO;
    }

    @Override
    public String toString() {
        return CVE_ART+";"+ALMACEN+";"+NUM_MOV+";"+CVE_CPTO;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
