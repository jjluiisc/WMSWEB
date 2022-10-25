package mx.reder.wms.reports;

import com.atcloud.dao.engine.DatabaseServices;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.atcloud.util.F;
import com.atcloud.util.Fecha;
import com.atcloud.util.Letras;
import com.atcloud.util.Numero;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.imageio.ImageIO;
import mx.gob.sat.cartaPorte20.CartaPorteDocument;
import mx.gob.sat.cartaPorte20.CartaPorteDocument.CartaPorte;
import mx.gob.sat.cfd.x4.ComprobanteDocument;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.CfdiRelacionados;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x4.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.pagos20.PagosDocument;
import mx.gob.sat.pagos20.PagosDocument.Pagos;
import mx.gob.sat.pagos20.PagosDocument.Pagos.Pago;
import mx.gob.sat.timbreFiscalDigital.TimbreFiscalDigitalDocument;
import mx.gob.sat.timbreFiscalDigital.TimbreFiscalDigitalDocument.TimbreFiscalDigital;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoDeComprobante;
import mx.reder.wms.cfdi.entity.InformacionAduaneraCFD;
import mx.reder.wms.dao.entity.ASPELClienteDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDAO;
import mx.reder.wms.dao.entity.ASPELInformacionEnvioDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDAO;
import mx.reder.wms.dao.entity.ASPELVendedorDAO;
import mx.reder.wms.dao.entity.ColoniaSATDAO;
import mx.reder.wms.dao.entity.EntidadFederativaDAO;
import mx.reder.wms.dao.entity.LocalidadSATDAO;
import mx.reder.wms.dao.entity.MunicipioSATDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.RutaFacturaDAO;
import mx.reder.wms.to.ASPELFacturaDetalleTO;
import mx.reder.wms.util.Configuracion;
import org.apache.log4j.Logger;

public class PDFFactoryFacturaImp extends PdfPageEventHelper implements PDFFactory {
    static Logger log = Logger.getLogger(PDFFactoryFacturaImp.class.getName());

    private DatabaseServices ds = null;
    private String cadenaOriginal = null;
    private String qr = null;
    public BaseFont baseFont = null;
    public BaseFont baseFontImp = null;
    public Font header0 = null;
    public Font header0Bold = null;
    public Font header = null;
    public Font headerBold = null;
    public Font header2 = null;
    public Font header2Bold = null;
    public Font header2BoldWhite = null;
    public Font header4 = null;
    public Font detalle = null;
    public Font detalleBold = null;
    public PdfWriter writer = null;
    public PdfContentByte canvas = null;
    public Document document = null;
    public int paginaNumero = 0, totalPaginas = 0;
    public int detalleNo = 0;
    public float detalleWidth = 0;
    public float lineOffset = 0;
    public final float lineOffsetPagina = 650f;
    public final float footerOffset = 280f;
    public final int descripcionLongitudMaxima = 45;
    public final int lineasPorDetalle = 4;
    public Color clearTransparent = null;
    public Color clearGray = null;
    public Color colorWhite = null;
    public Color colorBlack = null;
    private RutaFacturaDAO rutaFacturaDAO;
    private OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO;
    private ASPELPedidoDAO aspelPedidoDAO;
    private ASPELVendedorDAO aspelVendedorDAO;
    private ASPELFacturaDAO aspelFacturaDAO;
    private ASPELClienteDAO aspelClienteDAO;
    private ASPELInformacionEnvioDAO aspelInformacionEnvioDAO;
    private ArrayList<ASPELFacturaDetalleTO> detallesFactura;

    @Override
    public void setup(String fontPath) throws Exception {
        log.info("Iniciando PDFFactory ...");

        String font = fontPath==null ? BaseFont.HELVETICA : fontPath;
        log.debug(font);

        baseFont = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, BaseFont.EMBEDDED);
        baseFontImp = BaseFont.createFont(font, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        header0 = new Font(baseFontImp, 12f, Font.NORMAL);
        header0Bold = new Font(baseFontImp, 12f, Font.BOLD);
        header = new Font(baseFontImp, 9f, Font.NORMAL);
        headerBold = new Font(baseFontImp, 9f, Font.BOLD);
        header2 = new Font(baseFontImp, 8f, Font.NORMAL);
        header2Bold = new Font(baseFontImp, 8f, Font.BOLD);
        header2BoldWhite = new Font(baseFontImp, 8f, Font.BOLD, Color.WHITE);
        header4 = new Font(baseFontImp, 4f, Font.NORMAL);
        detalle = new Font(baseFont, 7f, Font.NORMAL);
        detalleBold = new Font(baseFont, 7f, Font.BOLD);
        clearTransparent = new Color(0xFF, 0xFF, 0xFF, 0x00);
        clearGray = new Color(0xDC, 0xDD, 0xE3);
        colorWhite = new Color(0xFF, 0xFF, 0xFF);
        colorBlack = new Color(0x35, 0x35, 0x35);

        log.info("Listo");
    }

    private String getFont() {
        File filefont = new File(Configuracion.getInstance().getProperty("ruta.font"));
        if (filefont.exists())
            return filefont.getAbsolutePath();
        return null;
    }

    @Override
    public void terminate() {
        log.info("Fin");
    }

    @Override
    public byte[] genera(DatabaseServices ds, ComprobanteDocument cd, TimbreFiscalDigitalDocument tfd, CartaPorteDocument cpd, PagosDocument pd,
            String cadenaOriginal, String qr, RutaFacturaDAO rutaFacturaDAO, OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO,
            ASPELPedidoDAO aspelPedidoDAO, ASPELVendedorDAO aspelVendedorDAO, ASPELFacturaDAO aspelFacturaDAO, ASPELClienteDAO aspelClienteDAO,
            ASPELInformacionEnvioDAO aspelInformacionEnvioDAO, ArrayList<ASPELFacturaDetalleTO> detallesFactura) throws Exception {
        this.ds = ds;
        this.cadenaOriginal = cadenaOriginal;
        this.qr = qr;

        this.rutaFacturaDAO = rutaFacturaDAO;
        this.ordenSurtidoPedidoDAO = ordenSurtidoPedidoDAO;
        this.aspelPedidoDAO = aspelPedidoDAO;
        this.aspelVendedorDAO = aspelVendedorDAO;
        this.aspelFacturaDAO = aspelFacturaDAO;
        this.aspelClienteDAO = aspelClienteDAO;
        this.aspelInformacionEnvioDAO = aspelInformacionEnvioDAO;
        this.detallesFactura = detallesFactura;

        Comprobante comprobante = cd.getComprobante();
        TimbreFiscalDigital timbreFiscal = tfd.getTimbreFiscalDigital();
        CartaPorte cartaPorte = cpd == null ? null : cpd.getCartaPorte();
        Pagos pagos = pd == null ? null : pd.getPagos();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document = new Document(PageSize.A4, 0, 0, 0, 0);
        writer = PdfWriter.getInstance(document, out);
        writer.setPageEvent(this);

        document.open();
        canvas = writer.getDirectContent();

        lineOffset = lineOffsetPagina;
        detalleWidth = 8f;

        //Concepto[] conceptos = comprobante.getConceptos().getConceptoArray();
        //conceptos = resizeArray(conceptos, 30);

        int lineasAdicionalesDescripcion = 0;
        for (ASPELFacturaDetalleTO detalleFactura : detallesFactura) {
            String[] tokensDescripcion = splitDescripcion(detalleFactura.getDescripcion());
            if (tokensDescripcion.length > 1) {
                lineasAdicionalesDescripcion += tokensDescripcion.length - 1;
            }
        }

        // Lineas Adicionales por la seccion de Total
        lineasAdicionalesDescripcion += 13;

        if (cpd!=null) {
            // Test pagination
            //resizeArrayMercancia(cpd, 1);
            //resizeArrayMercancia(cpd, 2);
            //resizeArrayMercancia(cpd, 3);
            //resizeArrayMercancia(cpd, 6);
            //resizeArrayMercancia(cpd, 7); // *
            //resizeArrayMercancia(cpd, 8);
            //resizeArrayMercancia(cpd, 9);
            //resizeArrayMercancia(cpd, cpd.getCartaPorte().getMercancias().getMercanciaArray().length - 4);
            //resizeArrayMercancia(cpd, cpd.getCartaPorte().getMercancias().getMercanciaArray().length - 5);
            //resizeArrayMercancia(cpd, cpd.getCartaPorte().getMercancias().getMercanciaArray().length - 6);

            // Complemento Carta Porte
            lineasAdicionalesDescripcion += 4;
            lineasAdicionalesDescripcion += (cpd.getCartaPorte().getUbicaciones().getUbicacionArray().length * 8);
            lineasAdicionalesDescripcion += (cpd.getCartaPorte().getMercancias().getMercanciaArray().length * 9);
            // Autotransporte
            lineasAdicionalesDescripcion += 15;
            // Figura Transporte
            lineasAdicionalesDescripcion += 5;
        }

        if (pd!=null) {
            // Test pagination
            //resizeArrayPagos(pd, 3);
            //resizeArrayPagos(pd, 10);

            // Al inicio del Pago
            lineasAdicionalesDescripcion ++;
            for (Pago pago : pagos.getPagoArray()) {
                // Por cada Pago
                lineasAdicionalesDescripcion += 4;
                // Por cada Documento Relacionado
                Pagos.Pago.DoctoRelacionado[] pagoDRItems = pago.getDoctoRelacionadoArray();
                lineasAdicionalesDescripcion += (6 * pagoDRItems.length);
            }
        }

        //
        // Paginas
        //
        float lineasAdicionalesDescripcionHeigth = lineasAdicionalesDescripcion * detalleWidth;
        float lineTotalHeigth = (detallesFactura.size() * (detalleWidth * lineasPorDetalle))+lineasAdicionalesDescripcionHeigth;
        lineTotalHeigth += (detallesFactura.size() * 5f);

        double resultado = ((lineTotalHeigth) / (lineOffset - footerOffset));
        double fraccion = Numero.redondea2(resultado - ((int) resultado));
        totalPaginas = (int) resultado;
        if (fraccion > 0.0) {
            totalPaginas++;
        }

        paginaNumero = 0;
        detalleNo = 0;

        //
        //
        //
        printHeader(comprobante, timbreFiscal);
        for (ASPELFacturaDetalleTO detalleFactura : detallesFactura) {
            printDetalle(comprobante, timbreFiscal, cartaPorte, detalleFactura);
            if (lineOffset < footerOffset) {
                printPaginaNueva(comprobante, timbreFiscal);
            }
        }
        if (cpd!=null) {
            printCartaPorte(comprobante, timbreFiscal, cartaPorte);
        }
        if (pd!=null) {
            printPagos(comprobante, timbreFiscal, pagos);
        }
        printTotales(comprobante, timbreFiscal);
        printCfdiRelacionados(comprobante);
        printSelloYCadenaOriginal(comprobante, timbreFiscal);
        printFooter();

        document.close();

        if (paginaNumero!=totalPaginas) {
            log.info("lineOffset = ["+lineOffset+"] footerOffset = ["+footerOffset+"]");
            log.info("lineasAdicionalesDescripcion = ["+lineasAdicionalesDescripcion+"] lineasAdicionalesDescripcionHeigth = ["+lineasAdicionalesDescripcionHeigth+"]");
            log.info("lineTotalHeigth = ["+lineTotalHeigth+"]");
            log.info("lineas = ["+detallesFactura.size()+"] paginacion = ["+paginaNumero+" / "+totalPaginas+"]");
            log.info("((lineTotalHeigth+cadenaOriginalTotalHeigth) / (lineOffset - footerOffset)) = ["+resultado+"]");
            log.info("resultado = ["+resultado+"] fraccion = ["+fraccion+"] totalPaginas = ["+totalPaginas+"]");

            log.info("Error de paginacion ["+comprobante.getEmisor().getRfc()+";"+comprobante.getSerie()+";"+comprobante.getFolio()+"] "
                   +"paginaNumero = "+paginaNumero+"!=totalPaginas = "+totalPaginas+"");
        }

        return out.toByteArray();
    }

    // <editor-fold defaultstate="collapsed" desc="resize, write, draw, addChunk, printHeader methods. Click on the+sign on the left to edit the code.">

    private Concepto[] resizeArray(Concepto[] conceptos, int n) {
        ArrayList<Concepto> array = new ArrayList();
        for (int i = 0; i < n; i++) {
            Concepto _concepto = conceptos[i % conceptos.length];
            Concepto concepto = Concepto.Factory.newInstance();
            concepto.setNoIdentificacion(_concepto.getNoIdentificacion());
            concepto.setDescripcion(_concepto.getDescripcion());
            concepto.setClaveProdServ(_concepto.getClaveProdServ());
            concepto.setClaveUnidad(_concepto.getClaveUnidad());
            concepto.setCantidad(_concepto.getCantidad());
            concepto.setValorUnitario(_concepto.getValorUnitario());
            concepto.setImporte(_concepto.getImporte());
            concepto.setDescuento(_concepto.getDescuento());
            concepto.setImpuestos(_concepto.getImpuestos());
            /*if ((i%3)==0||(i%7)==0) {
                StringBuilder sb = new StringBuilder();
                String descripcion = concepto.getDescripcion();
                for (int j=0; j<200; j++) {
                    sb.append(j).append(" ");
                }
                concepto.setDescripcion(sb.toString());
            }*/
            concepto.setCantidad(Numero.getBigDecimal((double) (i+1)));
            array.add(concepto);
        }
        Concepto[] toArray = new Concepto[array.size()];
        array.toArray(toArray);
        return toArray;
    }

    public void resizeArrayPagos(PagosDocument pd, int n) {
        Pago pagos[] = pd.getPagos().getPagoArray();
        ArrayList<Pago> array = new ArrayList();
        for (int i = 0; i < n; i++) {
            Pago _pago = pagos[i % pagos.length];
            Pago pago = Pago.Factory.newInstance();
            pago.setCadPago(_pago.getCadPago());
            pago.setCertPago(_pago.getCertPago());
            pago.setCtaBeneficiario(_pago.getCtaBeneficiario());
            pago.setCtaOrdenante(_pago.getCtaOrdenante());
            pago.setDoctoRelacionadoArray(_pago.getDoctoRelacionadoArray());
            /*if ((i%2)==0) {
                Pago.DoctoRelacionado[] _arrayDR = _pago.getDoctoRelacionadoArray();
                ArrayList<Pago.DoctoRelacionado> arrayDR = new ArrayList<>();
                for (int j = 0; j < 10; j++) {
                    arrayDR.add(_arrayDR[j % _arrayDR.length]);
                }
                Pago.DoctoRelacionado[] toArray = new Pago.DoctoRelacionado[arrayDR.size()];
                arrayDR.toArray(toArray);
                _pago.setDoctoRelacionadoArray(toArray);
            }*/
            pago.setFechaPago(_pago.getFechaPago());
            pago.setFormaDePagoP(_pago.getFormaDePagoP());
            pago.setImpuestosP(_pago.getImpuestosP());
            pago.setMonedaP(_pago.getMonedaP());
            pago.setMonto(_pago.getMonto());
            pago.setNomBancoOrdExt(_pago.getNomBancoOrdExt());
            pago.setNumOperacion(_pago.getNumOperacion());
            pago.setRfcEmisorCtaBen(_pago.getRfcEmisorCtaBen());
            pago.setRfcEmisorCtaOrd(_pago.getRfcEmisorCtaOrd());
            pago.setSelloPago(_pago.getSelloPago());
            pago.setTipoCadPago(_pago.getTipoCadPago());
            pago.setTipoCambioP(_pago.getTipoCambioP());

            array.add(pago);
        }
        Pago[] toArray = new Pago[array.size()];
        array.toArray(toArray);
        pd.getPagos().setPagoArray(toArray);
    }

    public void resizeArrayMercancia(CartaPorteDocument cpd, int cuantos) {
        CartaPorte.Mercancias.Mercancia[] mercancias = cpd.getCartaPorte().getMercancias().getMercanciaArray();
        if (mercancias.length < cuantos) {
            return;
        }
        do {
            cpd.getCartaPorte().getMercancias().removeMercancia(mercancias.length - 1);
            mercancias = cpd.getCartaPorte().getMercancias().getMercanciaArray();
        } while (mercancias.length > cuantos);
    }

    public String[] resizeArrayCO(Object[] items, int n) {
        ArrayList array = new ArrayList();
        array.add("BEGIN =>");
        for (int i = 0; i < n; i++) {
            array.add(items[i % items.length]);
        }
        array.add("<= END");
        String[] toArray = new String[array.size()];
        array.toArray(toArray);
        return toArray;
    }

    private void writeParagraph(String text, float llx, float lly, float urx, float ury) throws Exception {
        writeParagraph(text, headerBold, llx, lly, urx, ury, Element.ALIGN_LEFT);
    }

    private void writeParagraph(String text, Font font, float llx, float lly, float urx, float ury) throws Exception {
        writeParagraph(text, font, llx, lly, urx, ury, Element.ALIGN_LEFT);
    }

    private void writeParagraph(String text, Font font, float llx, float lly, float urx, float ury, int alignment) throws Exception {
        Paragraph idParagraph = new Paragraph();
        idParagraph.setLeading(0f);
        idParagraph.setFont(font);
        idParagraph.setAlignment(alignment);

        addChunk(idParagraph, text);

        writeElement(idParagraph, llx, lly, urx, ury);
    }

    public void writeText(String text, float size, float x, float y) throws Exception {
        writeText(Element.ALIGN_LEFT, text, size, x, y);
    }

    public void writeText(int alignment, String text, float size, float x, float y) throws Exception {
        writeText(alignment, text, baseFont, size, x, y);
    }

    public void writeText(int alignment, String text, BaseFont font, float size, float x, float y) throws Exception {
        canvas.beginText();
        canvas.setFontAndSize(font, size);
        canvas.showTextAligned(alignment, text, x, y, 0);
        canvas.endText();
    }

    public void drawRectangle(float llx, float lly, float urx, float ury) throws Exception {
        canvas.saveState();
        canvas.setGrayFill(0.9f);
        canvas.rectangle(llx, lly, urx - llx, ury - lly);
        canvas.fillStroke();
        canvas.restoreState();
    }

    public void drawRectangleStroke(float llx, float lly, float urx, float ury, Color colorStroke) throws Exception {
        canvas.saveState();
        canvas.setLineWidth(0.5f);
        canvas.setColorStroke(colorStroke);
        canvas.rectangle(llx, lly, urx - llx, ury - lly);
        canvas.stroke();
        canvas.restoreState();
    }

    public void drawRectangleStroke(float llx, float lly, float urx, float ury, Color colorStroke, Color colorFill) throws Exception {
        canvas.saveState();
        canvas.setLineWidth(0.5f);
        canvas.setColorStroke(colorStroke);
        canvas.setColorFill(colorFill);
        canvas.rectangle(llx, lly, urx - llx, ury - lly);
        canvas.fillStroke();
        canvas.restoreState();
    }

    public void writeElement(Element element, float llx, float lly, float urx, float ury) throws Exception {
        writeElement(Element.ALIGN_LEFT, element, llx, lly, urx, ury);
    }

    public void writeElement(Element element, float llx, float lly, float urx, float ury, boolean rectangle) throws Exception {
        if (rectangle) {
            drawRectangle(llx, lly, urx, ury);
        }
        writeElement(Element.ALIGN_LEFT, element, llx, lly, urx, ury);
    }

    public void writeElement(int alignment, Element element, float llx, float lly, float urx, float ury) throws Exception {
        ColumnText ct = new ColumnText(writer.getDirectContent());
        ct.setAlignment(alignment);
        ct.addElement(element);
        ct.setSimpleColumn(llx, lly, urx, ury);
        ct.go();
    }

    public void addChunk(Paragraph paragraph, String text) throws Exception {
        addChunk(paragraph, text, paragraph.getFont(), 1000f);
    }

    public void addChunk(Paragraph paragraph, String text, Font font) throws Exception {
        addChunk(paragraph, text, font, 1000f);
    }

    public void addChunk(Paragraph paragraph, String text, float width) throws Exception {
        addChunk(paragraph, text, paragraph.getFont(), width);
    }

    public void addChunk(Paragraph paragraph, String text, Font font, float width) throws Exception {
        Chunk chunk = new Chunk(text, font);
        paragraph.add(chunk);
        if (chunk.getWidthPoint() < width) {
            paragraph.add(Chunk.NEWLINE);
        } else {
            paragraph.add(new Chunk(" ", font));
        }
    }

    public void printHeaderTitle(String text, float llx, float lly, float urx, float ury) throws Exception {
        printHeaderTitle(text, headerBold, llx, lly, urx, ury, Element.ALIGN_LEFT);
    }

    public void printHeaderTitle(String text, Font font, float llx, float lly, float urx, float ury) throws Exception {
        printHeaderTitle(text, font, llx, lly, urx, ury, Element.ALIGN_LEFT);
    }

    public void printHeaderTitle(String text, Font font, float llx, float lly, float urx, float ury, int alignment) throws Exception {
        Paragraph idParagraph = new Paragraph();
        idParagraph.setLeading(0f);
        idParagraph.setFont(font);
        idParagraph.setAlignment(alignment);

        addChunk(idParagraph, text);

        writeElement(idParagraph, llx, lly, urx, ury);
    }

    // </editor-fold>

    public void printHeader(Comprobante comprobante, TimbreFiscalDigital timbreFiscal) throws Exception {
        paginaNumero++;
        lineOffset = lineOffsetPagina;

        String tipoComprobante = "";
        switch (comprobante.getTipoDeComprobante().intValue()) {
            case CTipoDeComprobante.INT_I:
                tipoComprobante = "Ingreso";
                break;
            case CTipoDeComprobante.INT_E:
                tipoComprobante = "Egreso";
                break;
            case CTipoDeComprobante.INT_T:
                tipoComprobante = "Traslado";
                break;
            case CTipoDeComprobante.INT_N:
                tipoComprobante = "CartaPorte";
                break;
            case CTipoDeComprobante.INT_P:
                tipoComprobante = "Pago";
                break;
        }

        // Emisor
        Paragraph emisorParagraph = new Paragraph();
        emisorParagraph.setLeading(10f);
        emisorParagraph.setFont(header2);

        String regimenFiscal = comprobante.getEmisor().getRegimenFiscal().toString();

        Emisor emisor = comprobante.getEmisor();
        addChunk(emisorParagraph, nullText(emisor.getNombre()), headerBold);
        addChunk(emisorParagraph, nullText(emisor.getRfc()), headerBold);
        addChunk(emisorParagraph, "Regimen Fiscal: ", header2Bold, 0f);
        addChunk(emisorParagraph, regimenFiscal, header2);
        addChunk(emisorParagraph, "Numero de certificado: ", header2Bold, 0f);
        addChunk(emisorParagraph, comprobante.getNoCertificado(), header2);

        writeElement(emisorParagraph, 15f, 820f, 300f, 765f);

        // Comprobante
        drawRectangleStroke(300f, 805f, 580f, 820f, clearGray, clearGray);
        printHeaderTitle("CFDI de "+tipoComprobante, headerBold, 300f, 800f, 580f, 810f, Element.ALIGN_CENTER);

        Paragraph comprobanteParagraph = new Paragraph();
        comprobanteParagraph.setLeading(10f);
        comprobanteParagraph.setFont(header2);

        addChunk(comprobanteParagraph, "Serie y Folio: ", header2Bold, 0f);
        addChunk(comprobanteParagraph, nullText(comprobante.getSerie())+" "
               +nullText(comprobante.getFolio()), header2);
        addChunk(comprobanteParagraph, "Lugar de expedición: ", header2Bold, 0f);
        addChunk(comprobanteParagraph, comprobante.getLugarExpedicion(), header2);
        addChunk(comprobanteParagraph, "Fecha y hora de emisión: ", header2Bold, 0f);
        addChunk(comprobanteParagraph, Fecha.getFechaHora(comprobante.getFecha().getTime()), header2);

        writeElement(comprobanteParagraph, 300f, 805f, 580f, 765f);

        // Divisor
        drawRectangleStroke(15f, 760f, 580f, 759f, clearGray);

        // Receptor
        drawRectangleStroke(15f, 740f, 300f, 755f, clearGray, clearGray);
        printHeaderTitle("Cliente", headerBold, 15f, 735f, 300f, 745f, Element.ALIGN_CENTER);

        Paragraph clienteParagraph = new Paragraph();
        clienteParagraph.setLeading(10f);
        clienteParagraph.setFont(header2);

        Receptor receptor = comprobante.getReceptor();
        String receptorNombre = ComprobantePDF.xmlEntities(nullText(receptor.getNombre()));
        String receptorRfc = ComprobantePDF.xmlEntities(receptor.getRfc());

        addChunk(clienteParagraph, receptorNombre, headerBold);
        addChunk(clienteParagraph, "RFC: ", header2Bold, 0f);
        addChunk(clienteParagraph, receptorRfc, header2, 0f);
        addChunk(clienteParagraph, " Uso CFDI: ", header2Bold, 0f);
        addChunk(clienteParagraph, receptor.getUsoCFDI().toString(), header2);
        if (receptor.getDomicilioFiscalReceptor()!=null) {
            addChunk(clienteParagraph, "Domicilio Fiscal: ", header2Bold, 0f);
            addChunk(clienteParagraph, receptor.getDomicilioFiscalReceptor(), header2, 0f);
        }
        if (receptor.getRegimenFiscalReceptor()!=null) {
            addChunk(clienteParagraph, " Regimen Fiscal: ", header2Bold, 0f);
            addChunk(clienteParagraph, receptor.getRegimenFiscalReceptor().toString(), header2);
        }

        StringBuilder domicilioEntrega = new StringBuilder();
        domicilioEntrega.append("Calle: ").append(aspelInformacionEnvioDAO.CALLE);
        if (aspelInformacionEnvioDAO.NUMEXT!=null&&!aspelInformacionEnvioDAO.NUMEXT.isEmpty())
            domicilioEntrega.append(" ").append(aspelInformacionEnvioDAO.NUMEXT);
        if (aspelInformacionEnvioDAO.NUMINT!=null&&!aspelInformacionEnvioDAO.NUMINT.isEmpty())
            domicilioEntrega.append(" ").append(aspelInformacionEnvioDAO.NUMINT);
        domicilioEntrega.append(" Colonia: ").append(aspelInformacionEnvioDAO.COLONIA);
        domicilioEntrega.append(" Codigo Postal: ").append(aspelInformacionEnvioDAO.CODIGO);
        if (aspelInformacionEnvioDAO.POB!=null&&!aspelInformacionEnvioDAO.POB.isEmpty())
            domicilioEntrega.append(" Poblacion: ").append(aspelInformacionEnvioDAO.POB);
        if (aspelInformacionEnvioDAO.MUNICIPIO!=null&&!aspelInformacionEnvioDAO.MUNICIPIO.isEmpty())
            domicilioEntrega.append(" Municipio: ").append(aspelInformacionEnvioDAO.MUNICIPIO);
        if (aspelInformacionEnvioDAO.ESTADO!=null&&!aspelInformacionEnvioDAO.ESTADO.isEmpty())
            domicilioEntrega.append(" Estado: ").append(aspelInformacionEnvioDAO.ESTADO);
        if (aspelInformacionEnvioDAO.REFERDIR!=null&&!aspelInformacionEnvioDAO.REFERDIR.isEmpty())
            domicilioEntrega.append(" Referencia: ").append(aspelInformacionEnvioDAO.REFERDIR);

        addChunk(clienteParagraph, "Domicilio Entrega: ", header2Bold, 0f);
        addChunk(clienteParagraph, domicilioEntrega.toString(), header2);

        writeElement(clienteParagraph, 15f, 740f, 300f, 655f);

        drawRectangleStroke(300f, 740f, 580f, 755f, clearGray, clearGray);
        printHeaderTitle("Datos Adicionales", headerBold, 300f, 735f, 580f, 745f, Element.ALIGN_CENTER);

        Paragraph datosParagraph = new Paragraph();
        datosParagraph.setLeading(10f);
        datosParagraph.setFont(header2);

        addChunk(datosParagraph, "Clave Cliente: ", header2Bold, 0f);
        addChunk(datosParagraph, aspelFacturaDAO.CVE_CLPV+" "+aspelClienteDAO.NOMBRE, header2);
        addChunk(datosParagraph, "Clave Vendedor: ", header2Bold, 0f);
        addChunk(datosParagraph, aspelVendedorDAO.CVE_VEND+" "+aspelVendedorDAO.NOMBRE, header2);
        addChunk(datosParagraph, "Fecha Generación: ", header2Bold, 0f);
        addChunk(datosParagraph, Fecha.getFechaHora(aspelFacturaDAO.FECHAELAB), header2);
        addChunk(datosParagraph, "Fecha Vencimiento: ", header2Bold, 0f);
        addChunk(datosParagraph, Fecha.getFechaHora(aspelFacturaDAO.FECHA_VEN), header2);
        addChunk(datosParagraph, "Pedido: ", header2Bold, 0f);
        addChunk(datosParagraph, aspelPedidoDAO.CVE_DOC, header2);

        String indicador = "";
        if (rutaFacturaDAO.serie==null||rutaFacturaDAO.serie.isEmpty())
            indicador = "";
        else if (rutaFacturaDAO.serie.compareTo("BG")==0
              ||rutaFacturaDAO.serie.compareTo("BO")==0
              ||rutaFacturaDAO.serie.compareTo("BX")==0
              ||rutaFacturaDAO.serie.compareTo("EFX")==0
              ||rutaFacturaDAO.serie.compareTo("EFO")==0)
            indicador = "FA";
        else if (rutaFacturaDAO.serie.compareTo("NN")==0
              ||rutaFacturaDAO.serie.compareTo("NO")==0
              ||rutaFacturaDAO.serie.compareTo("NX")==0
              ||rutaFacturaDAO.serie.compareTo("ENO")==0
              ||rutaFacturaDAO.serie.compareTo("ENX")==0)
            indicador = "NV";

        addChunk(datosParagraph, "Ruta: ", header0Bold, 0f);
        addChunk(datosParagraph, ordenSurtidoPedidoDAO.ruta+" "+indicador, header0Bold);

        writeElement(datosParagraph, 300f, 740f, 580f, 655f);

        // Detalles
        drawRectangleStroke(15f, 650f, 105f, 665f, clearGray, clearGray);
        printHeaderTitle("Codigo", header2Bold, 15f, 645f, 105f, 655f, Element.ALIGN_CENTER);

        drawRectangleStroke(105f, 650f, 150f, 665f, clearGray, clearGray);
        printHeaderTitle("Unidad", header2Bold, 105f, 645f, 150f, 655f, Element.ALIGN_CENTER);

        drawRectangleStroke(150f, 650f, 345f, 665f, clearGray, clearGray);
        printHeaderTitle("Descripción", header2Bold, 150f, 645f, 345f, 655f, Element.ALIGN_CENTER);

        drawRectangleStroke(345f, 650f, 400f, 665f, clearGray, clearGray);
        printHeaderTitle("Cantidad", header2Bold, 345f, 645f, 400f, 655f, Element.ALIGN_CENTER);

        drawRectangleStroke(400f, 650f, 460f, 665f, clearGray, clearGray);
        printHeaderTitle("P.U.", header2Bold, 400f, 645f, 460f, 655f, Element.ALIGN_CENTER);

        drawRectangleStroke(460f, 650f, 520f, 665f, clearGray, clearGray);
        printHeaderTitle("Importe", header2Bold, 460f, 645f, 520f, 655f, Element.ALIGN_CENTER);

        drawRectangleStroke(520f, 650f, 580f, 665f, clearGray, clearGray);
        printHeaderTitle("Descuento", header2Bold, 520f, 645f, 580f, 655f, Element.ALIGN_CENTER);
    }

    public void printPagos(Comprobante comprobante, TimbreFiscalDigital timbreFiscal, Pagos pagos) throws Exception {
        lineOffset -= 10f;

        for (Pagos.Pago pago : pagos.getPagoArray()) {
            drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 34f, colorBlack, clearTransparent);

            drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 10f, colorBlack, clearGray);
            lineOffset -= 8f;
            writeParagraph("P  A  G  O", header, 100f, lineOffset, 345f, lineOffset - 10f);

            lineOffset -= 2f;

            // Pago
            Paragraph fechaPagoParagraph = new Paragraph();
            fechaPagoParagraph.setLeading(10f);
            fechaPagoParagraph.setFont(header2);

            addChunk(fechaPagoParagraph, "Fecha de Pago: ", header2Bold);
            addChunk(fechaPagoParagraph, Fecha.getFechaHora(pago.getFechaPago().getTime()));

            writeElement(fechaPagoParagraph, 20f, lineOffset, 100f, lineOffset - 20f);

            Paragraph formaPagoParagraph = new Paragraph();
            formaPagoParagraph.setLeading(10f);
            formaPagoParagraph.setFont(header2);

            addChunk(formaPagoParagraph, "Forma de Pago: ", header2Bold);
            addChunk(formaPagoParagraph, pago.getFormaDePagoP().toString());

            writeElement(formaPagoParagraph, 120f, lineOffset, 200f, lineOffset - 20f);

            Paragraph monedaParagraph = new Paragraph();
            monedaParagraph.setLeading(10f);
            monedaParagraph.setFont(header2);

            addChunk(monedaParagraph, "Moneda: ", header2Bold);
            addChunk(monedaParagraph, pago.getMonedaP().toString());

            writeElement(monedaParagraph, 220f, lineOffset, 300f, lineOffset - 20f);

            Paragraph montoParagraph = new Paragraph();
            montoParagraph.setLeading(10f);
            montoParagraph.setFont(header2);

            addChunk(montoParagraph, "Monto: ", header2Bold);
            addChunk(montoParagraph, Numero.getMoneda(pago.getMonto().doubleValue()));

            writeElement(montoParagraph, 320f, lineOffset, 400f, lineOffset - 20f);

            Paragraph tipoDeCambioParagraph = new Paragraph();
            tipoDeCambioParagraph.setLeading(10f);
            tipoDeCambioParagraph.setFont(header2);

            addChunk(tipoDeCambioParagraph, "Tipo de Cambio: ", header2Bold);
            addChunk(tipoDeCambioParagraph, pago.getTipoCambioP() == null ? "" : pago.getTipoCambioP().toString());

            writeElement(tipoDeCambioParagraph, 420f, lineOffset, 500f, lineOffset - 20f);

            lineOffset -= 24f;

            drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 10f, colorBlack, clearGray);
            lineOffset -= 8f;
            writeParagraph("D O C U M E N T O S      R E L A C I O N A D O S", header, 100f, lineOffset, 345f, lineOffset - 10f);

            lineOffset -= 2f;

            for (Pagos.Pago.DoctoRelacionado documentoRelacionado : pago.getDoctoRelacionadoArray()) {

                drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 48f, colorBlack, clearTransparent);

                // Documento Relacionado Pago
                Paragraph idDocumentoParagraph = new Paragraph();
                idDocumentoParagraph.setLeading(10f);
                idDocumentoParagraph.setFont(header2);

                addChunk(idDocumentoParagraph, "Id Documento:", header2Bold);
                addChunk(idDocumentoParagraph, documentoRelacionado.getIdDocumento());

                writeElement(idDocumentoParagraph, 20f, lineOffset, 200f, lineOffset - 20f);

                Paragraph serieDRParagraph = new Paragraph();
                serieDRParagraph.setLeading(10f);
                serieDRParagraph.setFont(header2);

                addChunk(serieDRParagraph, "Serie:", header2Bold);
                addChunk(serieDRParagraph, documentoRelacionado.getSerie() == null ? "" : documentoRelacionado.getSerie());

                writeElement(serieDRParagraph, 220f, lineOffset, 300f, lineOffset - 20f);

                Paragraph folioDRParagraph = new Paragraph();
                folioDRParagraph.setLeading(10f);
                folioDRParagraph.setFont(header2);

                addChunk(folioDRParagraph, "Folio:", header2Bold);
                addChunk(folioDRParagraph, documentoRelacionado.getFolio() == null ? "" : documentoRelacionado.getFolio());

                writeElement(folioDRParagraph, 320f, lineOffset, 400f, lineOffset - 20f);

                Paragraph monedaDRParagraph = new Paragraph();
                monedaDRParagraph.setLeading(10f);
                monedaDRParagraph.setFont(header2);

                addChunk(monedaDRParagraph, "Moneda DR:", header2Bold);
                addChunk(monedaDRParagraph, documentoRelacionado.getMonedaDR() == null ? "" : documentoRelacionado.getMonedaDR().toString());

                writeElement(monedaDRParagraph, 420f, lineOffset, 500f, lineOffset - 20f);

                lineOffset -= 24f;

                Paragraph metodoPagoDRParagraph = new Paragraph();
                metodoPagoDRParagraph.setLeading(10f);
                metodoPagoDRParagraph.setFont(header2);

                //addChunk(metodoPagoDRParagraph, "Metodo de Pago DR:", header2Bold);
                //addChunk(metodoPagoDRParagraph, documentoRelacionado.getMetodoDePagoDR().toString());
                writeElement(metodoPagoDRParagraph, 20f, lineOffset, 110f, lineOffset - 20f);

                Paragraph tipoDeCambioDRParagraph = new Paragraph();
                tipoDeCambioDRParagraph.setLeading(10f);
                tipoDeCambioDRParagraph.setFont(header2);

                addChunk(tipoDeCambioDRParagraph, "Equivalencia DR:", header2Bold);
                addChunk(tipoDeCambioDRParagraph, documentoRelacionado.getEquivalenciaDR() == null ? "" : documentoRelacionado.getEquivalenciaDR().toString());

                writeElement(tipoDeCambioDRParagraph, 120f, lineOffset, 210f, lineOffset - 20f);

                Paragraph numeroParcialidadDRParagraph = new Paragraph();
                numeroParcialidadDRParagraph.setLeading(10f);
                numeroParcialidadDRParagraph.setFont(header2);

                addChunk(numeroParcialidadDRParagraph, "Numero Parcialidad:", header2Bold);
                addChunk(numeroParcialidadDRParagraph, documentoRelacionado.getNumParcialidad().toString());

                writeElement(numeroParcialidadDRParagraph, 220f, lineOffset, 310f, lineOffset - 20f);

                Paragraph importeSaldoAnteriorDRParagraph = new Paragraph();
                importeSaldoAnteriorDRParagraph.setLeading(10f);
                importeSaldoAnteriorDRParagraph.setFont(header2);

                addChunk(importeSaldoAnteriorDRParagraph, "Imp. Saldo Anterior:", header2Bold);
                addChunk(importeSaldoAnteriorDRParagraph, Numero.getMoneda(documentoRelacionado.getImpSaldoAnt().doubleValue()));

                writeElement(importeSaldoAnteriorDRParagraph, 320f, lineOffset, 400f, lineOffset - 20f);

                Paragraph importePagadoDRParagraph = new Paragraph();
                importePagadoDRParagraph.setLeading(10f);
                importePagadoDRParagraph.setFont(header2);

                addChunk(importePagadoDRParagraph, "Imp. Pagado:", header2Bold);
                addChunk(importePagadoDRParagraph, Numero.getMoneda(documentoRelacionado.getImpPagado().doubleValue()));

                writeElement(importePagadoDRParagraph, 410f, lineOffset, 480f, lineOffset - 20f);

                Paragraph importeSaldoInsolutoDRParagraph = new Paragraph();
                importeSaldoInsolutoDRParagraph.setLeading(10f);
                importeSaldoInsolutoDRParagraph.setFont(header2);

                addChunk(importeSaldoInsolutoDRParagraph, "Imp. Saldo Insoluto:", header2Bold);
                addChunk(importeSaldoInsolutoDRParagraph, Numero.getMoneda(documentoRelacionado.getImpSaldoInsoluto().doubleValue()));

                writeElement(importeSaldoInsolutoDRParagraph, 490f, lineOffset, 580f, lineOffset - 20f);

                lineOffset -= 24f;

                //
                //
                //
                float detalleOffset = lineOffset - (detalleWidth * 8);
                if (detalleOffset < footerOffset) {
                    printPaginaNueva(comprobante, timbreFiscal);
                }
            }
        }
    }

    public void printCartaPorte(Comprobante comprobante, TimbreFiscalDigital timbreFiscal, CartaPorte cartaPorte) throws Exception {
        lineOffset -= 10f;

        if (lineOffset - 260f < footerOffset) {
            printPaginaNueva(comprobante, timbreFiscal);
        }

        Color darkGray = new Color(0x88, 0x8A, 0x97);

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 10f, darkGray, clearGray);
        lineOffset -= 8f;
        printHeaderTitle("COMPLEMENTO CARTA PORTE", 107f, lineOffset, 575f, lineOffset - 10f);

        lineOffset -= 2f;

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 20f, darkGray, colorWhite);
        lineOffset -= 8f;
        printHeaderTitle("Version:", header2Bold, 22f, lineOffset, 62f, lineOffset - 10f);
        printHeaderTitle(cartaPorte.getVersion(), header2, 62f, lineOffset, 82f, lineOffset - 10f);
        printHeaderTitle("Transporte Internacional:", header2Bold, 92f, lineOffset, 202f, lineOffset - 10f);
        printHeaderTitle(cartaPorte.getTranspInternac().toString(), header2, 202f, lineOffset, 222f, lineOffset - 10f);
        printHeaderTitle("Total Distancia Recorrida:", header2Bold, 222f, lineOffset, 332f, lineOffset - 10f);
        printHeaderTitle(cartaPorte.getTotalDistRec().toString(), header2, 332f, lineOffset, 372f, lineOffset - 10f);

        lineOffset -= 12f;

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 10f, darkGray, clearGray);
        lineOffset -= 8f;
        printHeaderTitle("UBICACIONES", 107f, lineOffset, 575f, lineOffset - 10f);

        lineOffset -= 2f;

        CartaPorte.Ubicaciones.Ubicacion[] ubicaciones = cartaPorte.getUbicaciones().getUbicacionArray();
        for (CartaPorte.Ubicaciones.Ubicacion ubicacion : ubicaciones) {

            drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 70f, darkGray, colorWhite);
            lineOffset -= 8f;
            printHeaderTitle("Tipo:", header2Bold, 22f, lineOffset, 42f, lineOffset - 10f);
            printHeaderTitle(ubicacion.getTipoUbicacion().toString(), header2, 42f, lineOffset, 82f, lineOffset - 10f);
            printHeaderTitle("ID:", header2Bold, 92f, lineOffset, 112f, lineOffset - 10f);
            printHeaderTitle(ubicacion.getIDUbicacion(), header2, 112f, lineOffset, 182f, lineOffset - 10f);
            printHeaderTitle("RFC:", header2Bold, 182f, lineOffset, 212f, lineOffset - 10f);
            printHeaderTitle(ubicacion.getRFCRemitenteDestinatario(), header2, 212f, lineOffset, 292f, lineOffset - 10f);
            if (ubicacion.getTipoUbicacion().toString().compareTo("Destino") == 0) {
                printHeaderTitle("Fecha y Hora de Llegada:", header2Bold, 292f, lineOffset, 392f, lineOffset - 10f);
            } else {
                printHeaderTitle("Fecha y Hora de Salida:", header2Bold, 292f, lineOffset, 392f, lineOffset - 10f);
            }
            printHeaderTitle(Fecha.getFechaHora(ubicacion.getFechaHoraSalidaLlegada().getTime()), header2, 392f, lineOffset, 482f, lineOffset - 10f);

            lineOffset -= 10f;

            printHeaderTitle("Nombre:", header2Bold, 22f, lineOffset, 62f, lineOffset - 10f);
            printHeaderTitle(nullText(ubicacion.getNombreRemitenteDestinatario()), header2, 62f, lineOffset, 512f, lineOffset - 10f);

            lineOffset -= 20f;

            String direccion1 = ubicacion.getDomicilio().getCalle()+" "
                   +nullText(ubicacion.getDomicilio().getNumeroExterior())+" "
                   +nullText(ubicacion.getDomicilio().getNumeroInterior())+" Codigo Postal: "
                   +ubicacion.getDomicilio().getCodigoPostal();

            printHeaderTitle("Dirección:", header2Bold, 22f, lineOffset, 62f, lineOffset - 10f);
            printHeaderTitle(direccion1, header2, 62f, lineOffset, 512f, lineOffset - 10f);

            lineOffset -= 10f;

            ColoniaSATDAO coloniaSATDAO = new ColoniaSATDAO(ubicacion.getDomicilio().getColonia(), ubicacion.getDomicilio().getCodigoPostal());
            ds.exists(coloniaSATDAO);
            EntidadFederativaDAO entidadFederativaDAO = (EntidadFederativaDAO) ds.first(new EntidadFederativaDAO(),
                    "estado = '"+ubicacion.getDomicilio().getEstado()+"'");
            ds.exists(entidadFederativaDAO);
            MunicipioSATDAO municipioSATDAO = new MunicipioSATDAO(ubicacion.getDomicilio().getMunicipio(), ubicacion.getDomicilio().getEstado());
            ds.exists(municipioSATDAO);
            LocalidadSATDAO localidadSATDAO = new LocalidadSATDAO(ubicacion.getDomicilio().getLocalidad(), ubicacion.getDomicilio().getEstado());
            ds.exists(localidadSATDAO);

            String direccion2 = "Colonia: "
                   +coloniaSATDAO.descripcion+" Municipio: "
                   +municipioSATDAO.descripcion+" Estado: "
                   +entidadFederativaDAO.nombre;

            printHeaderTitle(direccion2, header2, 22f, lineOffset, 512f, lineOffset - 10f);

            lineOffset -= 10f;

            String direccion3 = "Localidad: "
                   +localidadSATDAO.descripcion+" Pais: "
                   +ubicacion.getDomicilio().getPais().toString();

            printHeaderTitle(direccion3, header2, 22f, lineOffset, 512f, lineOffset - 10f);

            lineOffset -= 12f;
        }

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 10f, darkGray, clearGray);
        lineOffset -= 8f;
        printHeaderTitle("MERCANCIAS", 107f, lineOffset, 575f, lineOffset - 10f);

        lineOffset -= 2f;

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 20f, darkGray, colorWhite);
        lineOffset -= 8f;
        printHeaderTitle("Numero Total de Mercancias:", header2Bold, 22f, lineOffset, 152f, lineOffset - 10f);
        printHeaderTitle(String.valueOf(cartaPorte.getMercancias().getNumTotalMercancias()), header2, 152f, lineOffset, 172f, lineOffset - 10f);
        printHeaderTitle("Peso Bruto Total:", header2Bold, 172f, lineOffset, 242f, lineOffset - 10f);
        printHeaderTitle(String.valueOf(cartaPorte.getMercancias().getPesoBrutoTotal()), header2, 242f, lineOffset, 272f, lineOffset - 10f);
        printHeaderTitle("Unidad Peso:", header2Bold, 272f, lineOffset, 322f, lineOffset - 10f);
        printHeaderTitle(cartaPorte.getMercancias().getUnidadPeso().toString(), header2, 322f, lineOffset, 352f, lineOffset - 10f);

        lineOffset -= 12f;

        CartaPorte.Mercancias.Mercancia[] mercancias = cartaPorte.getMercancias().getMercanciaArray();
        for (CartaPorte.Mercancias.Mercancia mercancia : mercancias) {

            drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 50f, darkGray, colorWhite);
            lineOffset -= 8f;
            printHeaderTitle("Bien Transportado:", header2Bold, 22f, lineOffset, 102f, lineOffset - 10f);
            printHeaderTitle(mercancia.getBienesTransp(), header2, 102f, lineOffset, 162f, lineOffset - 10f);
            printHeaderTitle("Descripción:", header2Bold, 162f, lineOffset, 212f, lineOffset - 10f);
            printHeaderTitle(mercancia.getDescripcion(), header2, 212f, lineOffset, 512f, lineOffset - 10f);

            lineOffset -= 10f;

            printHeaderTitle("Cantidad:", header2Bold, 22f, lineOffset, 72f, lineOffset - 10f);
            printHeaderTitle(mercancia.getCantidad().toString(), header2, 72f, lineOffset, 112f, lineOffset - 10f);
            printHeaderTitle("Clave Unidad:", header2Bold, 112f, lineOffset, 182f, lineOffset - 10f);
            printHeaderTitle(mercancia.getClaveUnidad().toString(), header2, 182f, lineOffset, 232f, lineOffset - 10f);
            printHeaderTitle("Peso en KG:", header2Bold, 232f, lineOffset, 292f, lineOffset - 10f);
            printHeaderTitle(mercancia.getPesoEnKg().toString(), header2, 292f, lineOffset, 352f, lineOffset - 10f);

            lineOffset -= 10f;

            if (mercancia.getMaterialPeligroso()!=null && mercancia.getMaterialPeligroso().compareTo("Sí") == 0) {
                printHeaderTitle("Clave Material Peligroso:", header2Bold, 22f, lineOffset, 122f, lineOffset - 10f);
                printHeaderTitle(mercancia.getCveMaterialPeligroso(), header2, 122f, lineOffset, 172f, lineOffset - 10f);
                printHeaderTitle("Embalaje:", header2Bold, 172f, lineOffset, 212f, lineOffset - 10f);
                printHeaderTitle(mercancia.getEmbalaje().toString(), header2, 212f, lineOffset, 252f, lineOffset - 10f);
                printHeaderTitle("Descripción Embalaje:", header2Bold, 252f, lineOffset, 352f, lineOffset - 10f);
                printHeaderTitle(mercancia.getDescripEmbalaje(), header2, 352f, lineOffset, 512f, lineOffset - 10f);

            }

            lineOffset -= 10f;

            if (mercancia.getCantidadTransportaArray()!=null && mercancia.getCantidadTransportaArray().length > 0) {
                CartaPorte.Mercancias.Mercancia.CantidadTransporta cantidadTransporta = mercancia.getCantidadTransportaArray()[0];

                printHeaderTitle("Origen:", header2Bold, 22f, lineOffset, 72f, lineOffset - 10f);
                printHeaderTitle(cantidadTransporta.getIDOrigen(), header2, 72f, lineOffset, 122f, lineOffset - 10f);
                printHeaderTitle("Destino:", header2Bold, 122f, lineOffset, 172f, lineOffset - 10f);
                printHeaderTitle(cantidadTransporta.getIDDestino(), header2, 172f, lineOffset, 222f, lineOffset - 10f);
                printHeaderTitle("Cantidad:", header2Bold, 222f, lineOffset, 282f, lineOffset - 10f);
                printHeaderTitle(cantidadTransporta.getCantidad().toString(), header2, 282f, lineOffset, 342f, lineOffset - 10f);
            }

            lineOffset -= 12f;

            if (lineOffset < footerOffset) {
                printPaginaNueva(comprobante, timbreFiscal);
            }
        }

        if ((lineOffset - 120) < footerOffset) {
            printPaginaNueva(comprobante, timbreFiscal);
        }

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 10f, darkGray, clearGray);
        lineOffset -= 8f;
        printHeaderTitle("AUTOTRANSPORTE", 107f, lineOffset, 575f, lineOffset - 10f);

        lineOffset -= 2f;

        CartaPorte.Mercancias.Autotransporte autotransporte = cartaPorte.getMercancias().getAutotransporte();

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 100f, darkGray, colorWhite);
        lineOffset -= 8f;
        printHeaderTitle("Permiso SCT:", header2Bold, 22f, lineOffset, 102f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getPermSCT().toString(), header2, 102f, lineOffset, 152f, lineOffset - 10f);
        printHeaderTitle("Número de Permiso SCT:", header2Bold, 152f, lineOffset, 262f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getNumPermisoSCT(), header2, 262f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 10f;

        printHeaderTitle("Configuración Vehicular:", header2Bold, 22f, lineOffset, 132f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getIdentificacionVehicular().getConfigVehicular().toString(), header2, 132f, lineOffset, 172f, lineOffset - 10f);
        printHeaderTitle("Placa:", header2Bold, 172f, lineOffset, 212f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getIdentificacionVehicular().getPlacaVM(), header2, 212f, lineOffset, 272f, lineOffset - 10f);
        printHeaderTitle("Año Modelo:", header2Bold, 272f, lineOffset, 332f, lineOffset - 10f);
        printHeaderTitle(String.valueOf(autotransporte.getIdentificacionVehicular().getAnioModeloVM()), header2, 332f, lineOffset, 402f, lineOffset - 10f);

        lineOffset -= 10f;

        printHeaderTitle("Póliza Responsabilidad Civíl:", header2Bold, 22f, lineOffset, 182f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getSeguros().getPolizaRespCivil(), header2, 182f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 10f;

        printHeaderTitle("Aseguradora Responsabilidad Civíl:", header2Bold, 22f, lineOffset, 182f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getSeguros().getAseguraRespCivil(), header2, 182f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 10f;

        printHeaderTitle("Póliza Carga:", header2Bold, 22f, lineOffset, 162f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getSeguros().getPolizaCarga(), header2, 162f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 10f;

        printHeaderTitle("Aseguradora Carga:", header2Bold, 22f, lineOffset, 162f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getSeguros().getAseguraCarga(), header2, 162f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 10f;

        if (autotransporte.getSeguros().getAseguraMedAmbiente()!=null) {
            printHeaderTitle("Póliza Medio Ambiente:", header2Bold, 22f, lineOffset, 182f, lineOffset - 10f);
            printHeaderTitle(autotransporte.getSeguros().getPolizaMedAmbiente(), header2, 182f, lineOffset, 512f, lineOffset - 10f);

            lineOffset -= 10f;

            printHeaderTitle("Aseguradora Medio Ambiente:", header2Bold, 22f, lineOffset, 182f, lineOffset - 10f);
            printHeaderTitle(autotransporte.getSeguros().getAseguraMedAmbiente(), header2, 182f, lineOffset, 512f, lineOffset - 10f);
        } else {
            lineOffset -= 10f;
        }

        lineOffset -= 10f;

        printHeaderTitle("Prima Seguro:", header2Bold, 22f, lineOffset, 162f, lineOffset - 10f);
        printHeaderTitle(autotransporte.getSeguros().getPrimaSeguro().toString(), header2, 162f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 10f;

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 10f, darkGray, clearGray);
        lineOffset -= 8f;
        printHeaderTitle("FIGURA TRANSPORTE", 107f, lineOffset, 575f, lineOffset - 10f);

        lineOffset -= 2f;

        CartaPorte.FiguraTransporte.TiposFigura tiposFigura = cartaPorte.getFiguraTransporte().getTiposFiguraArray(0);

        drawRectangleStroke(20f, lineOffset, 575f, lineOffset - 40f, darkGray, colorWhite);
        lineOffset -= 8f;
        printHeaderTitle("RFC:", header2Bold, 22f, lineOffset, 72f, lineOffset - 10f);
        printHeaderTitle(tiposFigura.getRFCFigura(), header2, 72f, lineOffset, 152f, lineOffset - 10f);
        printHeaderTitle("Licencia:", header2Bold, 152f, lineOffset, 212f, lineOffset - 10f);
        printHeaderTitle(tiposFigura.getNumLicencia(), header2, 212f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 10f;

        printHeaderTitle("Nombre:", header2Bold, 22f, lineOffset, 82f, lineOffset - 10f);
        printHeaderTitle(tiposFigura.getNombreFigura(), header2, 82f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 10f;

        String tipoFigura = tiposFigura.getTipoFigura().toString();
        switch (tipoFigura) {
            case "01":
                tipoFigura = "Operador";
                break;
            case "02":
                tipoFigura = "Propietario";
                break;
            case "03":
                tipoFigura = "Arrendador";
                break;
            case "04":
                tipoFigura = "Notificado";
                break;
        }
        printHeaderTitle("Tipo Figura:", header2Bold, 22f, lineOffset, 82f, lineOffset - 10f);
        printHeaderTitle(tipoFigura, header2, 82f, lineOffset, 512f, lineOffset - 10f);

        lineOffset -= 20f;
    }

    public void printDetalle(Comprobante comprobante, TimbreFiscalDigital timbreFiscal, CartaPorte cartaPorte, ASPELFacturaDetalleTO detalleFactura) throws Exception {
        detalleNo++;
        DecimalFormat df = new DecimalFormat("$#,##0.00");
        DecimalFormat dfI = new DecimalFormat("#0.00");

        // Detalle
        Paragraph detalleParagraph = new Paragraph();
        detalleParagraph.setLeading(detalleWidth);
        detalleParagraph.setFont(detalle);

        if (detalleFactura.getPrIva()>0) {
            detalleFactura.DESCR = "* "+detalleFactura.DESCR;
        }

        String[] tokensDescripcion = splitDescripcion(detalleFactura.getDescripcion());

        addChunk(detalleParagraph, F.f(nullText(detalleFactura.getNoIdentificacion()), 20, (short)(F.LJ+F.TR)), 0f);
        addChunk(detalleParagraph, F.f(nullText(detalleFactura.getUnidad()), 10, (short)(F.LJ+F.TR)), 0f);
        addChunk(detalleParagraph, F.f(tokensDescripcion[0], descripcionLongitudMaxima, (short)(F.LJ+F.TR)), 0f);
        addChunk(detalleParagraph, F.f(dfI.format((int)detalleFactura.getCantidad()), 9, (short)(F.RJ+F.TR)), 0f);
        addChunk(detalleParagraph, F.f(df.format(detalleFactura.getValorUnitario()), 12, (short)(F.RJ+F.TR)), 0f);
        addChunk(detalleParagraph, F.f(df.format(detalleFactura.getImporte()), 15, (short)(F.RJ+F.TR)), detalleBold, 0f);
        addChunk(detalleParagraph, F.f(df.format(detalleFactura.getDescuento()), 14, (short)(F.RJ+F.TR)), detalleBold, 0f);

        boolean addNewLine = true;
        float lineOffsetDetalle = lineOffset - (detalleParagraph.getLeading() * 1f);
        int lineasAdicionalesDescripcion = 0;

        if (tokensDescripcion.length > 1) {
            for (int i = 1; i < tokensDescripcion.length; i++) {
                detalleParagraph.add(Chunk.NEWLINE);

                addChunk(detalleParagraph, F.f("", 30, (short)(F.LJ+F.TR)), 0f);
                addChunk(detalleParagraph, F.f(tokensDescripcion[i], descripcionLongitudMaxima, (short)(F.LJ+F.TR)), 0f);

                lineasAdicionalesDescripcion++;
                lineOffsetDetalle -= detalleParagraph.getLeading() * 1f;

                if (lineOffsetDetalle < footerOffset) {
                    float detalleParagraphWidth = detalleParagraph.getLeading() * ((float) lineasPorDetalle+(float) lineasAdicionalesDescripcion);
                    writeElement(detalleParagraph, 15f, lineOffset, 580f, lineOffset - detalleParagraphWidth);
                    lineOffset -= detalleParagraphWidth;

                    printPaginaNueva(comprobante, timbreFiscal);

                    detalleParagraph = new Paragraph();
                    detalleParagraph.setLeading(detalleWidth);
                    detalleParagraph.setFont(detalle);

                    addNewLine = false;
                    lineOffsetDetalle = lineOffset - (detalleParagraph.getLeading() * 1f);
                    lineasAdicionalesDescripcion = 0;
                }
            }
        }

        detalleParagraph.add(Chunk.NEWLINE);

        addChunk(detalleParagraph, F.f(detalleFactura.getClaveProductoServicio(), 20, (short)(F.LJ+F.TR)), 0f);
        addChunk(detalleParagraph, F.f(detalleFactura.getClaveUnidad().toString(), 10, (short)(F.LJ+F.TR)), 0f);
        //descripcionLongitudMaxima es igual a 45;
        addChunk(detalleParagraph, F.f("P.Publico:", 15, (short)(F.RJ+F.TR)), detalleBold, 0f);
        addChunk(detalleParagraph, F.f(df.format(detalleFactura.PREPUB), 15, (short)(F.RJ+F.TR)), detalle, 0f);

        InformacionAduaneraCFD informacionAduaneraCFD = detalleFactura.getInformacionAduanera();
        if (informacionAduaneraCFD!=null) {
            addChunk(detalleParagraph, F.f("Lote:", 10, (short)(F.RJ+F.TR)), detalleBold, 0f);
            addChunk(detalleParagraph, F.f(informacionAduaneraCFD.getLote(), 10, (short)(F.LJ+F.TR)), detalle, 0f);
            Calendar fechaCaducidad = informacionAduaneraCFD.getFechaCaducidad();
            if (fechaCaducidad!=null) {
                addChunk(detalleParagraph, F.f("Caducidad:", 12, (short)(F.RJ+F.TR)), detalleBold, 0f);
                addChunk(detalleParagraph, F.f(Fecha.getFecha(fechaCaducidad.getTime()), 10, (short)(F.LJ+F.TR)), detalle, 0f);
            }
        }

        detalleParagraph.add(Chunk.NEWLINE);

        if (detalleFactura.getPrIesps()>0) {
            addChunk(detalleParagraph, "Traslado", detalleBold, 0f);
            addChunk(detalleParagraph,
                    "  Impuesto: IEPS"
                   +"  Tasa o cuota: "+dfI.format(detalleFactura.getPrIesps()*100.0d)
                   +"  Base: "+df.format(detalleFactura.getImporte() - detalleFactura.getDescuento())
                   +"  Importe: "+df.format(detalleFactura.getIesps()));
        }
        if (detalleFactura.getPrIva()>0) {
            addChunk(detalleParagraph, "Traslado", detalleBold, 0f);
            addChunk(detalleParagraph,
                    "  Impuesto: IVA"
                   +"  Tasa o cuota: "+dfI.format(detalleFactura.getPrIva()*100.0d)
                   +"  Base: "+df.format(detalleFactura.getImporte() + detalleFactura.getIesps() - detalleFactura.getDescuento())
                   +"  Importe: "+df.format(detalleFactura.getIva()));
        }

        float detalleParagraphWidth = detalleParagraph.getLeading() * ((float)lineasPorDetalle+(float) lineasAdicionalesDescripcion);
        writeElement(detalleParagraph, 15f, lineOffset, 580f, lineOffset - detalleParagraphWidth);

        detalleParagraphWidth += 5f;

        lineOffset -= detalleParagraphWidth;

        // Divisor
        drawRectangleStroke(15f, lineOffset, 580f, lineOffset, clearGray);
    }

    public void printTotales(Comprobante comprobante, TimbreFiscalDigital timbreFiscal) throws Exception {
        if (lineOffset - 100f < footerOffset) {
            printPaginaNueva(comprobante, timbreFiscal);
        }

        DecimalFormat df = new DecimalFormat("$,##0.00");
        DecimalFormat dfI = new DecimalFormat("#0.00");

        String moneda = comprobante.getMoneda().toString();

        // Subtotal
        lineOffset -= 40f;

        drawRectangleStroke(15f, lineOffset, 580f, lineOffset+20f, colorWhite, colorWhite);
        printHeaderTitle("Subtotal "+F.f(df.format(comprobante.getSubTotal().doubleValue()), 15, (short)(F.TR+F.RJ)),
                header0, 15f, lineOffset - 10f, 580f, lineOffset+5f, Element.ALIGN_RIGHT);

        lineOffset -= 20f;

        // Impuesto
        if (comprobante.getImpuestos()!=null) {
            if (comprobante.getImpuestos().getTraslados()!=null) {
                for (Comprobante.Impuestos.Traslados.Traslado traslado : comprobante.getImpuestos().getTraslados().getTrasladoArray()) {
                    String tipo = traslado.getImpuesto().toString().compareTo("002")==0 ? "IVA" :
                            traslado.getImpuesto().toString().compareTo("003")==0 ? "IEPS" : traslado.getImpuesto().toString();
                    drawRectangleStroke(15f, lineOffset, 580f, lineOffset+20f, colorWhite, colorWhite);
                    printHeaderTitle(tipo+" Traslado ("+traslado.getTasaOCuota()+") "+F.f(df.format(traslado.getImporte().doubleValue()), 15, (short)(F.TR+F.RJ)),
                            header0, 15f, lineOffset - 10f, 580f, lineOffset+5f, Element.ALIGN_RIGHT);

                    lineOffset -= 20f;
                }
            }
            if (comprobante.getImpuestos().getRetenciones()!=null) {
                for (Comprobante.Impuestos.Retenciones.Retencion retencion : comprobante.getImpuestos().getRetenciones().getRetencionArray()) {
                    String tipo = retencion.getImpuesto().toString();
                    drawRectangleStroke(15f, lineOffset, 580f, lineOffset+20f, colorWhite, colorWhite);
                    printHeaderTitle(tipo+" Retencion "+F.f(df.format(retencion.getImporte().doubleValue()), 15, (short)(F.TR+F.RJ)),
                            header0, 15f, lineOffset - 10f, 580f, lineOffset+5f, Element.ALIGN_RIGHT);

                    lineOffset -= 20f;
                }
            }
        }

        // Total
        lineOffset -= 5f;

        drawRectangleStroke(15f, lineOffset, 580f, lineOffset+20f, clearGray, clearGray);
        printHeaderTitle("Total "+moneda+F.f(df.format(comprobante.getTotal().doubleValue()), 15, (short)(F.TR+F.RJ))+" ",
                header0Bold, 15f, lineOffset - 10f, 580f, lineOffset+5f, Element.ALIGN_RIGHT);

        // Metodo de Pago y Forma de Pago
        printHeaderTitle("Método de Pago", header2Bold, 15f, lineOffset+35f, 200f, lineOffset+50f, Element.ALIGN_LEFT);
        printHeaderTitle("Forma de Pago", header2Bold, 210f, lineOffset+35f, 400f, lineOffset+50f, Element.ALIGN_LEFT);

        printHeaderTitle(nullText(comprobante.getMetodoPago()), header2, 15f, lineOffset+25f, 200f, lineOffset+40f, Element.ALIGN_LEFT);
        printHeaderTitle(nullText(comprobante.getFormaPago()), header2, 210f, lineOffset+25f, 400f, lineOffset+40f, Element.ALIGN_LEFT);

        // Contado o Credito
        printHeaderTitle(aspelClienteDAO.DIASCRED==0 ? "DE CONTADO" : "A CRÉDITO",
                header2Bold, 15f, lineOffset+55f, 200f, lineOffset+70f, Element.ALIGN_LEFT);            
        printHeaderTitle("* NO SE ACEPTAN DEVOLUCIONES DESPUES DE 7 DIAS DE ENTREGADA LA MERCANCIA",
                header2Bold, 15f, lineOffset+45f, 400f, lineOffset+60f, Element.ALIGN_LEFT);            
        
        // Importe con Letra
        /*lineOffset -= 15f;

        Paragraph importeConLetraParagraph = new Paragraph();
        importeConLetraParagraph.setLeading(10f);
        importeConLetraParagraph.setFont(headerBold);

        Letras letras = new Letras(moneda);
        addChunk(importeConLetraParagraph, letras.letras(comprobante.getTotal().doubleValue()));
        String tipoDeCambio = "";
        if (comprobante.getTipoCambio()!=null) {
            tipoDeCambio = "Tipo de Cambio: "+comprobante.getTipoCambio().toString();
        }
        addChunk(importeConLetraParagraph, "Moneda: "+comprobante.getMoneda().toString()+" "+tipoDeCambio);

        writeElement(importeConLetraParagraph, 15f, lineOffset - 10f, 580f, lineOffset+5f);*/

        
        // Pagare
        lineOffset -= 10f;
        
        Paragraph pagareParagraph = new Paragraph();
        pagareParagraph.setLeading(10f);
        pagareParagraph.setFont(header);
        pagareParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
        
        Letras letras = new Letras(moneda);

        addChunk(pagareParagraph, "Pagare bueno por "+df.format(comprobante.getTotal().doubleValue())+" En la Ciudad de México a "+Fecha.getFecha(aspelFacturaDAO.FECHAELAB)
                +" Debemos y pagaremos incondicionalmente por este pagare a la orden de REDER, S. DE R.L. DE C.V., la cantidad de "+df.format(comprobante.getTotal().doubleValue())+", "
                +"cantidad con letra: "+letras.letras(comprobante.getTotal().doubleValue())+" en la Ciudad de México, fecha fecha_del_ticket. "
                +"El suscriptor pagará a la vista intereses ordinarios mensuales a partir del día "+Fecha.getFecha(aspelFacturaDAO.FECHA_VEN)+" a razón del 5% "
                +"en caso de que el suscriptor no pague en la fecha de vencimiento la totalidad del saldo, ni cualquier pago de intereses ordinarios "
                +"se pagaran intereses moratorios a razón del 5% mensual.");
        
        writeElement(pagareParagraph, 15f, lineOffset - 50f, 580f, lineOffset+5f);

        // Firmas
        lineOffset -= 80f;

        Paragraph firma1Paragraph = new Paragraph();
        firma1Paragraph.setLeading(10f);
        firma1Paragraph.setFont(header);
        firma1Paragraph.setAlignment(Element.ALIGN_CENTER);
        
        addChunk(firma1Paragraph, "__________________________________");
        addChunk(firma1Paragraph, aspelClienteDAO.NOMBRE);
        
        writeElement(firma1Paragraph, 15f, lineOffset - 70f, 190f, lineOffset+5f);
        
        Paragraph firma2Paragraph = new Paragraph();
        firma2Paragraph.setLeading(10f);
        firma2Paragraph.setFont(header);
        firma2Paragraph.setAlignment(Element.ALIGN_CENTER);
        
        addChunk(firma2Paragraph, "__________________________________");
        addChunk(firma2Paragraph, "Firma de la persona que suscribe el pagare");
        
        writeElement(firma2Paragraph, 190f, lineOffset - 70f, 380f, lineOffset+5f);

        Paragraph firma3Paragraph = new Paragraph();
        firma3Paragraph.setLeading(10f);
        firma3Paragraph.setFont(header);
        firma3Paragraph.setAlignment(Element.ALIGN_CENTER);
        
        addChunk(firma3Paragraph, "__________________________________");
        addChunk(firma3Paragraph, "Nombre completo, domicilio y firma del aval");
        
        writeElement(firma3Paragraph, 390f, lineOffset - 70f, 580f, lineOffset+5f);

    }

    public void printCfdiRelacionados(Comprobante comprobante) throws Exception {
        CfdiRelacionados[] cfdiRelacionados = comprobante.getCfdiRelacionadosArray();
        if (cfdiRelacionados==null||cfdiRelacionados.length==0)
            return;

        // Cfdi Relacionados
        lineOffset -= 10f;

        drawRectangleStroke(15f, lineOffset, 580f, lineOffset+15f, clearGray, clearGray);
        printHeaderTitle("CFDI relacionados",
                headerBold, 15f, lineOffset - 10f, 580f, lineOffset+5f, Element.ALIGN_LEFT);

        lineOffset -= 25f;

        printHeaderTitle("Tipo de relación", header2Bold, 15f, lineOffset, 200f, lineOffset+15f, Element.ALIGN_LEFT);
        printHeaderTitle("UUID relacionados", header2Bold, 210f, lineOffset, 400f, lineOffset+15f, Element.ALIGN_LEFT);

        for (CfdiRelacionados cfdiRelacionado : cfdiRelacionados) {
            CfdiRelacionados.CfdiRelacionado[] relacionados = cfdiRelacionado.getCfdiRelacionadoArray();

            for (CfdiRelacionados.CfdiRelacionado relacionado : relacionados) {
                lineOffset -= 10f;

                printHeaderTitle(cfdiRelacionado.getTipoRelacion().toString(), header2, 15f, lineOffset, 200f, lineOffset+15f, Element.ALIGN_LEFT);
                printHeaderTitle(relacionado.getUUID(), header2, 210f, lineOffset, 400f, lineOffset+15f, Element.ALIGN_LEFT);
            }
        }
    }

    public void printPaginaNueva(Comprobante comprobante, TimbreFiscalDigital timbreFiscal) throws Exception {
        // Pie de Pagina
        printFooter();
        printSelloYCadenaOriginal(comprobante, timbreFiscal);

        //
        document.newPage();

        // Header
        printHeader(comprobante, timbreFiscal);
    }

    public void printFooter() throws Exception {
        // Pagina
        Paragraph paginaParagraph = new Paragraph();
        paginaParagraph.setAlignment(Element.ALIGN_RIGHT);

        addChunk(paginaParagraph, "Pagina "+paginaNumero+" de "+totalPaginas, header2Bold);

        writeElement(paginaParagraph, 500f, 40f, 574f, 20f);

        printLeyenda();
    }

    public void printLeyenda() throws Exception {
        // Leyenda CFD
        Paragraph leyendaCFDParagraph = new Paragraph();
        addChunk(leyendaCFDParagraph, "ESTE DOCUMENTO ES UNA REPRESENTACION IMPRESA DE UN CFDI.", header2Bold);

        writeElement(leyendaCFDParagraph, 300f, 30f, 574f, 10f);
    }

    public void printSelloYCadenaOriginal(Comprobante comprobante, TimbreFiscalDigital timbreFiscal) throws Exception {
        // Divisor
        drawRectangleStroke(15f, 155f, 580f, 154f, clearGray);

        // Izquierda
        drawRectangleStroke(120f, 135f, 355f, 150f, clearGray, clearGray);
        printHeaderTitle("Folio Fiscal", header2Bold, 122f, 130f, 345f, 140f);

        printHeaderTitle(timbreFiscal.getUUID(), header2, 120f, 115f, 355f, 125f);

        drawRectangleStroke(120f, 105f, 355f, 120f, clearGray, clearGray);
        printHeaderTitle("RFC del PAC", header2Bold, 122f, 100f, 355f, 110f);

        printHeaderTitle(timbreFiscal.getRfcProvCertif(), header2, 120f, 85f, 345f, 95f);

        // Cadena Original
        drawRectangleStroke(120f, 75f, 355f, 90f, clearGray, clearGray);
        printHeaderTitle("Cadena Original del Timbre", header2Bold, 122f, 70f, 345f, 80f);

        Paragraph cadenaOriginalParagraph = new Paragraph();
        cadenaOriginalParagraph.setLeading(7f);
        cadenaOriginalParagraph.setFont(header4);

        addChunk(cadenaOriginalParagraph, cadenaOriginal);

        writeElement(cadenaOriginalParagraph, 120f, 75f, 355f, 30f);

        // Derecha
        drawRectangleStroke(360f, 135f, 465f, 150f, clearGray, clearGray);
        printHeaderTitle("Número de Certificado SAT", header2Bold, 362f, 130f, 465f, 140f);

        printHeaderTitle(timbreFiscal.getNoCertificadoSAT(), header2, 362f, 115f, 465f, 125f);

        drawRectangleStroke(470f, 135f, 580f, 150f, clearGray, clearGray);
        printHeaderTitle("Fecha y Hora de Certificación", header2Bold, 472f, 130f, 580f, 140f);

        printHeaderTitle(Fecha.getFechaHora(timbreFiscal.getFechaTimbrado().getTime()), header2, 470f, 115f, 580f, 125f);

        // Sello
        drawRectangleStroke(360f, 105f, 465f, 120f, clearGray, clearGray);
        printHeaderTitle("Sello Digital del SAT", header2Bold, 362f, 100f, 465f, 110f);

        Paragraph selloSATParagraph = new Paragraph();
        selloSATParagraph.setLeading(7f);
        selloSATParagraph.setFont(header4);

        addChunk(selloSATParagraph, timbreFiscal.getSelloSAT());

        writeElement(selloSATParagraph, 360f, 105f, 465f, 30f);

        // Sello CFDI
        drawRectangleStroke(470f, 105f, 580f, 120f, clearGray, clearGray);
        printHeaderTitle("Sello Digital del CFDI", header2Bold, 472f, 100f, 580f, 110f);

        Paragraph selloParagraph = new Paragraph();
        selloParagraph.setLeading(7f);
        selloParagraph.setFont(header4);

        addChunk(selloParagraph, comprobante.getSello());

        writeElement(selloParagraph, 470f, 105f, 580f, 30f);

        // QR
        generaQR(qr);
    }

    public void generaQR(String data) throws Exception {
        int qr_image_width = 115;
        int qr_image_height = 115;

        // Encode URL in QR format
        Writer writerQR = new QRCodeWriter();
        BitMatrix matrix = writerQR.encode(data, BarcodeFormat.QR_CODE, qr_image_width, qr_image_height);

        // Create buffered image to draw to
        BufferedImage image = new BufferedImage(qr_image_width, qr_image_height, BufferedImage.TYPE_INT_RGB);

        // Iterate through the matrix and draw the pixels to the image
        for (int y = 0; y < qr_image_height; y++) {
            for (int x = 0; x < qr_image_width; x++) {
                int grayValue = (matrix.get(x, y) ? 0 : 1) & 0xff;
                image.setRGB(x, y, (grayValue == 0 ? 0 : 0xFFFFFF));
            }
        }

        // Write the image to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        Image qrImage = Image.getInstance(baos.toByteArray());
        baos.close();

        writeElement(qrImage, 5f, 150f, 120f, 35f);
    }

    // <editor-fold defaultstate="collapsed" desc="nullText, split methods. Click on the+sign on the left to edit the code.">
    public String nullText(Object texto) {
        if (texto == null) {
            return "";
        }
        return texto.toString()+" ";
    }

    public String[] splitDescripcion(String texto) {
        ArrayList tokens = new ArrayList();

        int j = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (((int) c) == 63) {
                String[] tokensLen = splitParrafo(texto.substring(j, i), descripcionLongitudMaxima);
                for (String token : tokensLen) {
                    tokens.add(token);
                }
                j = i+1;
            }
        }

        String[] tokensLen = splitParrafo(texto.substring(j), descripcionLongitudMaxima);
        for (String token : tokensLen) {
            tokens.add(token);
        }

        String[] array = new String[tokens.size()];
        tokens.toArray(array);
        return array;
    }

    public String[] splitParrafo(String texto, int len) {
        ArrayList tokens = new ArrayList();

        StringBuffer linea = new StringBuffer();
        String[] palabras = texto.split(" ");
        for (String palabra : palabras) {
            if ((linea.length()+palabra.length()+1) > len) {
                tokens.add(linea.toString());
                linea = linea = new StringBuffer();
            }
            linea.append(palabra).append(" ");
        }
        tokens.add(linea.toString());

        String[] array = new String[tokens.size()];
        tokens.toArray(array);
        return array;
    }

    public String[] splitTexto(String[] head, String texto, int len) {
        ArrayList tokens = new ArrayList();
        if (head!=null) {
            for (int i = 0; i < head.length; i++) {
                tokens.add(head[i]);
            }
        }
        int begin = 0;
        int end = 0;
        while (true) {
            end = texto.length() > (begin+len) ? begin+len : texto.length();
            tokens.add(texto.substring(begin, end));
            begin = end;
            if ((begin+1) > texto.length()) {
                break;
            }
        }
        String[] array = new String[tokens.size()];
        tokens.toArray(array);
        return array;
    }

    // </editor-fold>
}
