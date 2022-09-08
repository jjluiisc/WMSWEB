package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

/**
 *
 * @author joelbecerram
 */
public class ASPELLotesProductosCollection implements DatabaseRecordASPEL, CollectionRecord {
    protected String empresa = "";
    public String CVE_ART = null;
    public String LOTE = null;
    public String PEDIMENTO = null;
    public Integer CVE_ALM = null;
    public Date FCHCADUC = null;
    public Date FCHADUANA = null;
    public Date FCHULTMOV = null;
    public String NOM_ADUAN = null;
    public Double CANTIDAD = null;
    public Integer REG_LTPD = null;
    public Integer CVE_OBS = null;
    public String CIUDAD = null;
    public String FRONTERA = null;
    public Date FEC_PROD_LT = null;
    public String GLN = null;
    public String STATUS = null;
    public String PEDIMENTOSAT = null;

    public String DESCR = null;

    public ASPELLotesProductosCollection() {
    }

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        return "SELECT l.*, i.DESCR "
            +"FROM REDER20.dbo.LTPD"+empresa+" l "
            +"LEFT JOIN REDER20.dbo.INVE"+empresa+" i ON i.CVE_ART = l.CVE_ART "
            +"WHERE "+where;
    }

    @Override
    public String getWhere() {
        return "i.CVE_ART = '"+CVE_ART+"'";
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
