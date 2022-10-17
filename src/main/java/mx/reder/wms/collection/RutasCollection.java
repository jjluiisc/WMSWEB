package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import java.util.Date;

public class RutasCollection implements CollectionRecord {
    public int id = 0;
    public String compania = "";
    public String ruta = "";
    public String status = "";
    public Date fechastatus = new Date(0);
    public String usuario = "";
    public Date fechacreacion = null;
    public Date fechacierre = null;

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.* ")
            .append("FROM Ruta r ")
            .append("WHERE ").append(where).append(" ");
        return sql.toString();
    }

    @Override
    public String getWhere() {
        return "r.compania = '"+compania+"' AND r.id = "+id;
    }
}
