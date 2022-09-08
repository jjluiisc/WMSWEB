package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;

/**
 *
 * @author joelbecerram
 */
public class EquiposSurtidoCollection implements CollectionRecord {
    public String compania = "";
    public String equipo = "";
    public long cuantos = 0;

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT s.compania, s.equipo, COUNT(*) AS cuantos ")
            .append("FROM Surtidor s ")
            .append("WHERE ").append(where).append(" ")
            .append("GROUP BY s.compania, s.equipo ")
            .append("ORDER BY s.compania, s.equipo");
        return sql.toString();
    }

    @Override
    public String getWhere() {
        return "s.compania = '"+compania;
    }
}
