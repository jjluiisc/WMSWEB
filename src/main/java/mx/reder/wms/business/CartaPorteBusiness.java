package mx.reder.wms.business;

import com.atcloud.commerce.entity.MailBox;
import com.atcloud.commerce.services.EmailServices;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.CommonServices;
import com.atcloud.util.F;
import com.atcloud.util.Fecha;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import mx.gob.sat.cartaPorte20.CartaPorteDocument;
import mx.gob.sat.cfd.x4.ComprobanteDocument;
import mx.gob.sat.pagos20.PagosDocument;
import mx.gob.sat.timbreFiscalDigital.TimbreFiscalDigitalDocument;
import mx.reder.wms.cfdi.RespuestaComprobante;
import mx.reder.wms.cfdi.TimbradoSWCFDImp;
import mx.reder.wms.cfdi.entity.ComprobanteCFD;
import mx.reder.wms.cfdi.entity.ConceptoCFD;
import mx.reder.wms.cfdi.entity.TimbradoCFD;
import mx.reder.wms.cfdi.imp.CartaPorteAutotransporteCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteAutotransporteRemolqueCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteAutotransporteSeguroCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteCantidadTransportaCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteDomicilioCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteFiguraTransporteCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteMercanciaCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteTipoFiguraTransporteCFDImp;
import mx.reder.wms.cfdi.imp.CartaPorteUbicacionCFDImp;
import mx.reder.wms.cfdi.imp.ComprobanteCFDImp;
import mx.reder.wms.cfdi.imp.ConceptoImp;
import mx.reder.wms.cfdi.imp.DocumentoImp;
import mx.reder.wms.cfdi.imp.ExtraccionImp;
import mx.reder.wms.cfdi.imp.ReceptorImp;
import mx.reder.wms.dao.GenericDAO;
import mx.reder.wms.dao.entity.ASPELClienteCamposLibresDAO;
import mx.reder.wms.dao.entity.ASPELClienteDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDAO;
import mx.reder.wms.dao.entity.ASPELInformacionEnvioDAO;
import mx.reder.wms.dao.entity.ASPELOperadorDAO;
import mx.reder.wms.dao.entity.ASPELProductoDAO;
import mx.reder.wms.dao.entity.CertificadoSelloDigitalDAO;
import mx.reder.wms.dao.entity.ClaveProdServCPDAO;
import mx.reder.wms.dao.entity.CodigoPostalSATDAO;
import mx.reder.wms.dao.entity.ColoniaSATDAO;
import mx.reder.wms.dao.entity.CompaniaDAO;
import mx.reder.wms.dao.entity.ConfigAutotransporteSATDAO;
import mx.reder.wms.dao.entity.DireccionDAO;
import mx.reder.wms.dao.entity.CartaPorteCfdiDAO;
import mx.reder.wms.dao.entity.CartaPorteFacturaDAO;
import mx.reder.wms.dao.entity.RutaCfdiDAO;
import mx.reder.wms.dao.entity.RutaFacturaDAO;
import mx.reder.wms.reports.PDFFactory;
import mx.reder.wms.reports.PDFFactoryImp;
import mx.reder.wms.to.ASPELFacturaDetalleTO;
import mx.reder.wms.to.AutotransporteTO;
import mx.reder.wms.to.CartaPorteFacturaTO;
import mx.reder.wms.to.CorreoFacturaTO;
import mx.reder.wms.to.RutaFacturaCartaPorteTO;
import mx.reder.wms.to.TipoFiguraTransporteTO;
import mx.reder.wms.util.Configuracion;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class CartaPorteBusiness {
    static Logger log = Logger.getLogger(CartaPorteBusiness.class);

    private DatabaseServices ds;
    private DatabaseServices dsA;
    private CommonServices cs = new CommonServices();

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }

    public void setDatabaseAspelServices(DatabaseServices dsA) {
        this.dsA = dsA;
    }

    public ArrayList<CartaPorteFacturaTO> cartaPorteFacturas(String compania, String usuario, String idruta, String factura) throws Exception {
        CompaniaDAO companiaDAO = new CompaniaDAO();
        companiaDAO.compania = compania;
        if (!ds.exists(companiaDAO))
            throw new WebException("No existe esta Compania ["+companiaDAO+"]");

        if (idruta==null&&factura==null)
            throw new WebException("No se especifico ni ruta ni factura");

        StringBuilder where = new StringBuilder();
        where.append("compania = '").append(compania).append("' ");
        if (idruta!=null)
            where.append("AND idruta = ").append(idruta).append(" ");
        if (factura!=null)
            where.append("AND factura = '").append(factura).append("' ");

        ArrayList<CartaPorteFacturaTO> facturas = new ArrayList<>();
        ArrayList<RutaFacturaDAO> rutaFacturas = ds.select(new RutaFacturaDAO(), where.toString(), "parada, flsurtido");
        for (RutaFacturaDAO rutaFacturaDAO : rutaFacturas) {
            CartaPorteFacturaDAO cartaPorteFacturaDAO = new CartaPorteFacturaDAO();
            cartaPorteFacturaDAO.compania = rutaFacturaDAO.compania;
            cartaPorteFacturaDAO.flsurtido = rutaFacturaDAO.flsurtido;

            CartaPorteFacturaTO cartaPorteFacturaTO = new CartaPorteFacturaTO();

            //
            // Si existe la factura no se va agregar al grid, no tiene caso ir por los demas datos.
            //
            if (ds.exists(cartaPorteFacturaDAO)) {
                Reflector.copyAllFields(cartaPorteFacturaDAO, cartaPorteFacturaTO);
                facturas.add(cartaPorteFacturaTO);
                continue;
            }

            Reflector.copyAllFields(rutaFacturaDAO, cartaPorteFacturaTO);

            ASPELFacturaDAO aspelFacturaDAO = new ASPELFacturaDAO();
            aspelFacturaDAO.setEmpresa(rutaFacturaDAO.compania);
            aspelFacturaDAO.CVE_DOC = rutaFacturaDAO.factura;
            if (!dsA.exists(aspelFacturaDAO))
                throw new WebException("No existe esta Factura ["+aspelFacturaDAO+"]");

            log.debug(Reflector.toStringAllFields(aspelFacturaDAO));

            ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
            aspelClienteDAO.setEmpresa(rutaFacturaDAO.compania);
            aspelClienteDAO.CLAVE = aspelFacturaDAO.CVE_CLPV;
            if (!dsA.exists(aspelClienteDAO))
                throw new WebException("No existe este Cliente ["+aspelClienteDAO+"]");

            log.debug(Reflector.toStringAllFields(aspelClienteDAO));

            ASPELClienteCamposLibresDAO aspelClienteCamposLibresDAO = new ASPELClienteCamposLibresDAO();
            aspelClienteCamposLibresDAO.setEmpresa(rutaFacturaDAO.compania);
            aspelClienteCamposLibresDAO.CVE_CLIE = aspelFacturaDAO.CVE_CLPV;
            if (!dsA.exists(aspelClienteCamposLibresDAO))
                throw new WebException("No existe este Cliente Campos Libres ["+aspelClienteCamposLibresDAO+"]");

            log.debug(Reflector.toStringAllFields(aspelClienteCamposLibresDAO));

            cartaPorteFacturaTO.distancia = aspelClienteCamposLibresDAO.CAMPLIB12==null ? 0.0 : aspelClienteCamposLibresDAO.CAMPLIB12;

            facturas.add(cartaPorteFacturaTO);
        }

        return facturas;
    }

    public CartaPorteCfdiDAO cartaPorte(String compania, String usuario, String figuraTransporte, String autotransporte, ArrayList<RutaFacturaCartaPorteTO> facturas) throws Exception {
        CompaniaDAO companiaDAO = new CompaniaDAO();
        companiaDAO.compania = compania;
        if (!ds.exists(companiaDAO))
            throw new WebException("No existe esta Compania ["+companiaDAO+"]");

        DireccionDAO direccionDAO = new DireccionDAO();
        direccionDAO.direccion = companiaDAO.direccion;
        if (!ds.exists(direccionDAO))
            throw new WebException("No existe esta Direccion ["+direccionDAO+"]");

        File dir = new File(Configuracion.getInstance().getProperty("ruta.pdf"));
        if (!dir.exists())
            throw new WebException("No existe este directorio ["+dir.getAbsolutePath()+"]");

        //
        //
        //
        ASPELOperadorDAO aspelOperadorDAO = new ASPELOperadorDAO();
        aspelOperadorDAO.setEmpresa(compania);
        aspelOperadorDAO.CVE_OPE = figuraTransporte;

        if (!dsA.exists(aspelOperadorDAO))
            throw new WebException("No existe esta Figura de Transporte ["+aspelOperadorDAO+"]");

        TipoFiguraTransporteTO tipoFiguraTransporteTO = new TipoFiguraTransporteTO();
        tipoFiguraTransporteTO.fromXML(cs, aspelOperadorDAO);

        log.debug(Reflector.toStringAllFields(tipoFiguraTransporteTO));

        aspelOperadorDAO = new ASPELOperadorDAO();
        aspelOperadorDAO.setEmpresa(compania);
        aspelOperadorDAO.CVE_OPE = autotransporte;

        if (!dsA.exists(aspelOperadorDAO))
            throw new WebException("No existe este Autotransporte ["+aspelOperadorDAO+"]");

        AutotransporteTO autotransporteTO = new AutotransporteTO();
        autotransporteTO.fromXML(cs, aspelOperadorDAO);

        if (autotransporteTO.polizamedambiente==null||autotransporteTO.polizamedambiente.isEmpty()) {
            autotransporteTO.polizamedambiente = autotransporteTO.polizarespcivil;
            autotransporteTO.aseguramedambiente = autotransporteTO.asegurarespcivil;
        }
        if (autotransporteTO.polizacarga==null||autotransporteTO.polizacarga.isEmpty()) {
            autotransporteTO.polizacarga = autotransporteTO.polizarespcivil;
            autotransporteTO.aseguracarga = autotransporteTO.asegurarespcivil;
        }

        log.debug(Reflector.toStringAllFields(autotransporteTO));

       //
        //
        //
        ArrayList ubicaciones = new ArrayList<>();

        CartaPorteUbicacionCFDImp cartaPorteUbicacionOrigenCFD = new CartaPorteUbicacionCFDImp();
        cartaPorteUbicacionOrigenCFD.tipoUbicacion = "Origen";
        cartaPorteUbicacionOrigenCFD.idUbicacion = "OR101010";
        cartaPorteUbicacionOrigenCFD.rfcRemitenteDestinatario = companiaDAO.rfc;
        cartaPorteUbicacionOrigenCFD.nombreRFC = companiaDAO.razonsocial;
        cartaPorteUbicacionOrigenCFD.fechaHoraSalidaLlegada = new Date();
        cartaPorteUbicacionOrigenCFD.distanciaRecorrida = null;
        cartaPorteUbicacionOrigenCFD.tipoEstacion = "01";
        cartaPorteUbicacionOrigenCFD.domicilio = new CartaPorteDomicilioCFDImp();
        cartaPorteUbicacionOrigenCFD.domicilio.calle = direccionDAO.calle;
        cartaPorteUbicacionOrigenCFD.domicilio.noExterior = direccionDAO.noexterior;
        cartaPorteUbicacionOrigenCFD.domicilio.noInterior = direccionDAO.nointerior;
        cartaPorteUbicacionOrigenCFD.domicilio.colonia = direccionDAO.colonia;
        cartaPorteUbicacionOrigenCFD.domicilio.municipio = direccionDAO.poblacion;
        cartaPorteUbicacionOrigenCFD.domicilio.estado = direccionDAO.entidadfederativa;
        cartaPorteUbicacionOrigenCFD.domicilio.pais = direccionDAO.pais;
        cartaPorteUbicacionOrigenCFD.domicilio.codigoPostal = direccionDAO.codigopostal;

        CodigoPostalSATDAO codigoPostalSATDAO = new CodigoPostalSATDAO(cartaPorteUbicacionOrigenCFD.domicilio.codigoPostal);
        if (!ds.exists(codigoPostalSATDAO))
            throw new WebException("No existe Codigo Postal para este codigo postal ["+cartaPorteUbicacionOrigenCFD.domicilio.codigoPostal+"]");
        cartaPorteUbicacionOrigenCFD.domicilio.localidad = codigoPostalSATDAO.localidad;
        cartaPorteUbicacionOrigenCFD.domicilio.municipio = codigoPostalSATDAO.municipio;
        cartaPorteUbicacionOrigenCFD.domicilio.estado = codigoPostalSATDAO.estado;
        cartaPorteUbicacionOrigenCFD.domicilio.pais = "MEX";
        ColoniaSATDAO coloniaSATDAO = (ColoniaSATDAO)ds.first(new ColoniaSATDAO(), "codigopostal = '"+cartaPorteUbicacionOrigenCFD.domicilio.codigoPostal+"'");
        if (coloniaSATDAO==null)
            throw new WebException("No existe Colonia para este codigo postal ["+cartaPorteUbicacionOrigenCFD.domicilio.codigoPostal+"]");
        cartaPorteUbicacionOrigenCFD.domicilio.colonia = coloniaSATDAO.colonia;

        ubicaciones.add(cartaPorteUbicacionOrigenCFD);

        //
        //
        //
        DocumentoImp documentoCFD = new DocumentoImp();
        documentoCFD.serie = "CP";
        documentoCFD.folio = F.f(GenericDAO.obtenerSiguienteFolio(ds, compania, "flcartaporte"), 10, F.ZF);
        documentoCFD.fecha = new Date();
        documentoCFD.tipoComprobante = "T";
        documentoCFD.moneda = "XXX";
        documentoCFD.exportacion = "01";

        // La clave en el RFC del receptor del CFDI debe ser la misma que la registrada para el emisor.
        ReceptorImp receptorCFD = new ReceptorImp();
        receptorCFD.nombre = companiaDAO.razonsocial;
        receptorCFD.rfc = companiaDAO.rfc;
        receptorCFD.regimenFiscal = companiaDAO.regimenfiscal;
        receptorCFD.usoCFDI = "S01";
        receptorCFD.formaDePago = null;
        receptorCFD.metodoDePago = null;
        receptorCFD.numCtaPago = null;
        receptorCFD.calle = direccionDAO.calle;
        receptorCFD.noExterior = direccionDAO.noexterior;
        receptorCFD.noInterior = direccionDAO.nointerior;
        receptorCFD.colonia = direccionDAO.colonia;
        receptorCFD.municipio = direccionDAO.poblacion;
        receptorCFD.estado = direccionDAO.entidadfederativa;
        receptorCFD.pais = direccionDAO.pais;
        receptorCFD.codigoPostal = direccionDAO.codigopostal;

        int countFacturas = 0;
        HashMap<String, ConceptoImp> detallesH = new HashMap<>();
        HashMap<String, ArrayList<CartaPorteCantidadTransportaCFDImp>> detallesDestinos = new HashMap<>();
        ArrayList<CartaPorteFacturaDAO> cartaPorteFacturas = new ArrayList<>();
        ArrayList<CorreoFacturaTO> correos = new ArrayList<>();

        for (RutaFacturaCartaPorteTO factura : facturas) {
            RutaFacturaDAO rutaFacturaDAO = new RutaFacturaDAO();
            rutaFacturaDAO.compania = factura.compania;
            rutaFacturaDAO.flsurtido = factura.flsurtido;
            if (!ds.exists(rutaFacturaDAO))
                throw new WebException("No existe esta RutaFactura ["+rutaFacturaDAO+"]");

            countFacturas ++;

            ASPELFacturaDAO aspelFacturaDAO = new ASPELFacturaDAO();
            aspelFacturaDAO.setEmpresa(rutaFacturaDAO.compania);
            aspelFacturaDAO.CVE_DOC = rutaFacturaDAO.factura;
            if (!dsA.exists(aspelFacturaDAO))
                throw new WebException("No existe esta Factura ["+aspelFacturaDAO+"]");

            log.debug(Reflector.toStringAllFields(aspelFacturaDAO));

            ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
            aspelClienteDAO.setEmpresa(rutaFacturaDAO.compania);
            aspelClienteDAO.CLAVE = aspelFacturaDAO.CVE_CLPV;
            if (!dsA.exists(aspelClienteDAO))
                throw new WebException("No existe este Cliente ["+aspelClienteDAO+"]");

            log.debug(Reflector.toStringAllFields(aspelClienteDAO));

            // Arreglo el Metodo de Pago
            if (aspelClienteDAO.METODODEPAGO==null||aspelClienteDAO.METODODEPAGO.matches("\\d+"))
                aspelClienteDAO.METODODEPAGO = "PPD";

            ASPELInformacionEnvioDAO aspelInformacionEnvioDAO = new ASPELInformacionEnvioDAO();
            aspelInformacionEnvioDAO.setEmpresa(rutaFacturaDAO.compania);
            aspelInformacionEnvioDAO.CVE_INFO = aspelFacturaDAO.DAT_ENVIO;
            if (!dsA.exists(aspelInformacionEnvioDAO)) {
                aspelInformacionEnvioDAO.CALLE = aspelClienteDAO.CALLE;
                aspelInformacionEnvioDAO.NUMEXT = aspelClienteDAO.NUMEXT;
                aspelInformacionEnvioDAO.NUMINT = aspelClienteDAO.NUMINT;
                aspelInformacionEnvioDAO.CODIGO = aspelClienteDAO.CODIGO;
                ; //throw new WebException("No existe la Direccion de Envio ["+aspelInformacionEnvioDAO+"]");
            }

            log.debug(Reflector.toStringAllFields(aspelInformacionEnvioDAO));

            //
            //
            //
            CartaPorteUbicacionCFDImp cartaPorteUbicacionDestinoCFD = new CartaPorteUbicacionCFDImp();
            cartaPorteUbicacionDestinoCFD.tipoUbicacion = "Destino";
            cartaPorteUbicacionDestinoCFD.idUbicacion = "DE"+F.f(countFacturas, 6, F.ZF);
            cartaPorteUbicacionDestinoCFD.rfcRemitenteDestinatario = aspelClienteDAO.RFC;
            cartaPorteUbicacionDestinoCFD.nombreRFC = aspelClienteDAO.NOMBRE;
            cartaPorteUbicacionDestinoCFD.fechaHoraSalidaLlegada = factura.fechahorallegada;
            cartaPorteUbicacionDestinoCFD.distanciaRecorrida = factura.distanciarecorrida;
            cartaPorteUbicacionDestinoCFD.tipoEstacion = "01";
            cartaPorteUbicacionDestinoCFD.domicilio = new CartaPorteDomicilioCFDImp();
            cartaPorteUbicacionDestinoCFD.domicilio.calle = aspelInformacionEnvioDAO.CALLE;
            cartaPorteUbicacionDestinoCFD.domicilio.noExterior = aspelInformacionEnvioDAO.NUMEXT;
            cartaPorteUbicacionDestinoCFD.domicilio.noInterior = aspelInformacionEnvioDAO.NUMINT;
            cartaPorteUbicacionDestinoCFD.domicilio.codigoPostal = aspelInformacionEnvioDAO.CODIGO;

            codigoPostalSATDAO = new CodigoPostalSATDAO(cartaPorteUbicacionDestinoCFD.domicilio.codigoPostal);
            if (!ds.exists(codigoPostalSATDAO))
                throw new WebException("No existe Codigo Postal para este codigo postal ["+cartaPorteUbicacionDestinoCFD.domicilio.codigoPostal+"]");
            cartaPorteUbicacionDestinoCFD.domicilio.localidad = codigoPostalSATDAO.localidad;
            cartaPorteUbicacionDestinoCFD.domicilio.municipio = codigoPostalSATDAO.municipio;
            cartaPorteUbicacionDestinoCFD.domicilio.estado = codigoPostalSATDAO.estado;
            cartaPorteUbicacionDestinoCFD.domicilio.pais = "MEX";
            coloniaSATDAO = (ColoniaSATDAO)ds.first(new ColoniaSATDAO(), "codigopostal = '"+cartaPorteUbicacionDestinoCFD.domicilio.codigoPostal+"'");
            if (coloniaSATDAO==null)
                throw new WebException("No existe Colonia para este codigo postal ["+cartaPorteUbicacionDestinoCFD.domicilio.codigoPostal+"]");
            cartaPorteUbicacionDestinoCFD.domicilio.colonia = coloniaSATDAO.colonia;

            ubicaciones.add(cartaPorteUbicacionDestinoCFD);

            //
            //
            //
            ArrayList<ASPELFacturaDetalleTO> detallesFactura = dsA.collection(new ASPELFacturaDetalleTO(),
                    "SELECT pf.CVE_DOC, pf.NUM_PAR, pf.CVE_ART, pf.CANT, pf.PXS, pf.PREC, pf.COST, pf.IMPU1, pf.IMPU2, pf.IMPU3, pf.IMPU4,"
                    +"pf.IMP1APLA, pf.IMP2APLA, pf.IMP3APLA, pf.IMP4APLA, pf.TOTIMP1, pf.TOTIMP2, pf.TOTIMP3, pf.TOTIMP4, pf.DESC1, pf.DESC2, pf.DESC3,"
                    +"pf.COMI, pf.APAR, pf.ACT_INV, pf.NUM_ALM, pf.POLIT_APLI, pf.TIP_CAM, pf.UNI_VENTA, pf.TIPO_PROD, pf.CVE_OBS, pf.REG_SERIE, pf.E_LTPD,"
                    +"pf.TIPO_ELEM, pf.NUM_MOV, pf.TOT_PARTIDA, pf.IMPRIMIR, pf.MAN_IEPS, pf.APL_MAN_IMP, pf.CUOTA_IEPS, pf.APL_MAN_IEPS, pf.MTO_PORC,"
                    +"pf.MTO_CUOTA, pf.CVE_ESQ, pf.DESCR_ART, pf.UUID, pf.VERSION_SINC, p.CVE_PRODSERV, p.CVE_UNIDAD, p.DESCR, p.UNI_MED, "
                    +"COALESCE(pl.CAMPLIB5, 0) AS PREPUB, pl.CAMPLIB7 AS SUSTANCIAACTIVA "
                    +"FROM PAR_FACTF"+rutaFacturaDAO.compania+" pf LEFT JOIN INVE"+rutaFacturaDAO.compania+" p ON pf.CVE_ART = p.CVE_ART "
                    +"LEFT JOIN INVE_CLIB"+rutaFacturaDAO.compania+" pl ON pf.CVE_ART = pl.CVE_PROD "
                    +"WHERE pf.CVE_DOC = '"+rutaFacturaDAO.factura+"'");

            for (ASPELFacturaDetalleTO detalle : detallesFactura) {
                //
                //
                //
                ArrayList<CartaPorteCantidadTransportaCFDImp> destinos = detallesDestinos.get(detalle.getSKU());
                if (destinos==null)
                    destinos = new ArrayList<>();

                CartaPorteCantidadTransportaCFDImp cartaPorteCantidadTransportaCFDImp = new CartaPorteCantidadTransportaCFDImp();
                cartaPorteCantidadTransportaCFDImp.cantidad = detalle.getCantidad();
                cartaPorteCantidadTransportaCFDImp.idOrigen = "OR101010";
                cartaPorteCantidadTransportaCFDImp.idDestino = cartaPorteUbicacionDestinoCFD.idUbicacion;
                destinos.add(cartaPorteCantidadTransportaCFDImp);

                detallesDestinos.put(detalle.getSKU(), destinos);

                //
                //
                //
                ConceptoImp conceptoImp = detallesH.get(detalle.getSKU());

                if (conceptoImp!=null) {
                    conceptoImp.cantidad += detalle.CANT;
                } else {
                    conceptoImp = new ConceptoImp();
                    conceptoImp.claveUnidad = detalle.getClaveUnidad().toString();
                    conceptoImp.claveProductoServicio = detalle.getClaveProductoServicio();
                    conceptoImp.unidadMedida = detalle.getUnidad();
                    conceptoImp.sku = detalle.getSKU();
                    conceptoImp.descripcion = detalle.getDescripcion();
                    conceptoImp.cantidad = detalle.getCantidad();
                    conceptoImp.precio = 0.0;
                    conceptoImp.prIesps = 0.0;
                    conceptoImp.totIesps = 0.0;
                    conceptoImp.importe = 0.0;
                    conceptoImp.prDescuento = 0.0;
                    conceptoImp.descuento = 0.0;
                    conceptoImp.prIva = 0.0;
                    conceptoImp.totIva = 0.0;
                }

                detallesH.put(detalle.getSKU(), conceptoImp);
            }

            //
            // Agrego la Factura de la Carta Porte
            //
            CartaPorteFacturaDAO cartaPorteFacturaDAO = new CartaPorteFacturaDAO();
            Reflector.copyAllFields(rutaFacturaDAO, cartaPorteFacturaDAO);
            cartaPorteFacturas.add(cartaPorteFacturaDAO);

            //
            // Armo el Correo por cada Factura
            //
            if (rutaFacturaDAO.serie.compareTo("BG")==0
              ||rutaFacturaDAO.serie.compareTo("BO")==0
              ||rutaFacturaDAO.serie.compareTo("BX")==0
              ||rutaFacturaDAO.serie.compareTo("EFX")==0
              ||rutaFacturaDAO.serie.compareTo("EFO")==0
              ||rutaFacturaDAO.serie.compareTo("FO")==0) {

                CorreoFacturaTO correoFacturaTO = new CorreoFacturaTO();
                correoFacturaTO.email = aspelClienteDAO.EMAILPRED;
                correoFacturaTO.titulo = "Envío de Comprobante Fiscal Digital : "+aspelFacturaDAO.CVE_DOC;
                correoFacturaTO.mensaje = makeTextMensaje(companiaDAO, aspelClienteDAO, aspelFacturaDAO);

                ArrayList<RutaCfdiDAO> cfdis = ds.select(new RutaCfdiDAO(),
                        "compania = '"+rutaFacturaDAO.compania+"' AND flsurtido = "+rutaFacturaDAO.flsurtido+" AND idruta = "+rutaFacturaDAO.idruta+" AND status = 'A'");
                if (cfdis.isEmpty())
                    throw new WebException("No encontre CFDIs de esta factura ["+rutaFacturaDAO.compania+";"+rutaFacturaDAO.flsurtido+"] con estado A");
                if (cfdis.size()>1)
                    throw new WebException("Hay mas de 1 CFDIs de esta factura ["+rutaFacturaDAO.compania+";"+rutaFacturaDAO.flsurtido+"] con estado A");

                RutaCfdiDAO rutaCfdiDAO = cfdis.get(0);

                File filePDF = new File(dir, "Factura_"+rutaFacturaDAO.idruta+"_"+rutaFacturaDAO.factura+".pdf");
                log.debug("filePDF: "+filePDF.getAbsolutePath());
                if (!filePDF.exists()) {
                    if (rutaCfdiDAO.pdf!=null) {
                        try (FileOutputStream fos = new FileOutputStream(filePDF)) {
                            fos.write(rutaCfdiDAO.pdf);
                        }
                        correoFacturaTO.attachments.add(filePDF);
                    }
                } else {
                    correoFacturaTO.attachments.add(filePDF);
                }

                File fileXML = new File(dir, "Factura_"+rutaFacturaDAO.idruta+"_"+rutaFacturaDAO.factura+".xml");
                log.debug("fileXML: "+fileXML.getAbsolutePath());
                if (!fileXML.exists()) {
                    try (OutputStreamWriter ows = new OutputStreamWriter(new FileOutputStream(fileXML), "UTF-8")) {
                        ows.write(rutaCfdiDAO.xml);
                    }
                    correoFacturaTO.attachments.add(fileXML);
                } else {
                    correoFacturaTO.attachments.add(fileXML);
                }

                correos.add(correoFacturaTO);
            }
        }

        ArrayList<ConceptoCFD> detalles = new ArrayList<>();
        detalles.addAll(detallesH.values());

        //
        //
        //
        boolean hayMaterialPeligroso = false;
        // La clave registrada en el campo “Clave de producto o servicio” (ClaveProdServ) de la sección “Conceptos” del CFDI
        // debe ser la misma que se registre en el campo “Bienes Transportados” (BienesTransp) de la sección “Mercancia” del complemento Carta Porte.
        ArrayList mercancias = new ArrayList<>();

        for (ConceptoCFD conceptoImp : detalles) {

            ASPELProductoDAO aspelProductoDAO = new ASPELProductoDAO();
            aspelProductoDAO.setEmpresa(compania);
            aspelProductoDAO.CVE_ART = conceptoImp.getSKU();
            if (!ds.exists(aspelProductoDAO))
                throw new WebException("No existe este Producto ["+aspelProductoDAO+"]");

            ClaveProdServCPDAO claveProdServCPDAO = new ClaveProdServCPDAO(conceptoImp.getClaveProductoServicio());
            if (!ds.exists(claveProdServCPDAO))
                throw new WebException("No existe este Concepto ["+claveProdServCPDAO+"]");

            CartaPorteMercanciaCFDImp cartaPorteMercanciaCFD = new CartaPorteMercanciaCFDImp();
            // En este campo se deberá registrar la clave de producto de los bienes y/o mercancías que se trasladan vía Autotransporte.
            // El valor de este campo deberá contener una clave del catálogo del complemento Carta Porte c_ClaveProdServCP, publicado en el portal del SAT.
            cartaPorteMercanciaCFD.bienesTransp = conceptoImp.getClaveProductoServicio();
            cartaPorteMercanciaCFD.claveSTCC = null;
            cartaPorteMercanciaCFD.descripcion = conceptoImp.getDescripcion();
            cartaPorteMercanciaCFD.cantidad = conceptoImp.getCantidad();
            cartaPorteMercanciaCFD.claveUnidad = conceptoImp.getClaveUnidad().toString();
            cartaPorteMercanciaCFD.unidad = conceptoImp.getUnidad();
            cartaPorteMercanciaCFD.dimensiones = null;
            cartaPorteMercanciaCFD.materialPeligroso = null;
            cartaPorteMercanciaCFD.cveMaterialPeligroso = null;
            cartaPorteMercanciaCFD.embalaje = null;
            cartaPorteMercanciaCFD.descripEmbalaje = null;
            cartaPorteMercanciaCFD.pesoEnKg = aspelProductoDAO.PESO==null ? 0.0d : aspelProductoDAO.PESO;
            cartaPorteMercanciaCFD.valorMercancia = null;
            cartaPorteMercanciaCFD.moneda = null;
            cartaPorteMercanciaCFD.fraccionArancelaria = null;
            cartaPorteMercanciaCFD.uuidComercioExt = null;

            if (claveProdServCPDAO.materialpeligroso!=null
                    &&claveProdServCPDAO.materialpeligroso.compareTo("1")==0) {
                hayMaterialPeligroso = true;
                cartaPorteMercanciaCFD.materialPeligroso = "Sí";
                cartaPorteMercanciaCFD.cveMaterialPeligroso = cs.getStringBetween(aspelProductoDAO.MAT_PELI, "CveMaterialPeligroso=\"", "\"");
                cartaPorteMercanciaCFD.embalaje = cs.getStringBetween(aspelProductoDAO.MAT_PELI, "Embalaje=\"", "\"");
                cartaPorteMercanciaCFD.descripEmbalaje = null;
            }

            cartaPorteMercanciaCFD.cantidadTransporta = new ArrayList();

            ArrayList<CartaPorteCantidadTransportaCFDImp> destinos = detallesDestinos.get(conceptoImp.getSKU());
            if (destinos!=null) {
                cartaPorteMercanciaCFD.cantidadTransporta.addAll(destinos);
            }

            mercancias.add(cartaPorteMercanciaCFD);
        }

        //
        //
        //
        ConfigAutotransporteSATDAO configAutotransporteSATDAO = new ConfigAutotransporteSATDAO(autotransporteTO.configuracion);
        if (!ds.exists(configAutotransporteSATDAO))
            throw new WebException("No existe la Configuracion del Autotransporte ["+configAutotransporteSATDAO+"]");

        CartaPorteAutotransporteCFDImp cartaPorteAutotransporteCFD = new CartaPorteAutotransporteCFDImp();
        cartaPorteAutotransporteCFD.permisoSCT = autotransporteTO.tipopermiso;
        cartaPorteAutotransporteCFD.numPermisoSCT = autotransporteTO.numeropermiso;
        cartaPorteAutotransporteCFD.configVehicular = autotransporteTO.configuracion;
        cartaPorteAutotransporteCFD.placaVM = autotransporteTO.placa;
        cartaPorteAutotransporteCFD.anioModeloVM = Numero.getIntFromString(autotransporteTO.aniomodelo);

        cartaPorteAutotransporteCFD.seguros = new ArrayList<>();
        CartaPorteAutotransporteSeguroCFDImp cartaPorteAutotransporteSeguroCFD = new CartaPorteAutotransporteSeguroCFDImp();
        cartaPorteAutotransporteSeguroCFD.aseguraRespCivil = autotransporteTO.asegurarespcivil;
        cartaPorteAutotransporteSeguroCFD.polizaRespCivil = autotransporteTO.polizarespcivil;
        if (hayMaterialPeligroso) {
            cartaPorteAutotransporteSeguroCFD.aseguraMedAmbiente = autotransporteTO.aseguramedambiente;
            cartaPorteAutotransporteSeguroCFD.polizaMedAmbiente = autotransporteTO.polizamedambiente;
        }
        cartaPorteAutotransporteSeguroCFD.aseguraCarga = autotransporteTO.aseguracarga;
        cartaPorteAutotransporteSeguroCFD.polizaCarga = autotransporteTO.polizacarga;
        cartaPorteAutotransporteSeguroCFD.primaSeguro = autotransporteTO.primaseguro;
        cartaPorteAutotransporteCFD.seguros.add(cartaPorteAutotransporteSeguroCFD);
        cartaPorteAutotransporteCFD.remolques = null;

        // Si hay remolques
        //if (configAutotransporteSATDAO.remolque.compareTo("0")!=0) { // Diverza
        if (configAutotransporteSATDAO.remolque.compareTo("1")==0) { // SmarterWEB
            // Se pone CTR002=Caja con el mismo numero de Placa
            cartaPorteAutotransporteCFD.remolques = new ArrayList<>();
            CartaPorteAutotransporteRemolqueCFDImp cartaPorteAutotransporteRemolqueCFD = new CartaPorteAutotransporteRemolqueCFDImp();
            cartaPorteAutotransporteRemolqueCFD.placa = autotransporteTO.placa;
            cartaPorteAutotransporteRemolqueCFD.subTipoRem = "CTR002";
            cartaPorteAutotransporteCFD.remolques.add(cartaPorteAutotransporteRemolqueCFD);
        }

        CartaPorteFiguraTransporteCFDImp cartaPorteFiguraTransporteCFD = new CartaPorteFiguraTransporteCFDImp();
        cartaPorteFiguraTransporteCFD.tiposFigura = new ArrayList<>();

        //
        //
        //
        // TipoFigura: Operador
        CartaPorteTipoFiguraTransporteCFDImp cartaPorteTipoFiguraTransporteOperadorCFDImp = new CartaPorteTipoFiguraTransporteCFDImp();
        cartaPorteTipoFiguraTransporteOperadorCFDImp.tipoFigura = "01";
        cartaPorteTipoFiguraTransporteOperadorCFDImp.rfcFigura = tipoFiguraTransporteTO.rfc;
        cartaPorteTipoFiguraTransporteOperadorCFDImp.numLicencia = tipoFiguraTransporteTO.licencia;
        cartaPorteTipoFiguraTransporteOperadorCFDImp.nombreFigura = tipoFiguraTransporteTO.nombre;
        cartaPorteTipoFiguraTransporteOperadorCFDImp.numRegIdTribFigura = null;
        cartaPorteTipoFiguraTransporteOperadorCFDImp.pais = null; //tipoFiguraTransporteTO.residenciafiscal;
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio = new CartaPorteDomicilioCFDImp();
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.calle = cs.getStringBetween(tipoFiguraTransporteTO.direccion, "CodigoPostal=\"", "\"");
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.noExterior = cs.getStringBetween(tipoFiguraTransporteTO.direccion, "NumeroExterior=\"", "\"");
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.noInterior = cs.getStringBetween(tipoFiguraTransporteTO.direccion, "NumeroInterior=\"", "\"");
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.codigoPostal = cs.getStringBetween(tipoFiguraTransporteTO.direccion, "CodigoPostal=\"", "\"");

        coloniaSATDAO = (ColoniaSATDAO)ds.first(new ColoniaSATDAO(), "codigopostal = '"+cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.codigoPostal+"'");
        if (coloniaSATDAO==null)
            throw new WebException("TipoFiguraTransporte - No existe Codig Postal este codigo postal ["+cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.codigoPostal+"]");
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.localidad = codigoPostalSATDAO.localidad;
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.municipio = codigoPostalSATDAO.municipio;
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.estado = codigoPostalSATDAO.estado;
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.pais = "MEX";
        coloniaSATDAO = (ColoniaSATDAO)ds.first(new ColoniaSATDAO(), "codigopostal = '"+cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.codigoPostal+"'");
        if (coloniaSATDAO==null)
            throw new WebException("No existe Codigo Postal para este codigo postal ["+cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.codigoPostal+"]");
        cartaPorteTipoFiguraTransporteOperadorCFDImp.domicilio.colonia = coloniaSATDAO.colonia;

        cartaPorteFiguraTransporteCFD.tiposFigura.add(cartaPorteTipoFiguraTransporteOperadorCFDImp);

        //
        //
        //
        CartaPorteCFDImp cartaPorteImp = new CartaPorteCFDImp();
        // En este campo se deberá registrar el valor “Sí” o “No” para indicar cuando el traslado de bienes y/o mercancías sea de carácter internacional.
        // Se captura el valor “No”, lo cual significa que los campos “Entrada o Salida de Mercancías” (EntradaSalidaMerc),
        // “País de origen o destino” (PaisOrigenDestino) y “Vía de entrada o salida” (ViaEntradaSalida) no deben existir.
        cartaPorteImp.transpInternac = "No";
        cartaPorteImp.entradaSalidaMerc = null;
        cartaPorteImp.viaEntradaSalida = null;
        cartaPorteImp.ubicaciones = ubicaciones;
        cartaPorteImp.mercancias = mercancias;
        cartaPorteImp.unidadPeso = "KGM";
        cartaPorteImp.pesoBrutoTotal = null;
        cartaPorteImp.pesoNetoTotal = null;
        cartaPorteImp.autotransporte = cartaPorteAutotransporteCFD;
        cartaPorteImp.figuraTransporte = cartaPorteFiguraTransporteCFD;

        ComprobanteCFD comprobanteCFDImp = new ComprobanteCFDImp(companiaDAO, direccionDAO, documentoCFD,
            receptorCFD, direccionDAO, receptorCFD, detalles, cartaPorteImp, null);

        HashMap<String, String> propiedades = new HashMap<>();
        propiedades.put("url", Configuracion.getInstance().getProperty(companiaDAO.compania+".timbrado.url"));
        propiedades.put("usuario", Configuracion.getInstance().getProperty(companiaDAO.compania+".timbrado.usuario"));
        propiedades.put("password", Configuracion.getInstance().getProperty(companiaDAO.compania+".timbrado.password"));
        TimbradoCFD timbradoCFDImp = new TimbradoSWCFDImp(propiedades);

        String fecha = Fecha.getFechaHora(documentoCFD.getFecha());
        CertificadoSelloDigitalDAO certificadoSelloDigitalDAO = (CertificadoSelloDigitalDAO)ds.first(new CertificadoSelloDigitalDAO(),
            "compania = '"+companiaDAO.compania+"' AND fechainicial <= '"+fecha+"' AND fechafinal >= '"+fecha+"'");
        if(certificadoSelloDigitalDAO==null)
            throw new Exception("No existe CSD para la compania = '"+companiaDAO.compania+"' fecha = '"+fecha+"'");

        ExtraccionImp extraccionImp = new ExtraccionImp();
        extraccionImp.setCertificadoSelloDigital(certificadoSelloDigitalDAO);
        RespuestaComprobante respuesta = extraccionImp.getComprobante(comprobanteCFDImp, timbradoCFDImp);
        ComprobanteDocument.Comprobante comprobante = respuesta.getCd().getComprobante();
        TimbreFiscalDigitalDocument.TimbreFiscalDigital timbreFiscalDigital = respuesta.getTfd().getTimbreFiscalDigital();
        String xml = new String(respuesta.getXml(), "UTF-8");

        log.debug("fechatimbrado: "+timbreFiscalDigital.getFechaTimbrado().getTime());

        // Inserta el CFDI
        CartaPorteCfdiDAO cartaPorteCfdiDAO = new CartaPorteCfdiDAO();
        cartaPorteCfdiDAO.id = null;
        cartaPorteCfdiDAO.compania = compania;
        cartaPorteCfdiDAO.status = "A";
        cartaPorteCfdiDAO.fechastatus = new Date();
        cartaPorteCfdiDAO.nocertificado = certificadoSelloDigitalDAO.nocertificado;
        cartaPorteCfdiDAO.uuid = timbreFiscalDigital.getUUID();
        cartaPorteCfdiDAO.fechatimbre = timbreFiscalDigital.getFechaTimbrado().getTime();
        cartaPorteCfdiDAO.rfcemisor = comprobante.getEmisor().getRfc();
        cartaPorteCfdiDAO.rfcreceptor = comprobante.getReceptor().getRfc();
        cartaPorteCfdiDAO.total = comprobante.getTotal().doubleValue();
        cartaPorteCfdiDAO.xml = xml;
        cartaPorteCfdiDAO.cadenaoriginal = respuesta.getCadenaoriginal();
        cartaPorteCfdiDAO.qr = respuesta.getQr();
        cartaPorteCfdiDAO.fechacancelacion = null;
        cartaPorteCfdiDAO.acusecancelacion = null;

        ds.insert(cartaPorteCfdiDAO);

        Integer id = (Integer)ds.aggregate(cartaPorteCfdiDAO, "MAX", "id");
        cartaPorteCfdiDAO.id = id;

        // Inserta la informacion de Factura - CFDI Carta Porte
        for (CartaPorteFacturaDAO cartaPorteFacturaDAO : cartaPorteFacturas) {
            cartaPorteFacturaDAO.idcartaporte = cartaPorteCfdiDAO.id;
            cartaPorteFacturaDAO.status = "A";
            cartaPorteFacturaDAO.fechastatus = new Date();

            ds.insert(cartaPorteFacturaDAO);
        }

        // Envia Correos
        for (CorreoFacturaTO correoFacturaTO : correos) {
            log.debug("Envio el correo a: "+correoFacturaTO.email);

            try {
                EmailServices es = new EmailServices(correoFacturaTO.email, correoFacturaTO.titulo, correoFacturaTO.mensaje, correoFacturaTO.attachments);
                MailBox mailBox = new MailBox(
                        Configuracion.getInstance().getProperty("cfdi.smtp.server"),
                        Configuracion.getInstance().getIntProperty("cfdi.smtp.port"),
                        Configuracion.getInstance().getProperty("cfdi.smtp.username"),
                        Configuracion.getInstance().getProperty("cfdi.smtp.password"),
                        Configuracion.getInstance().getBooleanProperty("cfdi.smtp.ssl"),
                        Configuracion.getInstance().getBooleanProperty("cfdi.smtp.authenticate"),
                        Configuracion.getInstance().getBooleanProperty("cfdi.smtp.starttls")
                );
                mailBox.setTimeout(Configuracion.getInstance().getIntProperty("cfdi.smtp.timeout"));
                mailBox.setProtocols(Configuracion.getInstance().getProperty("cfdi.smtp.protocols"));
                es.sendEmail(mailBox);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return cartaPorteCfdiDAO;
    }

    public String makeTextMensaje(CompaniaDAO companiaDAO, ASPELClienteDAO aspelClienteDAO, ASPELFacturaDAO aspelFacturaDAO) {
        StringBuilder html = new StringBuilder();

        html.append("<html>");
        html.append("<head>");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        html.append("<style>");
        html.append("html {font-family: sans-serif; line-height: 1.15; -webkit-text-size-adjust: 100%; -webkit-tap-highlight-color: rgba(0, 0, 0, 0);}");
        html.append(".w-100 {width: 100%;}");
        html.append(".center {display: block; margin-left: auto; margin-right: auto;}");
        html.append(".column {float: left; width: 50%;}");
        html.append(".row:after {content: \"\"; display: table; clear: both;}");
        html.append(".lead {font-size: 1.25rem; font-weight: 300;}");
        html.append(".text-center {text-align: center !important;}");
        html.append(".text-left {text-align: left !important;}");
        html.append(".text-header {color: #00205c; font-size: 1.5em; font-weight: bold; font-style: normal;}");
        html.append(".text-column {color: #00205c; font-size: 1.2em; font-weight: bold; font-style: normal;}");
        html.append(".text-title {color: #00205c; font-size: 1.0em; font-weight: bold; font-style: normal;}");
        html.append(".text-data {color: #000000; font-size: 1.0em; font-weight: normal; font-style: normal;}");
        html.append("th, td {padding-top: 15px; padding-bottom: 15px; padding-right: 5px; padding-left: 5px;}");
        html.append(".padding-simple {padding-top: 5px; padding-bottom: 5px; padding-right: 5px; padding-left: 5px;}");
        html.append(".button {background-color: #26a845; border: none; color: white; padding: 15px 32px; text-align: center; text-decoration: none; display: inline-block; font-size: 16px;}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div style=\"height: 40px;\"></div>");
        html.append("<div class=\"w-100\">")
            .append("<div class=\"lead\" style=\"font-size: 2.0em;\"><b>Para: ").append(aspelClienteDAO.NOMBRE).append(" (").append(aspelClienteDAO.RFC).append(") </b></div>")
            .append("<div style=\"height: 40px;\"></div>")
            .append("<div class=\"lead\" style=\"font-size: 1.8em;\"><b>Estimado Cliente,</div>")
            .append("<div style=\"height: 20px;\"></div>")
            .append("<div class=\"lead\" style=\"font-size: 1.8em;\"><b>").append(companiaDAO.nombre).append(" (").append(companiaDAO.rfc)
                .append(") emiti&oacute; para Usted un(os) documento(s) de tipo Factura con las siguientes caracter&iacute;sticas: </b></div>")
            .append("<div style=\"height: 20px;\"></div>")
            .append("<div class=\"lead\" style=\"font-size: 1.5em;\">Serie y Folio: <b>").append(aspelFacturaDAO.CVE_DOC).append("</b></div>")
            .append("<div class=\"lead\" style=\"font-size: 1.5em;\">Fecha de Emisi&oacute;n: <b>").append(Fecha.getFechaHora(aspelFacturaDAO.FECHAELAB)).append("</b></div>")
            .append("<div class=\"lead\" style=\"font-size: 1.5em;\">Monto Total: <b>").append(Numero.getMoneda(aspelFacturaDAO.IMPORTE)).append("</b></div>")
            .append("<div style=\"height: 40px;\"></div>")
            .append("<div class=\"lead\" style=\"font-size: 1.8em;\">Consulta los datos adjuntos, por favor.</div>")
            .append("<div style=\"height: 40px;\"></div>");
        //html.append("<div class=\"w-100\"><img alt=\"logo\" width=\"300px\" class=\"center\" src=\"https://pedidos.laeuropea.com.mx/cfdi/assets/media/various/logo.png\"></div>");
        html.append("<div style=\"height: 20px;\"></div>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    public File generaPDF(CartaPorteCfdiDAO cartaPorteCfdiDAO) throws Exception {
        ComprobanteDocument cd = ComprobanteDocument.Factory.parse(cartaPorteCfdiDAO.xml);

        TimbreFiscalDigitalDocument tfd = null;
        CartaPorteDocument cpd = null;
        PagosDocument pd = null;

        //
        // Leo los valores del timbre del comprobante
        //
        int begin = cartaPorteCfdiDAO.xml.indexOf("<tfd:TimbreFiscalDigital");
        int end = cartaPorteCfdiDAO.xml.indexOf("</cfdi:Complemento>", begin);
        String timbreFiscal = cartaPorteCfdiDAO.xml.substring(begin, end);

        tfd = TimbreFiscalDigitalDocument.Factory.parse(timbreFiscal);
        log.debug("UUID = "+tfd.getTimbreFiscalDigital().getUUID());

        boolean cartaporte = cartaPorteCfdiDAO.xml.contains("cartaporte20");
        if (cartaporte) {
            begin = cartaPorteCfdiDAO.xml.indexOf("<cartaporte20:CartaPorte");
            end = cartaPorteCfdiDAO.xml.indexOf("</cartaporte20:CartaPorte>", begin);
            String cartaporteXML = cartaPorteCfdiDAO.xml.substring(begin, end+26);
            cartaporteXML = cartaporteXML.replace("Version=", "xmlns:cartaporte20=\"http://www.sat.gob.mx/CartaPorte20\" Version=");

            cpd = CartaPorteDocument.Factory.parse(cartaporteXML);
            log.debug("Version Carta Porte = "+cpd.getCartaPorte().getVersion());
        }

        log.debug("Generando el PDF ... ");
        PDFFactory pdfFactory = new PDFFactoryImp();
        pdfFactory.setup(getFontPath(), getLogoPath());
        byte[] pdf = pdfFactory.genera(ds, cd, tfd, cpd, null, cartaPorteCfdiDAO.cadenaoriginal, cartaPorteCfdiDAO.qr,
                null, null, null, null, null, null, null, null,
                null);
                pdfFactory.terminate();

        cartaPorteCfdiDAO.pdf = pdf;
        ds.update(cartaPorteCfdiDAO, new String[] {"pdf"});

        File dir = new File(Configuracion.getInstance().getProperty("ruta.pdf"));
        if (!dir.exists())
            throw new WebException("No existe este directorio ["+dir.getAbsolutePath()+"]");

        File fileCartaPorte = new File(dir, "CartaPorte_"+cartaPorteCfdiDAO.id+".pdf");
        log.debug("fileCartaPorte: "+fileCartaPorte.getAbsolutePath());

        try (FileOutputStream fos = new FileOutputStream(fileCartaPorte)) {
            fos.write(cartaPorteCfdiDAO.pdf);
        }

        return fileCartaPorte;
    }

    private String getFontPath() {
        String rutaFont = Configuracion.getInstance().getProperty("ruta.font");
        if (rutaFont==null)
            return null;
        File fileFont = new File(rutaFont);
        if (fileFont.exists())
            return fileFont.getAbsolutePath();
        return null;
    }

    private String getLogoPath() {
        String rutaLogo = Configuracion.getInstance().getProperty("ruta.logo");
        if (rutaLogo==null)
            return null;
        File fileLogo = new File(rutaLogo);
        if (fileLogo.exists())
            return fileLogo.getAbsolutePath();
        return null;
    }

}
