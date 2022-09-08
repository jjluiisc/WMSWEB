package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.util.Date;

/**
 *
 * @author joelbecerramiranda
 */
public class OrdenSurtidoPedidoContenedorDAO implements DatabaseRecord {
    private int idOrdenSurtidoPedidoContenedor = 0;
    public String compania = "";
    public int flsurtido = 0;
    public int partida = 0;
    public int idcontenedor = 0;
    public String codigo = "";
    public String contenedor = "";
    public String lote = "";
    public Date fecaducidad = new Date(0);
    public double surtidas = 0.0;

    public int getIdOrdenSurtidoPedidoContenedor() {
        return idOrdenSurtidoPedidoContenedor;
    }

    public void setIdOrdenSurtidoPedidoContenedor(int idOrdenSurtidoPedidoContenedor) {
        this.idOrdenSurtidoPedidoContenedor = idOrdenSurtidoPedidoContenedor;
    }
    
    public OrdenSurtidoPedidoContenedorDAO() {
    }

    public OrdenSurtidoPedidoContenedorDAO(String compania, int flsurtido, int partida, int idcontenedor) {
        this.compania = compania;
        this.flsurtido = flsurtido;
        this.partida = partida;
        this.idcontenedor = idcontenedor;
    }

    @Override
    public String getTable() {
        return "OrdenSurtidoPedidoContenedor";
    }

    @Override
    public String getOrder() {
        return "compania, flsurtido, partida, idcontenedor";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND partida = "+partida+" AND idcontenedor = "+idcontenedor;
    }

    @Override
    public String toString() {
        return compania+";"+flsurtido+";"+partida+";"+idcontenedor;
    }
}
