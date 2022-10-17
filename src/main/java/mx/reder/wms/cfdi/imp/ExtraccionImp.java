package mx.reder.wms.cfdi.imp;

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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import mx.gob.sat.cfd.x4.ComprobanteDocument;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.CfdiRelacionados;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.CfdiRelacionados.CfdiRelacionado;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Conceptos;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Impuestos;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Impuestos.Traslados;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Impuestos.Traslados.Traslado;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.sitioInternet.cfd.catalogos.CImpuesto;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoFactor;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoRelacion;
import mx.gob.sat.timbreFiscalDigital.TimbreFiscalDigitalDocument;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Complemento;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptionCharEscapeMap;
import org.apache.xmlbeans.XmlOptions;
import mx.reder.wms.cfdi.EncriptacionFacade;
import mx.reder.wms.cfdi.RespuestaComprobante;
import mx.reder.wms.cfdi.RespuestaPAC;
import mx.reder.wms.cfdi.entity.CartaPorteCFD;
import mx.reder.wms.cfdi.entity.CartaPorteUbicacionCFD;
import mx.reder.wms.cfdi.entity.CertificadoSelloDigitalCFD;
import mx.reder.wms.cfdi.entity.ComprobanteCFD;
import mx.reder.wms.cfdi.entity.ConceptoCFD;
import mx.reder.wms.cfdi.entity.CartaPorteDomicilioCFD;
import mx.reder.wms.cfdi.entity.CartaPorteFiguraTransporteCFD;
import mx.reder.wms.cfdi.entity.CartaPorteMercanciaCFD;
import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteCFD;
import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteRemolqueCFD;
import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteSeguroCFD;
import mx.reder.wms.cfdi.entity.CartaPorteCantidadTransportaCFD;
import mx.reder.wms.cfdi.entity.CartaPorteTipoFiguraTransporteCFD;
import mx.reder.wms.cfdi.entity.ComprobantePagosCFD;
import mx.reder.wms.cfdi.entity.PagoCFD;
import mx.reder.wms.cfdi.entity.PagoDRImpuestoCFD;
import mx.reder.wms.cfdi.entity.PagoDocumentoRelacionadoCFD;
import mx.reder.wms.cfdi.entity.TimbradoCFD;
import mx.gob.sat.cartaPorte20.CartaPorteDocument;
import mx.gob.sat.cartaPorte20.CartaPorteDocument.CartaPorte;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.InformacionGlobal;
import mx.gob.sat.pagos20.PagosDocument;
import mx.gob.sat.pagos20.PagosDocument.Pagos;
import mx.gob.sat.pagos20.PagosDocument.Pagos.Pago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMeses;
import mx.gob.sat.sitioInternet.cfd.catalogos.CPeriodicidad;

public class ExtraccionImp {
    static Logger log = Logger.getLogger(ExtraccionImp.class.getName());

    private Transformer transformer;
    private Transformer transformerTFD;
    private CertificadoSelloDigitalCFD efacturasellos;

    public ExtraccionImp() {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
            String home = System.getProperty("ecommerce.home");
            String xslt = home+"/cfdi/cadenaoriginal_4_0.xslt";
            log.debug("Archivo XSLT de Cadena Original = ["+xslt+"] ...");
            transformer = tFactory.newTransformer(new StreamSource(xslt));

            String xsltTFD = home+"/cfdi/cadenaoriginal_TFD_1_1.xslt";
            log.debug("Archivo XSLT de Cadena Original TFD = ["+xsltTFD+"] ...");
            transformerTFD = tFactory.newTransformer(new StreamSource(xsltTFD));

        } catch (TransformerConfigurationException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void setCertificadoSelloDigital(CertificadoSelloDigitalCFD efacturasellos) {
        this.efacturasellos = efacturasellos;
    }

    public RespuestaComprobante getComprobante(ComprobanteCFD comprobanteCFD, TimbradoCFD timbrado) throws Exception {
        ComprobanteDocument cd = ComprobanteDocument.Factory.newInstance();
        Comprobante comprobante = cd.addNewComprobante();

        cargaGenerales(comprobanteCFD, comprobante);
        cargaConceptos(comprobanteCFD, comprobante);
        cargaImpuestos(comprobante);
        if(comprobanteCFD.getCartaPorte()!=null)
            cargaComplementoCartaPorte(comprobanteCFD, comprobante, cd);
        if(comprobanteCFD.getComprobantePagos()!=null)
            cargaComplementoPago(comprobanteCFD, comprobante, cd);

        String cadenaOriginal = generaCadenaOriginal(cd);
        log.debug("Cadena Original ["+cadenaOriginal+"]");
        comprobante.setSello(EncriptacionFacade.getInstance().firma(comprobante.getNoCertificado(), cadenaOriginal));
        log.debug("Sello Digial ["+comprobante.getSello()+"]");

        XmlCursor cursor = cd.newCursor();
        if (cursor.toFirstChild()) {
            if (comprobante.getComplementoArray().length==0) {
                cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"),
                    "http://www.sat.gob.mx/cfd/4 http://www.sat.gob.mx/sitio_internet/cfd/4/cfdv40.xsd");
            } else {
                if (comprobanteCFD.getCartaPorte()!=null) {
                    cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"),
                        "http://www.sat.gob.mx/cfd/4 http://www.sat.gob.mx/sitio_internet/cfd/4/cfdv40.xsd http://www.sat.gob.mx/CartaPorte20 http://www.sat.gob.mx/sitio_internet/cfd/CartaPorte/CartaPorte20.xsd");
                }
                if (comprobanteCFD.getComprobantePagos()!=null) {
                    cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"),
                        "http://www.sat.gob.mx/cfd/4 http://www.sat.gob.mx/sitio_internet/cfd/4/cfdv40.xsd http://www.sat.gob.mx/Pagos20 http://www.sat.gob.mx/sitio_internet/cfd/Pagos/Pagos20.xsd");
                }
            }
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
        ns.put("http://www.sat.gob.mx/cfd/4", "cfdi");
        opts.setSaveSuggestedPrefixes(ns);

        String xmlText = cd.xmlText(opts);
        xmlText = xmlText.replaceAll(" xmlns=\"\"", "");

        if (comprobanteCFD.getCartaPorte()!=null) {
            xmlText = xmlText.replace(" xmlns:car=\"http://www.sat.gob.mx/CartaPorte20\"", "");
            xmlText = xmlText.replace("xmlns:cfdi=\"http://www.sat.gob.mx/cfd/4\"",
                    "xmlns:cfdi=\"http://www.sat.gob.mx/cfd/4\" xmlns:cartaporte20=\"http://www.sat.gob.mx/CartaPorte20\"");
            xmlText = xmlText.replace("car:", "cartaporte20:");
        }
        if (comprobanteCFD.getComprobantePagos()!=null) {
            xmlText = xmlText.replace(" xmlns:pag=\"http://www.sat.gob.mx/Pagos20\"", "");
            xmlText = xmlText.replace("xmlns:cfdi=\"http://www.sat.gob.mx/cfd/4\"",
                    "xmlns:cfdi=\"http://www.sat.gob.mx/cfd/4\" xmlns:pago20=\"http://www.sat.gob.mx/Pagos20\"");
            xmlText = xmlText.replace("pag:", "pago20:");
        }

        log.debug("Antes Timbrado: "+xmlText);

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
        comprobante.setVersion("4.0");
        comprobante.setSerie(comprobanteCFD.getSerie());
        comprobante.setFolio(comprobanteCFD.getFolio());

        comprobante.setFecha(comprobanteCFD.getFecha());

        if (comprobanteCFD.getTipoDeComprobante().toString().compareTo("P")!=0&&comprobanteCFD.getTipoDeComprobante().toString().compareTo("T")!=0)
            comprobante.setFormaPago(comprobanteCFD.getFormaDePago());
        if (comprobanteCFD.getMetodoDePago()!=null&&!comprobanteCFD.getMetodoDePago().toString().isEmpty())
            comprobante.setMetodoPago(comprobanteCFD.getMetodoDePago());
        comprobante.setLugarExpedicion(comprobanteCFD.getLugarExpedicion());

        EncriptacionFacade.getInstance().inicializa(efacturasellos);

        comprobante.setExportacion(comprobanteCFD.getExportacion());
        comprobante.setNoCertificado(efacturasellos.getNocertificado());
        comprobante.setCertificado(EncriptacionFacade.getInstance().getCertificadoBase64(efacturasellos.getNocertificado()));

        comprobante.setTipoDeComprobante(comprobanteCFD.getTipoDeComprobante());
        comprobante.setMoneda(comprobanteCFD.getMoneda());
        if (comprobanteCFD.getTipoCambio()!=null)
            comprobante.setTipoCambio(comprobanteCFD.getTipoCambio());

        cargaInformacionGlobal(comprobanteCFD, comprobante);
        cargaRelacionados(comprobanteCFD, comprobante);
        cargaEmisor(comprobanteCFD, comprobante);
        cargaReceptor(comprobanteCFD, comprobante);
    }

    public void cargaInformacionGlobal(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        String informacionglobal = comprobanteCFD.getInformacionGlobal();
        if (informacionglobal==null||informacionglobal.isEmpty())
            return;

        String[] partes = informacionglobal.split(",");
        if (partes.length==0)
            return;

        String periodicidad = partes[0];
        String meses = partes[1];
        String año = partes[2];

        InformacionGlobal informacionGlobal = comprobante.addNewInformacionGlobal();
        informacionGlobal.setPeriodicidad(CPeriodicidad.Enum.forString(periodicidad));
        informacionGlobal.setMeses(CMeses.Enum.forString(meses));
        informacionGlobal.setAño((short)Numero.getIntFromString(año));
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
        emisor.setRegimenFiscal(comprobanteCFD.getRegimenFiscal());
    }

    public void cargaReceptor(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        Receptor receptor = comprobante.addNewReceptor();
        receptor.setNombre(comprobanteCFD.getReceptor().getNombre());
        receptor.setRfc(comprobanteCFD.getReceptor().getRfc());
        receptor.setDomicilioFiscalReceptor(comprobanteCFD.getDomicilio().getCodigoPostal());
        receptor.setRegimenFiscalReceptor(comprobanteCFD.getReceptor().getRegimenFiscalReceptor());
        receptor.setUsoCFDI(comprobanteCFD.getUsoCFDI());
    }

    public void cargaConceptos(ComprobanteCFD comprobanteCFD, Comprobante comprobante) throws Exception {
        Conceptos conceptos = comprobante.addNewConceptos();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuento = BigDecimal.ZERO;

        Iterator it = comprobanteCFD.getConceptos().iterator();
        while(it.hasNext()) {
            ConceptoCFD conceptoCFD = (ConceptoCFD)it.next();
            Concepto concepto = conceptos.addNewConcepto();

            concepto.setDescripcion(conceptoCFD.getDescripcion());
            if (conceptoCFD.getNoIdentificacion()!=null)
                concepto.setNoIdentificacion(conceptoCFD.getNoIdentificacion());

            if (comprobanteCFD.getTipoDeComprobante().toString().compareTo("P")==0) {
                concepto.setCantidad(BigDecimal.ONE);
                concepto.setValorUnitario(BigDecimal.ZERO);
                concepto.setImporte(BigDecimal.ZERO);
            } else {
                concepto.setUnidad(conceptoCFD.getUnidad());
                concepto.setCantidad(Numero.getBigDecimal(conceptoCFD.getCantidad(), 6));
                concepto.setValorUnitario(Numero.getBigDecimal(conceptoCFD.getValorUnitario(), 6));
                concepto.setImporte(Numero.getBigDecimal(conceptoCFD.getImporte()));
            }

            concepto.setClaveProdServ(conceptoCFD.getClaveProductoServicio());
            concepto.setClaveUnidad(conceptoCFD.getClaveUnidad());
            if (conceptoCFD.getDescuento()>0.0)
                concepto.setDescuento(Numero.getBigDecimal(conceptoCFD.getDescuento()));
            concepto.setObjetoImp(conceptoCFD.getObjetoImp());

            subtotal = subtotal.add(concepto.getImporte());
            descuento = descuento.add(Numero.getBigDecimal(conceptoCFD.getDescuento()));

            if (conceptoCFD.getPrIva()>0.0||conceptoCFD.getPrIesps()>0.0) {
                Concepto.Impuestos impuestos = concepto.addNewImpuestos();
                Concepto.Impuestos.Traslados traslados = impuestos.addNewTraslados();
                // Primero el IESPS
                if (conceptoCFD.getPrIesps()>0.0) {
                    Concepto.Impuestos.Traslados.Traslado trasladoIesps = traslados.addNewTraslado();
                    trasladoIesps.setBase(Numero.getBigDecimal(conceptoCFD.getImporte() - conceptoCFD.getDescuento()));
                    trasladoIesps.setImporte(Numero.getBigDecimal(conceptoCFD.getIesps()));
                    trasladoIesps.setTasaOCuota(Numero.getBigDecimal(conceptoCFD.getPrIesps(),6));
                    trasladoIesps.setImpuesto(CImpuesto.X_003);
                    trasladoIesps.setTipoFactor(CTipoFactor.TASA);
                }
                // A la base del IVA se le suma el Impuesto de IESPS
                if (conceptoCFD.getPrIva()>0.0) {
                    Concepto.Impuestos.Traslados.Traslado trasladoIva = traslados.addNewTraslado();
                    trasladoIva.setBase(Numero.getBigDecimal(conceptoCFD.getImporte() + conceptoCFD.getIesps() - conceptoCFD.getDescuento()));
                    trasladoIva.setImporte(Numero.getBigDecimal(conceptoCFD.getIva()));
                    trasladoIva.setTasaOCuota(Numero.getBigDecimal(conceptoCFD.getPrIva(), 6));
                    trasladoIva.setImpuesto(CImpuesto.X_002);
                    trasladoIva.setTipoFactor(CTipoFactor.TASA);
                }
            }
        }

        comprobante.setSubTotal(Numero.getBigDecimal(subtotal));
        if (descuento.doubleValue()>0.0)
            comprobante.setDescuento(Numero.getBigDecimal(descuento));
    }

    public void cargaImpuestos(Comprobante comprobante) throws Exception {
        HashMap<String, ImpuestoAcumulado> impuestosTrasladados = new HashMap<>();

        Concepto[] conceptos = comprobante.getConceptos().getConceptoArray();
        for(Concepto concepto : conceptos) {
            Concepto.Impuestos impuestos = concepto.getImpuestos();

            if (impuestos!=null) {
                Concepto.Impuestos.Traslados.Traslado[] traslados = impuestos.getTraslados().getTrasladoArray();
                for(Concepto.Impuestos.Traslados.Traslado traslado : traslados) {
                    ImpuestoAcumulado impuesto = new ImpuestoAcumulado();
                    impuesto.impuesto = traslado.getImpuesto();
                    impuesto.tipofactor = traslado.getTipoFactor();
                    impuesto.tasa = traslado.getTasaOCuota().doubleValue();
                    impuesto.base = traslado.getBase().doubleValue();
                    impuesto.importe = traslado.getImporte().doubleValue();

                    // Impuestos del Pago
                    ImpuestoAcumulado impuestoAcumulado = impuestosTrasladados.get(impuesto.getKey());
                    if (impuestoAcumulado==null) {
                        impuestoAcumulado = impuesto;
                    } else {
                        impuestoAcumulado.acumula(impuesto);
                    }
                    impuestosTrasladados.put(impuestoAcumulado.getKey(), impuestoAcumulado);
                }
            }
        }

        Impuestos impuestos = comprobante.addNewImpuestos();
        BigDecimal totalImpuestos = BigDecimal.ZERO;

        if (impuestosTrasladados.size()>0) {
            Traslados traslados = impuestos.addNewTraslados();
            for (ImpuestoAcumulado impuestoAcumulado : impuestosTrasladados.values()) {
                log.debug("impuesto = "+impuestoAcumulado.getKey()+" base = "+impuestoAcumulado.base+" importe = "+impuestoAcumulado.importe);
                Traslado traslado = traslados.addNewTraslado();
                traslado.setBase(Numero.getBigDecimal(impuestoAcumulado.base));
                traslado.setImporte(Numero.getBigDecimal(impuestoAcumulado.importe));
                traslado.setTasaOCuota(Numero.getBigDecimal(impuestoAcumulado.tasa, 6));
                traslado.setImpuesto(impuestoAcumulado.impuesto);
                traslado.setTipoFactor(impuestoAcumulado.tipofactor);

                totalImpuestos = totalImpuestos.add(traslado.getImporte());
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

    public void cargaComplementoPago(ComprobanteCFD comprobanteCFD, Comprobante comprobante, ComprobanteDocument cd) {
        XmlObject complementoObject = getComplementoPago(comprobanteCFD);
        if (complementoObject==null)
            return;

        Complemento complemento = comprobante.addNewComplemento();
        complemento.set(complementoObject);

        comprobante.setSubTotal(BigDecimal.ZERO);
        comprobante.setTotal(BigDecimal.ZERO);
    }

    public PagosDocument getComplementoPago(ComprobanteCFD comprobanteCFD) {
        PagosDocument cpd = PagosDocument.Factory.newInstance();

        Pagos pagos = cpd.addNewPagos();
        pagos.setVersion("2.0");

        BigDecimal montoTotalPagos = BigDecimal.ZERO;
        HashMap<String, ImpuestoAcumulado> impuestosTrasladadosT = new HashMap<>();

        ComprobantePagosCFD comprobantePagosCFD = comprobanteCFD.getComprobantePagos();

        for(PagoCFD pagoCFD : comprobantePagosCFD.getPagos()) {
            HashMap<String, ImpuestoAcumulado> impuestosTrasladadosP = new HashMap<>();

            Pago pago = pagos.addNewPago();
            Calendar fechaPago = new GregorianCalendar();
            fechaPago.setTime(pagoCFD.getFechaAplicacion().getTime());
            fechaPago.clear(Calendar.MILLISECOND);
            fechaPago.clear(Calendar.ZONE_OFFSET);
            pago.setFechaPago(fechaPago);
            pago.setFormaDePagoP(pagoCFD.getFormaDePagoP());
            pago.setMonedaP(pagoCFD.getMoneda());
            if (pagoCFD.getMoneda().toString().compareTo("MXN")==0)
                pago.setTipoCambioP(BigDecimal.ONE);
            else
                pago.setTipoCambioP(pagoCFD.getTipoDeCambio());
            pago.setMonto(pagoCFD.getMonto());
            if (pagoCFD.getNumOperacion()!=null&&!pagoCFD.getNumOperacion().isEmpty())
                pago.setNumOperacion(pagoCFD.getNumOperacion());
            if (pagoCFD.getRfcEmisorCtaBen()!=null&&!pagoCFD.getRfcEmisorCtaBen().isEmpty())
                pago.setRfcEmisorCtaBen(pagoCFD.getRfcEmisorCtaBen());
            if (pagoCFD.getCtaBeneficiario()!=null&&!pagoCFD.getCtaBeneficiario().isEmpty())
                pago.setCtaBeneficiario(pagoCFD.getCtaBeneficiario());

            // Se tiene que multiplicar el monto de pagos por el tipo de cambio porque los pagos pueden venir expresados en monedas diferentes
            montoTotalPagos = montoTotalPagos.add(Numero.getBigDecimal(
                    Numero.redondea(pagoCFD.getMonto().doubleValue() * pago.getTipoCambioP().doubleValue())));

            for (PagoDocumentoRelacionadoCFD documentoRelacionadoCFD : pagoCFD.getDocumentosRelacionados()) {
                Pago.DoctoRelacionado doctoRelacionado = pago.addNewDoctoRelacionado();
                doctoRelacionado.setIdDocumento(documentoRelacionadoCFD.getIdDocumento());
                if (documentoRelacionadoCFD.getSerie()!=null&&!documentoRelacionadoCFD.getSerie().isEmpty())
                    doctoRelacionado.setSerie(documentoRelacionadoCFD.getSerie());
                if (documentoRelacionadoCFD.getFolio()!=null&&!documentoRelacionadoCFD.getFolio().isEmpty())
                    doctoRelacionado.setFolio(documentoRelacionadoCFD.getFolio());
                doctoRelacionado.setMonedaDR(documentoRelacionadoCFD.getMoneda());
                if (pagoCFD.getMoneda().toString().compareTo(documentoRelacionadoCFD.getMoneda().toString())==0)
                    doctoRelacionado.setEquivalenciaDR(BigDecimal.ONE);
                else
                    doctoRelacionado.setEquivalenciaDR(documentoRelacionadoCFD.getEquivalencia());
                if (documentoRelacionadoCFD.getNumParcialidad()!=null)
                    doctoRelacionado.setNumParcialidad(documentoRelacionadoCFD.getNumParcialidad());
                if (documentoRelacionadoCFD.getImpSaldoAnt()!=null)
                    doctoRelacionado.setImpSaldoAnt(documentoRelacionadoCFD.getImpSaldoAnt());
                if (documentoRelacionadoCFD.getImpPagado()!=null)
                    doctoRelacionado.setImpPagado(documentoRelacionadoCFD.getImpPagado());
                if (documentoRelacionadoCFD.getImpSaldoInsoluto()!=null)
                    doctoRelacionado.setImpSaldoInsoluto(documentoRelacionadoCFD.getImpSaldoInsoluto());

                doctoRelacionado.setObjetoImpDR(documentoRelacionadoCFD.getObjetoImp());

                List<PagoDRImpuestoCFD> impuestosTrasladados = documentoRelacionadoCFD.getImpuestosTrasladados();
                if (impuestosTrasladados!=null&&!impuestosTrasladados.isEmpty()) {
                    Pago.DoctoRelacionado.ImpuestosDR impuestosDR = doctoRelacionado.addNewImpuestosDR();
                    Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR impuestosTrasladosDR = impuestosDR.addNewTrasladosDR();
                    for (PagoDRImpuestoCFD pagoDRImpuesto : impuestosTrasladados) {
                        Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR impuestoTrasladoDR = impuestosTrasladosDR.addNewTrasladoDR();
                        impuestoTrasladoDR.setBaseDR(pagoDRImpuesto.getBase());
                        impuestoTrasladoDR.setImporteDR(pagoDRImpuesto.getImporte());
                        impuestoTrasladoDR.setImpuestoDR(pagoDRImpuesto.getImpuesto());
                        impuestoTrasladoDR.setTasaOCuotaDR(pagoDRImpuesto.getTasaOCuota());
                        impuestoTrasladoDR.setTipoFactorDR(pagoDRImpuesto.getTipoFactor());

                        ImpuestoAcumulado impuestoAcumulado = new ImpuestoAcumulado();
                        impuestoAcumulado.impuesto = pagoDRImpuesto.getImpuesto();
                        impuestoAcumulado.tipofactor = pagoDRImpuesto.getTipoFactor();
                        impuestoAcumulado.tasa = pagoDRImpuesto.getTasaOCuota().doubleValue();
                        impuestoAcumulado.base = pagoDRImpuesto.getBase().doubleValue();
                        impuestoAcumulado.importe = pagoDRImpuesto.getImporte().doubleValue();
                        impuestoAcumulado.tipoCambio = doctoRelacionado.getEquivalenciaDR().doubleValue();

                        // Impuestos del Pago
                        ImpuestoAcumulado impuestoAcumuladoPago = impuestosTrasladadosP.get(impuestoAcumulado.getKey());
                        if (impuestoAcumuladoPago==null) {
                            impuestoAcumuladoPago = impuestoAcumulado;
                        } else {
                            impuestoAcumuladoPago.acumula(impuestoAcumulado);
                        }
                        impuestosTrasladadosP.put(impuestoAcumuladoPago.getKey(), impuestoAcumuladoPago);
                    }
                }
            }

            if (!impuestosTrasladadosP.isEmpty()) {
                Pago.ImpuestosP impuestosP = pago.addNewImpuestosP();
                Pago.ImpuestosP.TrasladosP impuestosPTrasladados = impuestosP.addNewTrasladosP();
                for (ImpuestoAcumulado impuestoAcumulado : impuestosTrasladadosP.values()) {
                    Pago.ImpuestosP.TrasladosP.TrasladoP trasladoP = impuestosPTrasladados.addNewTrasladoP();
                    if (Numero.redondea(impuestoAcumulado.tipoCambio)==1.0d) {
                        trasladoP.setBaseP(Numero.getBigDecimal(impuestoAcumulado.base));
                        trasladoP.setImporteP(Numero.getBigDecimal(impuestoAcumulado.importe));
                    } else {
                        //
                        // Actualmente el resultado obtenido para el atributo ImporteP y BaseP no se redondea, esto debido a que no se cuenta con algún fundamento o
                        // sustento sobe la lógica de redondeo. Le comento que hemos escalado este tema a una consulta más exhaustiva ya que como PAC debemos
                        // regirnos a lo que las autoridades establecen en los documentos oficiales.
                        //
                        double base = impuestoAcumulado.base / impuestoAcumulado.tipoCambio;
                        BigDecimal baseBD = new BigDecimal(base).setScale(2, BigDecimal.ROUND_DOWN);
                        log.debug("base: "+base+" new BigDecimal "+baseBD);

                        double importe = impuestoAcumulado.importe / impuestoAcumulado.tipoCambio;
                        BigDecimal importeBD = new BigDecimal(importe).setScale(2, BigDecimal.ROUND_DOWN);
                        log.debug("importe: "+importe+" new BigDecimal "+importeBD);

                        trasladoP.setBaseP(baseBD);
                        trasladoP.setImporteP(importeBD);

                        // Como se truncaron los resultados de base e importe es necesario
                        // Recalcular el impuesto con los nuevos importes obtenidos de la base y del importe
                        impuestoAcumulado.base = Numero.redondea(baseBD.doubleValue() * impuestoAcumulado.tipoCambio);
                        impuestoAcumulado.importe = Numero.redondea(importeBD.doubleValue() * impuestoAcumulado.tipoCambio);
                    }
                    trasladoP.setTasaOCuotaP(Numero.getBigDecimal(impuestoAcumulado.tasa, 6));
                    trasladoP.setImpuestoP(impuestoAcumulado.impuesto);
                    trasladoP.setTipoFactorP(impuestoAcumulado.tipofactor);

                    // Impuestos Totales
                    ImpuestoAcumulado impuestoAcumuladoTotal = impuestosTrasladadosT.get(impuestoAcumulado.getKey());
                    if (impuestoAcumuladoTotal==null) {
                        impuestoAcumuladoTotal = impuestoAcumulado;
                    } else {
                        impuestoAcumuladoTotal.acumula(impuestoAcumulado);
                    }
                    impuestosTrasladadosT.put(impuestoAcumuladoTotal.getKey(), impuestoAcumuladoTotal);
                }
            }
        }

        Pagos.Totales totales = pagos.addNewTotales();
        Pago[] pagosArray = pagos.getPagoArray();
        for (Pago pago : pagosArray) {
            Pago.ImpuestosP.TrasladosP.TrasladoP[] trasladosP = pago.getImpuestosP().getTrasladosP().getTrasladoPArray();
            for (Pago.ImpuestosP.TrasladosP.TrasladoP trasladoP : trasladosP) {
                // 002=IVA
                if (trasladoP.getImpuestoP().equals(CImpuesto.X_002)) {
                    int tasa = (int)Numero.redondea(trasladoP.getTasaOCuotaP().doubleValue() * 100.0d);
                    switch (tasa) {
                        case 0:
                            BigDecimal impuestoIVA0 = totales.getTotalTrasladosImpuestoIVA0();
                            if (impuestoIVA0==null)
                                impuestoIVA0 = BigDecimal.ZERO;
                            BigDecimal baseIVA0 = totales.getTotalTrasladosBaseIVA0();
                            if (baseIVA0==null)
                                baseIVA0 = BigDecimal.ZERO;

                            impuestoIVA0 = impuestoIVA0.add(Numero.getBigDecimal(Numero.redondea(trasladoP.getImporteP().doubleValue() * pago.getTipoCambioP().doubleValue())));
                            baseIVA0 = baseIVA0.add(Numero.getBigDecimal(Numero.redondea(trasladoP.getBaseP().doubleValue() * pago.getTipoCambioP().doubleValue())));

                            totales.setTotalTrasladosImpuestoIVA0(impuestoIVA0);
                            totales.setTotalTrasladosBaseIVA0(baseIVA0);
                            break;
                        case 8:
                            BigDecimal impuestoIVA8 = totales.getTotalTrasladosImpuestoIVA8();
                            if (impuestoIVA8==null)
                                impuestoIVA8 = BigDecimal.ZERO;
                            BigDecimal baseIVA8 = totales.getTotalTrasladosBaseIVA8();
                            if (baseIVA8==null)
                                baseIVA8 = BigDecimal.ZERO;

                            impuestoIVA8 = impuestoIVA8.add(Numero.getBigDecimal(Numero.redondea(trasladoP.getImporteP().doubleValue() * pago.getTipoCambioP().doubleValue())));
                            baseIVA8 = baseIVA8.add(Numero.getBigDecimal(Numero.redondea(trasladoP.getBaseP().doubleValue() * pago.getTipoCambioP().doubleValue())));

                            totales.setTotalTrasladosImpuestoIVA8(impuestoIVA8);
                            totales.setTotalTrasladosBaseIVA8(baseIVA8);
                            break;
                        case 16:
                            BigDecimal impuestoIVA16 = totales.getTotalTrasladosImpuestoIVA16();
                            if (impuestoIVA16==null)
                                impuestoIVA16 = BigDecimal.ZERO;
                            BigDecimal baseIVA16 = totales.getTotalTrasladosBaseIVA16();
                            if (baseIVA16==null)
                                baseIVA16 = BigDecimal.ZERO;

                            impuestoIVA16 = impuestoIVA16.add(Numero.getBigDecimal(Numero.redondea(trasladoP.getImporteP().doubleValue() * pago.getTipoCambioP().doubleValue())));
                            baseIVA16 = baseIVA16.add(Numero.getBigDecimal(Numero.redondea(trasladoP.getBaseP().doubleValue() * pago.getTipoCambioP().doubleValue())));

                            totales.setTotalTrasladosImpuestoIVA16(impuestoIVA16);
                            totales.setTotalTrasladosBaseIVA16(baseIVA16);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        /*if (!impuestosTrasladadosT.isEmpty()) {
            for (ImpuestoAcumulado impuestoAcumulado : impuestosTrasladadosT.values()) {
                // 002=IVA
                if (impuestoAcumulado.impuesto.equals(CImpuesto.X_002)) {
                    int tasa = (int)Numero.redondea(impuestoAcumulado.tasa * 100.0d);
                    switch (tasa) {
                        case 0:
                            totales.setTotalTrasladosImpuestoIVA0(Numero.getBigDecimal(impuestoAcumulado.importe / impuestoAcumulado.tipoCambio));
                            totales.setTotalTrasladosBaseIVA0(Numero.getBigDecimal(impuestoAcumulado.base / impuestoAcumulado.tipoCambio));
                            break;
                        case 8:
                            totales.setTotalTrasladosImpuestoIVA8(Numero.getBigDecimal(impuestoAcumulado.importe / impuestoAcumulado.tipoCambio));
                            totales.setTotalTrasladosBaseIVA8(Numero.getBigDecimal(impuestoAcumulado.base / impuestoAcumulado.tipoCambio));
                            break;
                        case 16:
                            totales.setTotalTrasladosImpuestoIVA16(Numero.getBigDecimal(impuestoAcumulado.importe / impuestoAcumulado.tipoCambio));
                            totales.setTotalTrasladosBaseIVA16(Numero.getBigDecimal(impuestoAcumulado.base / impuestoAcumulado.tipoCambio));
                            break;
                        default:
                            break;
                    }
                }
            }
        }*/
        totales.setMontoTotalPagos(montoTotalPagos);

        return cpd;
    }

    public void cargaComplementoCartaPorte(ComprobanteCFD comprobanteCFD, Comprobante comprobante, ComprobanteDocument cd) {
        XmlObject complementoObject = getComplementoCartaPorte(comprobanteCFD, comprobante);
        if (complementoObject==null)
            return;

        Complemento complemento = comprobante.addNewComplemento();
        complemento.set(complementoObject);
    }

    public CartaPorteDocument getComplementoCartaPorte(ComprobanteCFD comprobanteCFD, Comprobante comprobante) {
        CartaPorteDocument cpd = CartaPorteDocument.Factory.newInstance();

        CartaPorteCFD cartaPorteCFD = comprobanteCFD.getCartaPorte();

        CartaPorte cartaPorte = cpd.addNewCartaPorte();
        cartaPorte.setVersion("2.0");
        cartaPorte.setTranspInternac(cartaPorteCFD.getTranspInternac());
        if (cartaPorteCFD.getEntradaSalida()!=null)
            cartaPorte.setEntradaSalidaMerc(cartaPorteCFD.getEntradaSalida());
        if (cartaPorteCFD.getViaEntradaSalida()!=null)
            cartaPorte.setViaEntradaSalida(cartaPorteCFD.getViaEntradaSalida());

        double totalDistanciaRecorrida = 0.0d;

        CartaPorte.Ubicaciones ubicaciones = cartaPorte.addNewUbicaciones();
        for (Object object : cartaPorteCFD.getUbicaciones()) {
            CartaPorteUbicacionCFD cartaPorteUbicacionCFD = (CartaPorteUbicacionCFD)object;

            if (cartaPorteUbicacionCFD.getDistanciaRecorrida()!=null)
                totalDistanciaRecorrida += cartaPorteUbicacionCFD.getDistanciaRecorrida();

            CartaPorte.Ubicaciones.Ubicacion ubicacion = ubicaciones.addNewUbicacion();
            ubicacion.setTipoUbicacion(cartaPorteUbicacionCFD.getTipoUbicacion());
            if (cartaPorteUbicacionCFD.getIDUbicacion()!=null)
                ubicacion.setIDUbicacion(cartaPorteUbicacionCFD.getIDUbicacion());
            if (cartaPorteUbicacionCFD.getRFCRemitenteDestinatario()!=null)
                ubicacion.setRFCRemitenteDestinatario(cartaPorteUbicacionCFD.getRFCRemitenteDestinatario());
            if (cartaPorteUbicacionCFD.getNombreRFC()!=null)
                ubicacion.setNombreRemitenteDestinatario(cartaPorteUbicacionCFD.getNombreRFC());
            ubicacion.setFechaHoraSalidaLlegada(cartaPorteUbicacionCFD.getFechaHoraSalidaLlegada());
            if (cartaPorteUbicacionCFD.getDistanciaRecorrida()!=null)
                ubicacion.setDistanciaRecorrida(Numero.getBigDecimal(cartaPorteUbicacionCFD.getDistanciaRecorrida()));
            if (cartaPorteUbicacionCFD.getTipoEstacion()!=null)
                ubicacion.setTipoEstacion(cartaPorteUbicacionCFD.getTipoEstacion());

            CartaPorteDomicilioCFD domicilioCFD = cartaPorteUbicacionCFD.getDomicilio();

            CartaPorte.Ubicaciones.Ubicacion.Domicilio domicilio = ubicacion.addNewDomicilio();
            domicilio.setCalle(domicilioCFD.getCalle());
            if (domicilioCFD.getNoExterior()!=null&&!domicilioCFD.getNoExterior().isEmpty())
                domicilio.setNumeroExterior(domicilioCFD.getNoExterior());
            if (domicilioCFD.getNoInterior()!=null&&!domicilioCFD.getNoInterior().isEmpty())
                domicilio.setNumeroInterior(domicilioCFD.getNoInterior());
            domicilio.setColonia(domicilioCFD.getColonia());
            if (domicilioCFD.getLocalidad()!=null&&!domicilioCFD.getLocalidad().isEmpty())
                domicilio.setLocalidad(domicilioCFD.getLocalidad());
            if (domicilioCFD.getReferencia()!=null&&!domicilioCFD.getReferencia().isEmpty())
                domicilio.setReferencia(domicilioCFD.getReferencia());
            domicilio.setMunicipio(domicilioCFD.getMunicipio());
            domicilio.setEstado(domicilioCFD.getEstado());
            domicilio.setPais(domicilioCFD.getPais());
            domicilio.setCodigoPostal(domicilioCFD.getCodigoPostal());
        }

        cartaPorte.setTotalDistRec(Numero.getBigDecimal(totalDistanciaRecorrida));

        double pesoBrutoTotal = 0.0;

        CartaPorte.Mercancias mercancias = cartaPorte.addNewMercancias();
        for (Object object : cartaPorteCFD.getMercancias()) {
            CartaPorteMercanciaCFD cartaPorteMercanciaCFD = (CartaPorteMercanciaCFD)object;

            pesoBrutoTotal += cartaPorteMercanciaCFD.getPesoEnKg();

            CartaPorte.Mercancias.Mercancia mercancia = mercancias.addNewMercancia();
            mercancia.setBienesTransp(cartaPorteMercanciaCFD.getBienesTransp());
            if (cartaPorteMercanciaCFD.getClaveSTCC()!=null)
                mercancia.setClaveSTCC(cartaPorteMercanciaCFD.getClaveSTCC());
            mercancia.setDescripcion(cartaPorteMercanciaCFD.getDescripcion());
            mercancia.setCantidad(Numero.getBigDecimal(cartaPorteMercanciaCFD.getCantidad()));
            mercancia.setClaveUnidad(cartaPorteMercanciaCFD.getClaveUnidad());
            mercancia.setUnidad(cartaPorteMercanciaCFD.getUnidad());
            if (cartaPorteMercanciaCFD.getDimensiones()!=null)
                mercancia.setDimensiones(cartaPorteMercanciaCFD.getDimensiones());
            if (cartaPorteMercanciaCFD.getMaterialPeligroso()!=null)
                mercancia.setMaterialPeligroso(cartaPorteMercanciaCFD.getMaterialPeligroso());
            if (cartaPorteMercanciaCFD.getCveMaterialPeligroso()!=null)
                mercancia.setCveMaterialPeligroso(cartaPorteMercanciaCFD.getCveMaterialPeligroso());
            if (cartaPorteMercanciaCFD.getEmbalaje()!=null)
                mercancia.setEmbalaje(cartaPorteMercanciaCFD.getEmbalaje());
            if (cartaPorteMercanciaCFD.getDescripEmbalaje()!=null)
                mercancia.setDescripEmbalaje(cartaPorteMercanciaCFD.getDescripEmbalaje());
            mercancia.setPesoEnKg(Numero.getBigDecimal(cartaPorteMercanciaCFD.getPesoEnKg()));
            if (cartaPorteMercanciaCFD.getValorMercancia()!=null) {
                mercancia.setValorMercancia(Numero.getBigDecimal(cartaPorteMercanciaCFD.getValorMercancia()));
                mercancia.setMoneda(cartaPorteMercanciaCFD.getMoneda());
            }
            if (cartaPorteMercanciaCFD.getFraccionArancelaria()!=null)
                mercancia.setFraccionArancelaria(cartaPorteMercanciaCFD.getFraccionArancelaria());
            if (cartaPorteMercanciaCFD.getUUIDComercioExt()!=null)
                mercancia.setUUIDComercioExt(cartaPorteMercanciaCFD.getUUIDComercioExt());

            List lCantidadTransporta = cartaPorteMercanciaCFD.getCantidadTransporta();
            if (lCantidadTransporta!=null&&lCantidadTransporta.size()>0) {
                for (Object object1 : cartaPorteMercanciaCFD.getCantidadTransporta()) {
                    CartaPorteCantidadTransportaCFD cartaPorteCantidadTransportaCFD = (CartaPorteCantidadTransportaCFD)object1;

                    CartaPorte.Mercancias.Mercancia.CantidadTransporta cantidadTransporta = mercancia.addNewCantidadTransporta();
                    cantidadTransporta.setCantidad(Numero.getBigDecimal(cartaPorteCantidadTransportaCFD.getCantidad()));
                    cantidadTransporta.setIDOrigen(cartaPorteCantidadTransportaCFD.getIDOrigen());
                    cantidadTransporta.setIDDestino(cartaPorteCantidadTransportaCFD.getIDDestino());
                }
            }
        }

        CartaPorteAutotransporteCFD cartaPorteAutotransporteCFD = cartaPorteCFD.getAutotransporte();
        if (cartaPorteAutotransporteCFD!=null) {
            CartaPorte.Mercancias.Autotransporte autotransporte = mercancias.addNewAutotransporte();
            autotransporte.setPermSCT(cartaPorteAutotransporteCFD.getPermSCT());
            autotransporte.setNumPermisoSCT(cartaPorteAutotransporteCFD.getNumPermisoSCT());

            CartaPorte.Mercancias.Autotransporte.IdentificacionVehicular identificacionVehicular = autotransporte.addNewIdentificacionVehicular();
            identificacionVehicular.setConfigVehicular(cartaPorteAutotransporteCFD.getConfigVehicular());
            identificacionVehicular.setPlacaVM(cartaPorteAutotransporteCFD.getPlacaVM());
            identificacionVehicular.setAnioModeloVM(cartaPorteAutotransporteCFD.getAnioModeloVM());

            for (Object object : cartaPorteAutotransporteCFD.getSeguros()) {
                CartaPorteAutotransporteSeguroCFD cartaPorteAutotransporteSeguroCFD = (CartaPorteAutotransporteSeguroCFD)object;

                CartaPorte.Mercancias.Autotransporte.Seguros seguros = autotransporte.addNewSeguros();
                if (cartaPorteAutotransporteSeguroCFD.getAseguraRespCivil()!=null)
                    seguros.setAseguraRespCivil(cartaPorteAutotransporteSeguroCFD.getAseguraRespCivil());
                if (cartaPorteAutotransporteSeguroCFD.getPolizaRespCivil()!=null)
                    seguros.setPolizaRespCivil(cartaPorteAutotransporteSeguroCFD.getPolizaRespCivil());
                if (cartaPorteAutotransporteSeguroCFD.getAseguraMedAmbiente()!=null)
                    seguros.setAseguraMedAmbiente(cartaPorteAutotransporteSeguroCFD.getAseguraMedAmbiente());
                if (cartaPorteAutotransporteSeguroCFD.getPolizaMedAmbiente()!=null)
                    seguros.setPolizaMedAmbiente(cartaPorteAutotransporteSeguroCFD.getPolizaMedAmbiente());
                if (cartaPorteAutotransporteSeguroCFD.getAseguraCarga()!=null)
                    seguros.setAseguraCarga(cartaPorteAutotransporteSeguroCFD.getAseguraCarga());
                if (cartaPorteAutotransporteSeguroCFD.getPolizaCarga()!=null)
                    seguros.setPolizaCarga(cartaPorteAutotransporteSeguroCFD.getPolizaCarga());
                seguros.setPrimaSeguro(Numero.getBigDecimal(cartaPorteAutotransporteSeguroCFD.getPrimaSeguro()));
            }

            if (cartaPorteAutotransporteCFD.getRemolques()!=null&&cartaPorteAutotransporteCFD.getRemolques().size()>0) {
                CartaPorte.Mercancias.Autotransporte.Remolques remolques = autotransporte.addNewRemolques();
                for (Object object : cartaPorteAutotransporteCFD.getRemolques()) {
                    CartaPorteAutotransporteRemolqueCFD cartaPorteAutotransporteRemolqueCFD = (CartaPorteAutotransporteRemolqueCFD)object;

                    CartaPorte.Mercancias.Autotransporte.Remolques.Remolque remolque =  remolques.addNewRemolque();
                    remolque.setPlaca(cartaPorteAutotransporteRemolqueCFD.getPlaca());
                    remolque.setSubTipoRem(cartaPorteAutotransporteRemolqueCFD.getSubTipoRem());
                }
            }
        }

        mercancias.setPesoBrutoTotal(Numero.getBigDecimal(pesoBrutoTotal));
        mercancias.setNumTotalMercancias(cartaPorteCFD.getMercancias().size());
        if (cartaPorteCFD.getUnidadPeso()!=null)
            mercancias.setUnidadPeso(cartaPorteCFD.getUnidadPeso());
        if (cartaPorteCFD.getPesoBrutoTotal()!=null)
            mercancias.setPesoBrutoTotal(Numero.getBigDecimal(cartaPorteCFD.getPesoBrutoTotal()));
        if (cartaPorteCFD.getPesoNetoTotal()!=null)
            mercancias.setPesoNetoTotal(Numero.getBigDecimal(cartaPorteCFD.getPesoNetoTotal()));

        CartaPorteFiguraTransporteCFD cartaPorteFiguraTransporteCFD = cartaPorteCFD.getFiguraTransporte();
        if (cartaPorteFiguraTransporteCFD!=null) {
            CartaPorte.FiguraTransporte figuraTransporte = cartaPorte.addNewFiguraTransporte();

            for (Object object : cartaPorteFiguraTransporteCFD.getTiposFigura()) {
                CartaPorteTipoFiguraTransporteCFD cartaPorteTipoFiguraTransporteCFD = (CartaPorteTipoFiguraTransporteCFD)object;

                CartaPorte.FiguraTransporte.TiposFigura tiposFigura = figuraTransporte.addNewTiposFigura();
                tiposFigura.setTipoFigura(cartaPorteTipoFiguraTransporteCFD.getTipoFigura());
                tiposFigura.setRFCFigura(cartaPorteTipoFiguraTransporteCFD.getRFCFigura());
                if (cartaPorteTipoFiguraTransporteCFD.getNumLicencia()!=null)
                    tiposFigura.setNumLicencia(cartaPorteTipoFiguraTransporteCFD.getNumLicencia());
                if (cartaPorteTipoFiguraTransporteCFD.getNombreFigura()!=null)
                    tiposFigura.setNombreFigura(cartaPorteTipoFiguraTransporteCFD.getNombreFigura());
                if (cartaPorteTipoFiguraTransporteCFD.getNumRegIdTribFigura()!=null)
                    tiposFigura.setNumRegIdTribFigura(cartaPorteTipoFiguraTransporteCFD.getNumRegIdTribFigura());
                if (cartaPorteTipoFiguraTransporteCFD.getResidenciaFiscalFigura()!=null)
                    tiposFigura.setResidenciaFiscalFigura(cartaPorteTipoFiguraTransporteCFD.getResidenciaFiscalFigura());

                CartaPorteDomicilioCFD domicilioCFD = cartaPorteTipoFiguraTransporteCFD.getDomicilio();

                /*
                CartaPorte.FiguraTransporte.TiposFigura.Domicilio domicilio = tiposFigura.addNewDomicilio();
                domicilio.setCalle(domicilioCFD.getCalle());
                if (domicilioCFD.getNoExterior()!=null&&!domicilioCFD.getNoExterior().isEmpty())
                    domicilio.setNumeroExterior(domicilioCFD.getNoExterior());
                if (domicilioCFD.getNoInterior()!=null&&!domicilioCFD.getNoInterior().isEmpty())
                    domicilio.setNumeroInterior(domicilioCFD.getNoInterior());
                domicilio.setColonia(domicilioCFD.getColonia());
                if (domicilioCFD.getLocalidad()!=null&&!domicilioCFD.getLocalidad().isEmpty())
                    domicilio.setLocalidad(domicilioCFD.getLocalidad());
                if (domicilioCFD.getReferencia()!=null&&!domicilioCFD.getReferencia().isEmpty())
                    domicilio.setReferencia(domicilioCFD.getReferencia());
                domicilio.setMunicipio(domicilioCFD.getMunicipio());
                domicilio.setEstado(domicilioCFD.getEstado());
                domicilio.setPais(domicilioCFD.getPais());
                domicilio.setCodigoPostal(domicilioCFD.getCodigoPostal());
                */
            }
        }

        //
        if (comprobante.getMoneda().toString().compareTo("XXX")==0) {
            comprobante.setSubTotal((Numero.getBigDecimal(comprobante.getSubTotal().doubleValue(), 0)));
            comprobante.setTotal((Numero.getBigDecimal(comprobante.getTotal().doubleValue(), 0)));
        }
        //

        return cpd;
    }

    public ComprobanteDocument timbraComprobante(TimbradoCFD timbrado, String cfdi) throws Exception {
        log.debug("Timbrando ... ");

        RespuestaPAC respuesta = timbrado.getCFDI("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+cfdi);
        return ComprobanteDocument.Factory.parse(respuesta.getCfdi());
    }

    //private String generaCadenaOriginalCartaPorte(ComprobanteDocument cd) throws Exception {
    //    StringWriter sw = new StringWriter();
    //    transformerCartaPorte.transform(new StreamSource(cd.newInputStream()), new StreamResult(sw));
    //    String cadenaOriginalCartaPorte = sw.toString();
    //    return cadenaOriginalCartaPorte;
    //}

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
