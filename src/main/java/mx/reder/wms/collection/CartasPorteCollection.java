package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import java.util.Date;

public class CartasPorteCollection implements CollectionRecord {
    public String compania = "";
    public int idcartaporte = 0;
    public Date fechatimbre = new Date(0);
    public String uuid = "";
    public String factura = "";
    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CPCFDI.fechatimbre, CPF.idcartaporte, CPCFDI.uuid, CPF.factura, CPF.compania ")
            .append("FROM   CartaPorteFactura CPF INNER JOIN CartaPorteCfdi CPCFDI ON CPF.idcartaporte = CPCFDI.id ")
            .append("WHERE ").append(where).append(" ");
        return sql.toString();
    }


       
    @Override
    public String getWhere() {
        return "CPF.compania = '"+compania+"' AND CPF.idcartaporte = "+idcartaporte;
    }
}
