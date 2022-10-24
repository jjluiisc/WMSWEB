package mx.reder.wms.reports;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.CommonServices;
import com.atcloud.web.WebException;
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
import mx.reder.wms.dao.entity.RutaCfdiDAO;
import mx.reder.wms.dao.entity.RutaFacturaDAO;
import mx.reder.wms.to.ASPELFacturaDetalleTO;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class ComprobantePDF {
    static Logger log = Logger.getLogger(ComprobantePDF.class.getName());

    private CommonServices cs = new CommonServices();
    private String path = null;

    public void setContextPath(String path) {
        this.path = path;
    }

    public byte[] pdf(DatabaseServices ds, RutaCfdiDAO rutaCfdiDAO, RutaFacturaDAO rutaFacturaDAO, OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO,
            ASPELPedidoDAO aspelPedidoDAO, ASPELVendedorDAO aspelVendedorDAO, ASPELFacturaDAO aspelFacturaDAO, ASPELClienteDAO aspelClienteDAO,
            ASPELInformacionEnvioDAO aspelInformacionEnvioDAO, ArrayList<ASPELFacturaDetalleTO> detalles) throws Exception {
        String xmlText = rutaCfdiDAO.xml;
        log.debug(xmlText);

        String version = cs.getStringBetween(xmlText, "Version=\"", "\"");
        log.debug("Version CFDI = "+version);

        ComprobanteDocument cd = null;
        if (version.compareTo("4.0")==0) {
            cd = ComprobanteDocument.Factory.parse(xmlText);
        }
        else {
            throw new WebException("Version de CFDI ["+version+"] no identificada.");
        }

        TimbreFiscalDigitalDocument tfd = null;
        CartaPorteDocument cpd = null;
        PagosDocument pd = null;

        //
        // Leo los valores del timbre del comprobante
        //
        int begin = xmlText.indexOf("<tfd:TimbreFiscalDigital");
        int end = xmlText.indexOf("</cfdi:Complemento>", begin);
        String timbreFiscal = xmlText.substring(begin, end);

        tfd = TimbreFiscalDigitalDocument.Factory.parse(timbreFiscal);
        log.debug("UUID = "+tfd.getTimbreFiscalDigital().getUUID());

        boolean cartaporte = xmlText.contains("cartaporte20");
        if (cartaporte) {
            begin = xmlText.indexOf("<cartaporte20:CartaPorte");
            end = xmlText.indexOf("</cartaporte20:CartaPorte>", begin);
            String cartaporteXML = xmlText.substring(begin, end+26);
            cartaporteXML = cartaporteXML.replace("Version=", "xmlns:cartaporte20=\"http://www.sat.gob.mx/CartaPorte20\" Version=");

            cpd = CartaPorteDocument.Factory.parse(cartaporteXML);
            log.debug("Version Carta Porte = "+cpd.getCartaPorte().getVersion());
        }

        boolean pagos = xmlText.contains("pago20:Pagos");
        if (pagos) {
            begin = xmlText.indexOf("<pago20:Pagos");
            end = xmlText.indexOf("</pago20:Pagos>", begin);
            String pagosXML = xmlText.substring(begin, end+15);
            pagosXML = pagosXML.replace("Version=", "xmlns:pago20=\"http://www.sat.gob.mx/Pagos20\" Version=");

            pd = PagosDocument.Factory.parse(pagosXML);
            log.debug("Version Pagos = "+pd.getPagos().getVersion());
        }

        boolean pagos10 = xmlText.contains("pago10:Pagos");
        if (pagos10) {
            begin = xmlText.indexOf("<pago10:Pagos");
            end = xmlText.indexOf("</pago10:Pagos>", begin);
            String pagosXML = xmlText.substring(begin, end+15);
            pagosXML = pagosXML.replace("Version=", "xmlns:pago20=\"http://www.sat.gob.mx/Pagos20\" Version=");
            pagosXML = pagosXML.replaceAll("pago10", "pago20");
            pagosXML = pagosXML.replaceAll("TipoCambioDR", "EquivalenciaDR");

            pd = PagosDocument.Factory.parse(pagosXML);
            log.debug("Version Pagos = "+pd.getPagos().getVersion());
        }

        /*
        *
        * Funciona la Tecnica, pero falla con los Comprobantes 3.3 porque al 'parsearlos', no se agregan los
        * complementos de pagos y carta porte que existen en los comprobantes 3.3
        *
        * Por eso es mejor la tecnica 'old-fashion' del indexOf
        *
        Complemento[] complementoArray = cd.getComprobante().getComplementoArray();
        if (complementoArray!=null) {
            for (Complemento complemento : complementoArray) {
                for (Node node = complemento.getDomNode().getFirstChild(); node!=null; node = node.getNextSibling()) {
                    String namespace = node.getNamespaceURI();
                    log.debug("Complemento Namespace ["+namespace+"]");

                    if (namespace.endsWith("TimbreFiscalDigital"))
                        tfd = TimbreFiscalDigitalDocument.Factory.parse(node);
                    else if (namespace.endsWith("CartaPorte20"))
                        cpd = CartaPorteDocument.Factory.parse(node);
                    else if (namespace.endsWith("Pagos20"))
                        pd = PagosDocument.Factory.parse(node);
                }
            }
        }
        *
        */

        //
        //
        //
        String fontPath = path==null ? null : path+"assets/fonts/Tahoma.ttf";

        log.debug("Generando el PDF ... ");
        PDFFactory pdfFactory = new PDFFactoryFacturaImp();
        pdfFactory.setup(fontPath);
        byte[] pdf = pdfFactory.genera(ds, cd, tfd, cpd, pd, rutaCfdiDAO.cadenaoriginal, rutaCfdiDAO.qr,
                rutaFacturaDAO, ordenSurtidoPedidoDAO, aspelPedidoDAO, aspelVendedorDAO, aspelFacturaDAO, aspelClienteDAO, aspelInformacionEnvioDAO,
                detalles);
        pdfFactory.terminate();

        return pdf;
    }

    public static String xmlEntities(String value) {
        if (value==null)
            return null;
        value = value.replaceAll("&apos;", "'");
        value = value.replaceAll("&amp;", "&");
        return value;
    }
}
