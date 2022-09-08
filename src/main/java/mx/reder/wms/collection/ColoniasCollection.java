package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;

/**
 *
 * @author joelbecerram
 */
public class ColoniasCollection implements CollectionRecord {
    public String colonia = "";
    public String dscolonia = "";
    public String poblacion = "";
    public String dspoblacion = "";
    public String entidadfederativa = "";
    public String dsentidadfederativa = "";
    public String pais = "";
    public String dspais = "";
    public String nombre = "";
    public String codigopostal = "";

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        return "SELECT c.colonia, c.nombre AS dscolonia, c.poblacion, c.entidadfederativa, c.pais, c.nombre, c.codigopostal, "
            +"pb.nombre AS dspoblacion, e.nombre AS dsentidadfederativa, p.nombre AS dspais "
            +"FROM Colonia c LEFT JOIN Poblacion pb ON c.poblacion = pb.poblacion "
            +"LEFT JOIN EntidadFederativa e ON c.entidadfederativa = e.entidadfederativa  "
            +"LEFT JOIN Pais p ON c.pais = p.pais  "
            +"WHERE "+where;
    }

    @Override
    public String getWhere() {
        return "(c.nombre LIKE '%"+colonia+"%' OR c.codigopostal LIKE '%"+colonia+"%')";
    }
}
