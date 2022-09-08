/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Luis
 */
public class LTPDDAO implements DatabaseRecord{
    public int REG_LTPD = 0;
    public String compania = "";
    public String CVE_ART = "";
    public String LOTE = "";
    public String PEDIMENTO = "";
    public int CVE_ALM = 0;
    public Date FCHCADUC = null;        
    public Date FCHULTMOV = null;        
    public BigDecimal CANTIDAD = BigDecimal.ZERO;
        
    public LTPDDAO() {
    }

    public LTPDDAO(String compania, int REG_LTPD) {
        this.compania = compania;
        this.REG_LTPD = REG_LTPD;
    }
        
    @Override
    public String getTable() {
        return "LTPD";
    }

    @Override
    public String getOrder() {
        return "REG_LTPD";
    }

    @Override
    public String getWhere() {
        return "REG_LTPD = "+REG_LTPD +" AND compania = '"+compania+"'";
    }

    @Override
    public String toString() {
        return REG_LTPD+";"+compania;
    }
}
