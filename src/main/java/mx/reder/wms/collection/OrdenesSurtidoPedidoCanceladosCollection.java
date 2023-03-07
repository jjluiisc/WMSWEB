/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import java.util.Date;

/**
 *
 * @author Luis
 */
public class OrdenesSurtidoPedidoCanceladosCollection implements CollectionRecord {
    public String compania = "";
    public String status = "";
    public Date fechapedido = null;
    public Date fechasurtido = null;
    public Date fechacancelacion = null;
    public int flsurtido = 0;
    public String pedido = "";    
    public String ruta = "";
    public String cliente = "";
    public String nombrecliente = "";
    public String vendedor = "";
    public String surtidor = "";
    public String motivocancelacion = "";
    
    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT os.compania,os.status,os.fechapedido,os.fechasurtido,os.fechacancelacion,os.flsurtido,os.pedido,os.ruta,os.cliente,os.nombrecliente,os.vendedor,os.surtidor,ISNULL(mc.descripcion,'') as motivocancelacion ")
            .append("FROM dbo.OrdenSurtidoPedido os ")
            .append("LEFT OUTER JOIN dbo.MotivoCancelacion mc ON os.motivocancelacion = mc.motivocancelacion ")
            .append("WHERE ").append(where).append(" ");
        return sql.toString();
    }

    @Override
    public String getWhere() {
        return "osp.compania = '"+compania+"' AND osp.flsurtido = "+flsurtido;
    }
}
