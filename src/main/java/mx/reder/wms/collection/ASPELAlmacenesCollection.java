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
public class ASPELAlmacenesCollection implements DatabaseRecordASPEL, CollectionRecord {
    protected String empresa = "";
    public Integer CVE_ALM  = 0;
    public String DESCR = null;

    public ASPELAlmacenesCollection() {
    }

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        return "SELECT CVE_ALM, DESCR "
            +"FROM REDER20.dbo.ALMACENES"+empresa+" "
            +"WHERE "+where;
    }

    @Override
    public String getWhere() {
        return "CVE_ALM = "+CVE_ALM;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
    
}
