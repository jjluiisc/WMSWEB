package mx.reder.wms.cfdi;

import com.atcloud.dao.engine.DatabaseServices;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import com.atcloud.util.F;
import com.atcloud.util.Numero;
import com.atcloud.util.Fecha;
import mx.reder.wms.cfdi.entity.ComprobanteCFD;
import mx.reder.wms.cfdi.entity.ConceptoCFD;
import mx.reder.wms.cfdi.entity.TimbradoCFD;
import mx.reder.wms.dao.entity.CertificadoSelloDigitalDAO;
import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.CfdiRelacionados;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.CfdiRelacionados.CfdiRelacionado;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos.Traslados;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos.Traslados.Traslado;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.sitioInternet.cfd.catalogos.CImpuesto;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoFactor;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoRelacion;
import mx.gob.sat.timbreFiscalDigital.TimbreFiscalDigitalDocument;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptionCharEscapeMap;
import org.apache.xmlbeans.XmlOptions;

public class ExtraccionImp {
    static Logger log = Logger.getLogger(ExtraccionImp.class.getName());

    private String compania;
    private Transformer transformer;
    private Transformer transformerTFD;
    private DatabaseServices ds;

    public ExtraccionImp(String compania) {
        this.compania = compania;
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
            String home = System.getProperty("ecommerce.home");
            String xslt = home+"/cfdi/cadenaoriginal_3_3.xslt";
            log.debug("Archivo XSLT de Cadena Original = ["+xslt+"] ...");
            transformer = tFactory.newTransformer(new StreamSource(xslt));

            String xsltTFD = home+"/cfdi/cadenaoriginal_TFD_1_1.xslt";
            log.debug("Archivo XSLT de Cadena Original TFD = ["+xsltTFD+"] ...");
            transformerTFD = tFactory.newTransformer(new StreamSource(xsltTFD));
        } catch (TransformerConfigurationException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }

    public RespuestaComprobante getComprobante(ComprobanteCFD comprobanteCFD, TimbradoCFD timbrado) throws Exception {
        ComprobanteDocument cd = ComprobanteDocument.Factory.newInstance();
        Comprobante comprobante = cd.addNewComprobante();

        cargaGenerales(comprobanteCFD, comprobante);
        cargaConceptos(comprobanteCFD, comprobante);
        cargaImpuestos(comprobanteCFD, comprobante);

        String cadenaOriginal = generaCadenaOriginal(cd);
        log.debug("Cadena Original ["+cadenaOriginal+"]");
        comprobante.setSello(EncriptacionFacade.getInstance().firma(comprobante.getNoCertificado(), cadenaOriginal));
        log.debug("Sello Digial ["+comprobante.getSello()+"]");

        XmlCursor cursor = cd.newCursor();
        if (cursor.toFirstChild()) {
            //if (comprobante.getComplemento()!=null)
            //    cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"),
            //        "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd http://www.sat.gob.mx/detallista http://www.sat.gob.mx/sitio_internet/cfd/detallista/detallista.xsd");
            //else
                cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"),
                    "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd");
        }

        XmlOptionCharEscapeMap escapes = new XmlOptionCharEscapeMap();
        escapes.addMapping('&', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        escapes.addMapping('\"', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        escapes.addMapping('>', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        escapes.addMapping('<', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        escapes.addMapping('\'', XmlOptionCharEscapeMap.PREDEF_ENTITY);

        XmlOptions opts = new XmlOptions();
        //if (Configuracion.getInstance().getBooleanProperty("xml.pretty.print")) {
        //    opts.setSavePrettyPrint();
        //    opts.setSavePrettyPrintIndent(4);
        //}
        opts.setUseCDataBookmarks();
        opts.setSaveSubstituteCharacters(escapes);
        //opts.setUseDefaultNamespace();
        HashMap ns = new HashMap();
        ns.put("http://www.sat.gob.mx/cfd/3", "cfdi");
        opts.setSaveSuggestedPrefixes(ns);

        String xmlText = cd.xmlText(opts);
        xmlText = xmlText.replaceAll(" xmlns=\"\"", "");
        log.debug("Antes Timbrado: "+xmlText);

        //
        //
        //
        /*
         * Falla la Validacion
         *
         * curl -O http://www.sat.gob.mx/sitio_internet/cfd/tipoDatos/tdCFDI
         *
         *
        String home = System.getProperty("ecommerce.home");
        String xsd = home+"/cfdi/cfdv33.xsd";

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(xsd));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(xmlText)));
         */

        //
        // Timbrar el comprobante
        //
        cd = timbraComprobante(timbrado, xmlText);
        log.debug("CFDI = "+cd.xmlText(opts));

        xmlText = cd.xmlText(opts);
        xmlText = xmlText.replaceAll(" xmlns=\"\"", "");
        log.debug("XML = "+xmlText);

        //
        // Leo los valores del timbre del comprobante
        //
        int begin = xmlText.indexOf("<tfd:TimbreFiscalDigital");
        int end = xmlText.indexOf("</cfdi:Complemento>", begin);
        String timbreFiscal = xmlText.substring(begin, end);

        TimbreFiscalDigitalDocument tfd = null;
        tfd = TimbreFiscalDigitalDocument.Factory.parse(timbreFiscal);
        String uuid = tfd.getTimbreFiscalDigital().getUUID();
        log.debug("UUID = "+uuid);

        //
        //
        //
        RespuestaComprobante respuesta = new RespuestaComprobante();
        respuesta.setXml(xmlText.getBytes("UTF-8"));
        respuesta.setQr(generaQR(cd, uuid));
        respuesta.setCadenaoriginal(generaCadenaOriginalTFD(tfd));
        respuesta.setCd(cd);
        respuesta.setTfd(tfd);

        return respuesta;
    }

    public void enviaComprobante() throws Exception {
    }

    public void cargaGenerales(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        comprobante.setVersion("3.3");
        comprobante.setSerie(comprobanteCFD.getSerie());
        comprobante.setFolio(comprobanteCFD.getFolio());

        comprobante.setFecha(comprobanteCFD.getFecha());

        if (comprobanteCFD.getTipoDeComprobante().toString().compareTo("P")!=0)
            comprobante.setFormaPago(comprobanteCFD.getFormaDePago());
        if (comprobanteCFD.getMetodoDePago()!=null&&!comprobanteCFD.getMetodoDePago().toString().isEmpty())
            comprobante.setMetodoPago(comprobanteCFD.getMetodoDePago());
        comprobante.setLugarExpedicion(comprobanteCFD.getLugarExpedicion());

        String fecha = Fecha.getFechaHora(comprobanteCFD.getFecha().getTime());
        CertificadoSelloDigitalDAO efacturasellos = (CertificadoSelloDigitalDAO)ds.first(new CertificadoSelloDigitalDAO(),
            "compania = '"+compania+"' AND fechainicial <= '"+fecha+"' AND fechafinal >= '"+fecha+"'");
        if(efacturasellos==null)
            throw new Exception("No existe efacturasellos para rfc = '"
                +compania+"' fecha = '"+fecha+"'");

        EncriptacionFacade.getInstance().inicializa(efacturasellos);

        comprobante.setNoCertificado(efacturasellos.nocertificado);
        comprobante.setCertificado(EncriptacionFacade.getInstance().getCertificadoBase64(efacturasellos.nocertificado));

        comprobante.setTipoDeComprobante(comprobanteCFD.getTipoDeComprobante());
        comprobante.setMoneda(comprobanteCFD.getMoneda());

        cargaRelacionados(comprobanteCFD, comprobante);
        cargaEmisor(comprobanteCFD, comprobante);
        cargaReceptor(comprobanteCFD, comprobante);
    }

    public void cargaRelacionados(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        String cfdirelacionados = comprobanteCFD.getCfdiRelacionados();
        if (cfdirelacionados==null||cfdirelacionados.isEmpty())
            return;

        String[] tokens = cfdirelacionados.split("\\|");
        if (tokens.length==0)
            return;

        String tipoRelacionANT = "";
        CfdiRelacionados cfdiRelacionados = null;

        for (String token : tokens) {
            if (token.isEmpty())
                continue;

            String[] partes = token.split(",");
            String tipoRelacion = partes[0];
            String UUID = partes[1];

            if (tipoRelacionANT.compareTo(tipoRelacion)!=0) {
                cfdiRelacionados = comprobante.addNewCfdiRelacionados();
                cfdiRelacionados.setTipoRelacion(CTipoRelacion.Enum.forString(tipoRelacion));

                tipoRelacionANT = tipoRelacion;
            }

            CfdiRelacionado cfdiRelacionado = cfdiRelacionados.addNewCfdiRelacionado();
            cfdiRelacionado.setUUID(UUID);
        }
    }

    public void cargaEmisor(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        Emisor emisor = comprobante.addNewEmisor();
        emisor.setNombre(comprobanteCFD.getEmisor().getNombre());
        emisor.setRfc(comprobanteCFD.getEmisor().getRfc());

        // Regimen Fiscal
        emisor.setRegimenFiscal(comprobanteCFD.getRegimenFiscal());
    }

    public void cargaReceptor(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        Receptor receptor = comprobante.addNewReceptor();
        receptor.setNombre(comprobanteCFD.getReceptor().getNombre());
        receptor.setRfc(comprobanteCFD.getReceptor().getRfc());

        receptor.setUsoCFDI(comprobanteCFD.getReceptor().getUsoCFDI());
    }

    public void cargaConceptos(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        Conceptos conceptos = comprobante.addNewConceptos();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuento = BigDecimal.ZERO;

        Iterator it = comprobanteCFD.getConceptos().iterator();
        while(it.hasNext()) {
            ConceptoCFD conceptoCFD = (ConceptoCFD)it.next();
            Concepto concepto = conceptos.addNewConcepto();

            concepto.setCantidad(Numero.getBigDecimal(conceptoCFD.getCantidad(), 4));
            concepto.setUnidad(conceptoCFD.getUnidad());
            concepto.setDescripcion(conceptoCFD.getDescripcion());
            concepto.setNoIdentificacion(conceptoCFD.getNoIdentificacion());
            concepto.setValorUnitario(Numero.getBigDecimal(conceptoCFD.getValorUnitario(), 4));
            concepto.setImporte(Numero.getBigDecimal(conceptoCFD.getImporte()));
            concepto.setClaveProdServ(conceptoCFD.getClaveProductoServicio());
            concepto.setClaveUnidad(conceptoCFD.getClaveUnidad());
            if (conceptoCFD.getDescuento()>0.0)
                concepto.setDescuento(Numero.getBigDecimal(conceptoCFD.getDescuento()));

            subtotal = subtotal.add(concepto.getImporte());
            descuento = descuento.add(Numero.getBigDecimal(conceptoCFD.getDescuento()));

            if (conceptoCFD.getPrIva()>0.0||conceptoCFD.getPrIesps()>0.0) {
                Concepto.Impuestos impuestos = concepto.addNewImpuestos();
                Concepto.Impuestos.Traslados traslados = impuestos.addNewTraslados();
                if (conceptoCFD.getPrIva()>0.0) {
                    Concepto.Impuestos.Traslados.Traslado trasladoIva = traslados.addNewTraslado();
                    trasladoIva.setBase(Numero.getBigDecimal(conceptoCFD.getImporte() - conceptoCFD.getDescuento()));
                    trasladoIva.setImporte(Numero.getBigDecimal(conceptoCFD.getIva()));
                    trasladoIva.setTasaOCuota(Numero.getBigDecimal(conceptoCFD.getPrIva(), 6));
                    trasladoIva.setImpuesto(CImpuesto.X_002);
                    trasladoIva.setTipoFactor(CTipoFactor.TASA);
                }
                if (conceptoCFD.getPrIesps()>0.0) {
                    Concepto.Impuestos.Traslados.Traslado trasladoIesps = traslados.addNewTraslado();
                    trasladoIesps.setBase(Numero.getBigDecimal(conceptoCFD.getImporte() - conceptoCFD.getDescuento()));
                    trasladoIesps.setImporte(Numero.getBigDecimal(conceptoCFD.getIesps()));
                    trasladoIesps.setTasaOCuota(Numero.getBigDecimal(conceptoCFD.getPrIesps(),6));
                    trasladoIesps.setImpuesto(CImpuesto.X_003);
                    trasladoIesps.setTipoFactor(CTipoFactor.TASA);
                }
            }
        }

        comprobante.setSubTotal(Numero.getBigDecimal(subtotal));
        if (descuento.doubleValue()>0.0)
            comprobante.setDescuento(Numero.getBigDecimal(descuento));
    }

    public void cargaImpuestos(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        HashMap tasasIva = new HashMap();
        HashMap tasasIesps = new HashMap();

        Concepto[] conceptos = comprobante.getConceptos().getConceptoArray();
        for(Concepto concepto : conceptos) {
            Concepto.Impuestos impuestos = concepto.getImpuestos();

            if (impuestos!=null) {
                Concepto.Impuestos.Traslados.Traslado[] traslados = impuestos.getTraslados().getTrasladoArray();
                for(Concepto.Impuestos.Traslados.Traslado traslado : traslados) {

                    // IVA
                    if (traslado.getImpuesto().equals(CImpuesto.X_002)) {
                        String tasaIva = String.valueOf(Numero.redondea(traslado.getTasaOCuota().doubleValue() * 100.0));
                        BigDecimal iva = Numero.getBigDecimal(traslado.getImporte());
                        BigDecimal montoAcumulado = (BigDecimal)tasasIva.get(tasaIva);
                        if(montoAcumulado == null)
                            montoAcumulado = iva;
                        else
                            montoAcumulado = montoAcumulado.add(iva);
                        log.debug("tasaIva = "+tasaIva+" taxable_amount = "+Numero.redondea(traslado.getImporte().doubleValue())
                            +" tax = "+iva+" montoAcumulado = "+montoAcumulado);
                        tasasIva.put(tasaIva, montoAcumulado);
                    }
                    // IESPS
                    else if (traslado.getImpuesto().equals(CImpuesto.X_003)) {
                        String tasaIesps = String.valueOf(Numero.redondea(traslado.getTasaOCuota().doubleValue() * 100.0));
                        BigDecimal iesps = Numero.getBigDecimal(traslado.getImporte());
                        BigDecimal montoAcumulado = (BigDecimal)tasasIesps.get(tasaIesps);
                        if(montoAcumulado == null)
                            montoAcumulado = iesps;
                        else
                            montoAcumulado = montoAcumulado.add(iesps);
                        log.debug("tasaIesps = "+tasaIesps+" taxable_amount = "+Numero.redondea(traslado.getImporte().doubleValue())
                            +" tax = "+iesps+" montoAcumulado = "+montoAcumulado);
                        tasasIesps.put(tasaIesps, montoAcumulado);

                    }
                }
            }
        }

        Impuestos impuestos = comprobante.addNewImpuestos();
        BigDecimal totalImpuestos = BigDecimal.ZERO;

        if (tasasIva.size()>0||tasasIesps.size()>0) {
            Traslados traslados = impuestos.addNewTraslados();

            Iterator itIVA = tasasIva.keySet().iterator();
            while(itIVA.hasNext()) {
                String tazaIva = (String)itIVA.next();
                BigDecimal montoAcumulado = (BigDecimal)tasasIva.get(tazaIva);
                totalImpuestos = totalImpuestos.add(montoAcumulado);

                Traslado trasladoIva = traslados.addNewTraslado();
                trasladoIva.setImporte(Numero.getBigDecimal(montoAcumulado));
                trasladoIva.setTasaOCuota(Numero.getBigDecimal(Numero.getDoubleFromString(tazaIva)/100.0, 6));
                trasladoIva.setImpuesto(CImpuesto.X_002);
                trasladoIva.setTipoFactor(CTipoFactor.TASA);
            }
            Iterator itIESPS = tasasIesps.keySet().iterator();
            while(itIESPS.hasNext()) {
                String tazaIesps = (String)itIESPS.next();
                BigDecimal montoAcumulado = (BigDecimal)tasasIesps.get(tazaIesps);
                totalImpuestos = totalImpuestos.add(montoAcumulado);

                Traslado trasladoIesps = traslados.addNewTraslado();
                trasladoIesps.setImporte(Numero.getBigDecimal(montoAcumulado));
                trasladoIesps.setTasaOCuota(Numero.getBigDecimal(Numero.getDoubleFromString(tazaIesps)/100.0,6));
                trasladoIesps.setImpuesto(CImpuesto.X_003);
                trasladoIesps.setTipoFactor(CTipoFactor.TASA);
            }
        }

        impuestos.setTotalImpuestosTrasladados(Numero.getBigDecimal(totalImpuestos));

        // Elimina el nodo si no hay impuestos
        if (totalImpuestos.doubleValue()==0.0d)
            comprobante.unsetImpuestos();

        if (comprobante.getDescuento()!=null)
            comprobante.setTotal(Numero.getBigDecimal(comprobante.getSubTotal()).add(totalImpuestos).subtract(comprobante.getDescuento()));
        else
            comprobante.setTotal(Numero.getBigDecimal(comprobante.getSubTotal()).add(totalImpuestos));
    }

    public ComprobanteDocument timbraComprobante(TimbradoCFD timbrado, String cfdi) throws Exception {
        log.debug("Timbrando ... ");

        RespuestaPAC respuesta = timbrado.getCFDI("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+cfdi);

        return respuesta.getCd();
    }

    private String generaCadenaOriginalTFD(TimbreFiscalDigitalDocument tfd) throws Exception {
        StringWriter sw = new StringWriter();
        transformerTFD.transform(new StreamSource(tfd.newInputStream()), new StreamResult(sw));
        String cadenaOriginalTFD = sw.toString();
        return cadenaOriginalTFD;
    }

    public String generaCadenaOriginal(ComprobanteDocument cd) throws Exception {
        StringWriter sw = new StringWriter();
        transformer.transform(new StreamSource(cd.newInputStream()), new StreamResult(sw));
        String cadenaOriginal = sw.toString();
        return cadenaOriginal;
    }

    private String generaQR(ComprobanteDocument cd, String UUID) {
        String sello = cd.getComprobante().getSello();
        StringBuilder ret = new StringBuilder("https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx");
        ret
            .append("?&id=")
            .append(UUID)
            .append("&re=")
            .append(cd.getComprobante().getEmisor().getRfc())
            .append("&rr=")
            .append(cd.getComprobante().getReceptor().getRfc())
            .append("&tt=")
            .append(F.f(cd.getComprobante().getTotal().doubleValue(), 17, 6, F.ZF))
            .append("&fe=")
            .append(sello.substring(sello.length() - 8))
            ;
        String qr = ret.toString();
        return qr;
    }
}
