package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;

/**
 *
 * @author joelbecerram
 */
public class ASPELLineasCollection implements DatabaseRecordASPEL, CollectionRecord {
    protected String empresa = "";
    public String CVE_LIN = null;
    public String DESC_LIN = null;
    public String ESUNGPO = null;
    public String CUENTA_COI = null;
    public String STATUS = null;

    public ASPELLineasCollection() {
    }

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        return "SELECT l.* "
            +"FROM REDER20.dbo.CLIN"+empresa+" l "
            +"WHERE "+where;
    }

    @Override
    public String getWhere() {
        return "CVE_LIN = '"+CVE_LIN+"'";
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
