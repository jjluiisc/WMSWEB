package mx.reder.wms.reports;

import com.atcloud.dao.engine.DatabaseServices;
import java.util.ArrayList;
import mx.gob.sat.cartaPorte20.CartaPorteDocument;
import mx.gob.sat.cfd.x4.ComprobanteDocument;
import mx.gob.sat.pagos20.PagosDocument;
import mx.gob.sat.timbreFiscalDigital.TimbreFiscalDigitalDocument;
import mx.reder.wms.dao.entity.ASPELClienteDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDAO;
import mx.reder.wms.dao.entity.ASPELInformacionEnvioDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDAO;
import mx.reder.wms.dao.entity.ASPELVendedorDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.RutaFacturaDAO;
import mx.reder.wms.to.ASPELFacturaDetalleTO;

public interface PDFFactory {
    public void setup(String fontPath, String logoPath) throws Exception;
    public byte[] genera(DatabaseServices ds, ComprobanteDocument cd, TimbreFiscalDigitalDocument tfd, CartaPorteDocument cpd, PagosDocument pd,
            String cadenaOriginal, String qr, String indicador, RutaFacturaDAO rutaFacturaDAO, OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO,
            ASPELPedidoDAO aspelPedidoDAO, ASPELVendedorDAO aspelVendedorDAO, ASPELFacturaDAO aspelFacturaDAO, ASPELClienteDAO aspelClienteDAO,
            ASPELInformacionEnvioDAO aspelInformacionEnvioDAO, ArrayList<ASPELFacturaDetalleTO> detallesFactura) throws Exception;
    public void terminate();
}
