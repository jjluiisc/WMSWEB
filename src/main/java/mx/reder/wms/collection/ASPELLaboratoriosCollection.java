/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

/**
 *
 * @author Luis
 */
public class ASPELLaboratoriosCollection implements DatabaseRecordASPEL, CollectionRecord {
    protected String empresa = "";
    public String CAMPLIB11 = null;

    public ASPELLaboratoriosCollection() {
    }

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        return "SELECT CAMPLIB11 "
            +"FROM REDER20.dbo.INVE_CLIB"+empresa+" "
            +"WHERE "+where;
    }

    @Override
    public String getWhere() {
        return "CAMPLIB11 = '"+CAMPLIB11+"'";
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
    
}
