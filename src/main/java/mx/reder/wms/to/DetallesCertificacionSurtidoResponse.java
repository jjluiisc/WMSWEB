package mx.reder.wms.to;

import java.util.ArrayList;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoLotesDAO;

/**
 *
 * @author joelbecerramiranda
 */
public class DetallesCertificacionSurtidoResponse {
    public OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO;
    public ArrayList<OrdenSurtidoPedidoLotesDAO> lotes;
    public ArrayList<OrdenSurtidoPedidoCertificaDAO> detalles;
}
