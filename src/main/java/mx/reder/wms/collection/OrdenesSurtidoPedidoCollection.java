package mx.reder.wms.collection;

import com.atcloud.collection.engine.CollectionRecord;
import java.util.Date;

public class OrdenesSurtidoPedidoCollection implements CollectionRecord {
    public String compania = "";
    public int flsurtido = 0;
    public String pedido = "";
    public String status = "";
    public Date fechastatus = new Date(0);
    public String usuario = "";
    public String equipo = "";
    public String surtidor = "";
    public Date fechapedido = null;
    public String cliente = "";
    public String nombrecliente = "";
    public String vendedor = "";
    public String ruta = "";
    public Date fechasurtido = null;
    public Date fechainicio = null;
    public Date fechatermino = null;
    public double cantidad = 0.0;
    public double surtidas = 0.0;
    public double certificadas = 0.0;
    public double total = 0.0;
    public int detalles = 0;
    public Date fechacertificando = null;
    public Date fechaconfirmada = null;
    public Date fechafacturada = null;

    @Override
    public String getSQL() {
        return getSQL(getWhere());
    }

    @Override
    public String getSQL(String where) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT osp.* ")
            .append("FROM OrdenSurtidoPedido osp ")
            .append("WHERE ").append(where).append(" ");
        return sql.toString();
    }

    @Override
    public String getWhere() {
        return "osp.compania = '"+compania+"' AND osp.flsurtido = "+flsurtido;
    }
}
