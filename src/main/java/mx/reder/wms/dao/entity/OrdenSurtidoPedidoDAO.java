package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.util.Date;

/**
 *
 * @author joelbecerramiranda
 */
public class OrdenSurtidoPedidoDAO implements DatabaseRecord, java.io.Serializable {
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
    public Date fechacancelacion = null;
    public String motivocancelacion = "";
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public OrdenSurtidoPedidoDAO() {
    }

    public OrdenSurtidoPedidoDAO(String compania, int flsurtido) {
        this.compania = compania;
        this.flsurtido = flsurtido;
    }

    @Override
    public String getTable() {
        return "OrdenSurtidoPedido";
    }

    @Override
    public String getOrder() {
        return "compania, flsurtido";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido;
    }

    @Override
    public String toString() {
        return compania+";"+flsurtido;
    }
}
