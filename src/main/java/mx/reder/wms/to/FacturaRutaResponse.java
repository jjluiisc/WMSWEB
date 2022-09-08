package mx.reder.wms.to;

import com.atcloud.to.ErrorTO;
import java.util.ArrayList;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;

/**
 *
 * @author joelbecerramiranda
 */
public class FacturaRutaResponse {
    public ArrayList<OrdenSurtidoPedidoDAO> ordenessurtido;
    public ArrayList<ErrorTO> resultados;
}
