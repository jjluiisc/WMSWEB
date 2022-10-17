package mx.reder.wms.to;

import com.atcloud.to.ErrorTO;
import java.util.ArrayList;
import mx.reder.wms.dao.entity.RutaFacturaDAO;
import mx.reder.wms.dao.entity.RutaDAO;

/**
 *
 * @author joelbecerramiranda
 */
public class FacturaRutaResponse {
    public RutaDAO ruta;
    public ArrayList<RutaFacturaDAO> facturas;
    public ArrayList<ErrorTO> errores;
}
