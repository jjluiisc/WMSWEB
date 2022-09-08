package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;

/**
 *
 * @author joelbecerramiranda
 */
public class OrdenSurtidoPedidoDetalleDAO implements DatabaseRecord, java.io.Serializable {
    public String compania = "";
    public int flsurtido = 0;
    public int partida = 0;
    public String pedido = "";
    public String codigo = "";
    public String descripcion = "";
    public String ubicacion = "";
    public double cantidad = 0.0;
    public double surtidas = 0.0;
    public double certificadas = 0.0;
    public double precio = 0.0;
    public double total = 0.0;
    public double preciopublico = 0.0;
    public double iva = 0.0;

    public OrdenSurtidoPedidoDetalleDAO() {
    }

    public OrdenSurtidoPedidoDetalleDAO(String compania, int flsurtido, int partida) {
        this.compania = compania;
        this.flsurtido = flsurtido;
        this.partida = partida;
    }

    @Override
    public String getTable() {
        return "OrdenSurtidoPedidoDetalle";
    }

    @Override
    public String getOrder() {
        return "compania, flsurtido, partida";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida;
    }

    @Override
    public String toString() {
        return compania+";"+flsurtido+";"+partida+";"+codigo;
    }
}
