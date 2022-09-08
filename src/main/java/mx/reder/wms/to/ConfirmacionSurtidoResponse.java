package mx.reder.wms.to;

import java.util.ArrayList;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;

/**
 *
 * @author joelbecerramiranda
 */
public class ConfirmacionSurtidoResponse {
    public OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO;
    public ArrayList<OrdenSurtidoPedidoCertificaDAO> certificadas;
    public ArrayList<ContenedorSurtidoTO> contenedores;
}
