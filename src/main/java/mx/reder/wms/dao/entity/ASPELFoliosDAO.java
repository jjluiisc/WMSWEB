package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELFoliosDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String TIP_DOC = "";
    public Integer FOLIODESDE = 0;
    public Integer FOLIOHASTA = 0;
    public Integer AUTORIZA = 0;
    public String SERIE = "";
    public String AUTOANIO = "";
    public Integer ULT_DOC = 0;
    public String TIPO = "";
    public Date FECH_ULT_DOC = new Date(0);
    public String CBB = "";
    public Date FECHAAPROBCBB = new Date(0);
    public byte[] IMGCBB = new byte[0];
    public String FOLIOPERSONALIZADO = "";
    public String PARCIALIDAD = "";
    public String STATUS = "";

    public ASPELFoliosDAO() {
    }

    public ASPELFoliosDAO(String TIP_DOC, int FOLIODESDE, String SERIE) {
        this.TIP_DOC = TIP_DOC;
        this.FOLIODESDE = FOLIODESDE;
        this.SERIE = SERIE;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.FOLIOSF"+empresa;
    }

    @Override
    public String getOrder() {
        return "TIP_DOC, FOLIODESDE, SERIE";
    }

    @Override
    public String getWhere() {
        return "TIP_DOC = '"+TIP_DOC+"' AND FOLIODESDE = "+FOLIODESDE+" AND SERIE = '"+SERIE+"'";
    }

    @Override
    public String toString() {
        return TIP_DOC+";"+FOLIODESDE+";"+SERIE;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
