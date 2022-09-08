/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;

/**
 *
 * @author Luis
 */
public class OrdenesSurtidoTicketCollection implements CollectionRecord {
    public String compania = "";
    public int flsurtido = 0;
    public String contenedor = "";
    public String pedido = "";
    public String cliente = "";
    public String nombrecliente = "";
    public String ruta = "";    

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT OSP.compania, OSP.flsurtido, OSPC.contenedor, OSP.pedido, OSP.cliente, OSP.nombrecliente, OSP.ruta  FROM OrdenSurtidoPedido OSP ")
            .append("INNER JOIN OrdenSurtidoPedidoCertifica OSPC ON OSP.compania = OSPC.compania AND OSP.flsurtido = OSPC.flsurtido ")
            .append("WHERE 1 = 1 AND ").append(where).append(" ")
            .append("UNION ")
            .append("SELECT OSP.compania, OSP.flsurtido, OSPC.contenedor, OSP.pedido, OSP.cliente, OSP.nombrecliente, OSP.ruta  FROM OrdenSurtidoPedido OSP  ")
            .append("INNER JOIN OrdenSurtidoPedidoContenedor OSPC ON OSP.compania = OSPC.compania AND OSP.flsurtido = OSPC.flsurtido ")
            .append("WHERE 1 = 1 AND ").append(where).append(" ");    
        return sql.toString();
    }
    
    @Override
    public String getWhere() {
        return "OSPC.compania = '"+compania+"' AND OSPC.contenedor = '"+contenedor+"'";
    }
}