package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebException;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import mx.reder.wms.cfdi.entity.InformacionAduaneraCFD;
import mx.reder.wms.dao.entity.ASPELClienteDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDAO;
import mx.reder.wms.dao.entity.ASPELInformacionEnvioDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDAO;
import mx.reder.wms.dao.entity.ASPELVendedorDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.RutaCfdiDAO;
import mx.reder.wms.dao.entity.RutaDAO;
import mx.reder.wms.dao.entity.RutaFacturaDAO;
import mx.reder.wms.reports.ComprobantePDF;
import mx.reder.wms.to.ASPELFacturaDetallePedimentoTO;
import mx.reder.wms.to.ASPELFacturaDetalleTO;
import mx.reder.wms.util.Configuracion;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class PaqueteDocumentalBusiness {
    static Logger log = Logger.getLogger(PaqueteDocumentalBusiness.class);

    private DatabaseServices ds;
    private DatabaseServices dsA;

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }

    public void setDatabaseAspelServices(DatabaseServices dsA) {
        this.dsA = dsA;
    }

    public File paqueteDocumental(String compania, String usuario, String ruta) throws Exception {
        RutaDAO rutaDAO = (RutaDAO)ds.first(new RutaDAO(), "compania = '"+compania+"' AND ruta = '"+ruta+"' AND status = 'FA' AND fechacierre IS NULL", "id DESC");
        if (rutaDAO==null)
            throw new WebException("No encontre esta Ruta ["+compania+";"+ruta+"] con estado FA y fechacierre IS NULL");

        ArrayList<RutaFacturaDAO> facturas = ds.select(new RutaFacturaDAO(), "idruta = "+rutaDAO.id+" AND status = 'FA'", "parada, flsurtido");
        if (facturas.isEmpty())
            throw new WebException("No encontre ninguna factura en esta Ruta ["+rutaDAO+"]");

        File dir = new File(Configuracion.getInstance().getProperty("ruta.pdf"));
        if (!dir.exists())
            throw new WebException("No existe este directorio ["+dir.getAbsolutePath()+"]");

        Document document = new Document(PageSize.A4, 0, 0, 0, 0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();
        PdfContentByte content = writer.getDirectContent();

        for (RutaFacturaDAO rutaFacturaDAO : facturas) {
            ArrayList<RutaCfdiDAO> cfdis = ds.select(new RutaCfdiDAO(),
                    "compania = '"+rutaFacturaDAO.compania+"' AND flsurtido = "+rutaFacturaDAO.flsurtido+" AND idruta = "+rutaFacturaDAO.idruta+" AND status = 'A'");
            if (cfdis.isEmpty())
                throw new WebException("No encontre CFDIs de esta factura ["+rutaFacturaDAO.compania+";"+rutaFacturaDAO.flsurtido+"] con estado A");
            if (cfdis.size()>1)
                throw new WebException("Hay mas de 1 CFDIs de esta factura ["+rutaFacturaDAO.compania+";"+rutaFacturaDAO.flsurtido+"] con estado A");

            RutaCfdiDAO rutaCfdiDAO = cfdis.get(0);
            //
            // Para regeneralo otra ves, PRUEBAS
            //
            rutaCfdiDAO.pdf = null;
            //
            //
            //
            if (rutaCfdiDAO.pdf==null) {
                OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
                ordenSurtidoPedidoDAO.compania = rutaFacturaDAO.compania;
                ordenSurtidoPedidoDAO.flsurtido = rutaFacturaDAO.flsurtido;
                if (!ds.exists(ordenSurtidoPedidoDAO))
                    throw new WebException("No existe la Orden de Surtido del Pedido ["+ordenSurtidoPedidoDAO+"]");

                ASPELPedidoDAO aspelPedidoDAO = new ASPELPedidoDAO();
                aspelPedidoDAO.setEmpresa(rutaFacturaDAO.compania);
                aspelPedidoDAO.CVE_DOC = ordenSurtidoPedidoDAO.pedido;
                if (!dsA.exists(aspelPedidoDAO))
                    throw new WebException("No existe este Pedido ["+aspelPedidoDAO+"]");

                ASPELVendedorDAO aspelVendedorDAO = new ASPELVendedorDAO();
                aspelVendedorDAO.setEmpresa(rutaFacturaDAO.compania);
                aspelVendedorDAO.CVE_VEND = aspelPedidoDAO.CVE_VEND;
                if (!dsA.exists(aspelVendedorDAO))
                    throw new WebException("No existe este Vendedor ["+aspelVendedorDAO+"]");

                ASPELFacturaDAO aspelFacturaDAO = new ASPELFacturaDAO();
                aspelFacturaDAO.setEmpresa(rutaFacturaDAO.compania);
                aspelFacturaDAO.CVE_DOC = rutaFacturaDAO.factura;
                if (!dsA.exists(aspelFacturaDAO))
                    throw new WebException("No existe esta Factura ["+aspelPedidoDAO+"]");

                ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
                aspelClienteDAO.setEmpresa(rutaFacturaDAO.compania);
                aspelClienteDAO.CLAVE = aspelFacturaDAO.CVE_CLPV;
                if (!dsA.exists(aspelClienteDAO))
                    throw new WebException("No existe este Cliente ["+aspelClienteDAO+"]");

                ASPELInformacionEnvioDAO aspelInformacionEnvioDAO = new ASPELInformacionEnvioDAO();
                aspelInformacionEnvioDAO.setEmpresa(rutaFacturaDAO.compania);
                aspelInformacionEnvioDAO.CVE_INFO = aspelFacturaDAO.DAT_ENVIO;
                if (!dsA.exists(aspelInformacionEnvioDAO))
                    log.error("No existe esta Informacion de Envio ["+aspelInformacionEnvioDAO+"]");

                ArrayList<ASPELFacturaDetalleTO> detallesFactura = dsA.collection(new ASPELFacturaDetalleTO(),
                        "SELECT pf.CVE_DOC, pf.NUM_PAR, pf.CVE_ART, pf.CANT, pf.PXS, pf.PREC, pf.COST, pf.IMPU1, pf.IMPU2, pf.IMPU3, pf.IMPU4,"
                        +"pf.IMP1APLA, pf.IMP2APLA, pf.IMP3APLA, pf.IMP4APLA, pf.TOTIMP1, pf.TOTIMP2, pf.TOTIMP3, pf.TOTIMP4, pf.DESC1, pf.DESC2, pf.DESC3,"
                        +"pf.COMI, pf.APAR, pf.ACT_INV, pf.NUM_ALM, pf.POLIT_APLI, pf.TIP_CAM, pf.UNI_VENTA, pf.TIPO_PROD, pf.CVE_OBS, pf.REG_SERIE, pf.E_LTPD,"
                        +"pf.TIPO_ELEM, pf.NUM_MOV, pf.TOT_PARTIDA, pf.IMPRIMIR, pf.MAN_IEPS, pf.APL_MAN_IMP, pf.CUOTA_IEPS, pf.APL_MAN_IEPS, pf.MTO_PORC,"
                        +"pf.MTO_CUOTA, pf.CVE_ESQ, pf.DESCR_ART, pf.UUID, pf.VERSION_SINC, p.CVE_PRODSERV, p.CVE_UNIDAD, p.DESCR, p.UNI_MED, "
                        +"COALESCE(pl.CAMPLIB5, 0) AS PREPUB, pl.CAMPLIB7 AS SUSTANCIAACTIVA "
                        +"FROM PAR_FACTF"+rutaFacturaDAO.compania+" pf LEFT JOIN INVE"+rutaFacturaDAO.compania+" p ON pf.CVE_ART = p.CVE_ART "
                        +"LEFT JOIN INVE_CLIB"+rutaFacturaDAO.compania+" pl ON pf.CVE_ART = pl.CVE_PROD "
                        +"WHERE pf.CVE_DOC = '"+aspelFacturaDAO.CVE_DOC+"'");

                for (int indx=0; indx<detallesFactura.size(); indx++) {
                    ASPELFacturaDetalleTO aspelFacturaDetalleTO = detallesFactura.get(indx);

                    ArrayList<InformacionAduaneraCFD> pedimentos = dsA.collection(new ASPELFacturaDetallePedimentoTO(),
                            "SELECT pf.CVE_DOC, pf.NUM_PAR, pf.CVE_ART, pf.CANT, elp.E_LTPD, elp.REG_LTPD, elp.CANTIDAD, elp.PXRS, "
                            +"lp.LOTE, lp.PEDIMENTO, lp.CVE_ALM, lp.FCHCADUC, lp.FCHADUANA "
                            +"FROM PAR_FACTF"+rutaFacturaDAO.compania+" pf "
                            +"LEFT JOIN ENLACE_LTPD"+rutaFacturaDAO.compania+" elp ON pf.E_LTPD = elp.E_LTPD "
                            +"LEFT JOIN LTPD"+rutaFacturaDAO.compania+" lp ON elp.REG_LTPD = lp.REG_LTPD "
                            +"WHERE pf.CVE_DOC = '"+aspelFacturaDetalleTO.CVE_DOC+"' AND pf.NUM_PAR = "+aspelFacturaDetalleTO.NUM_PAR);
                    if (!pedimentos.isEmpty()) {
                        aspelFacturaDetalleTO.setInformacionAduanera(pedimentos.get(0));

                        detallesFactura.set(indx, aspelFacturaDetalleTO);
                    }
                }

                ComprobantePDF comprobantePDF = new ComprobantePDF();
                byte[] bytesPdf = comprobantePDF.pdf(ds, rutaCfdiDAO, rutaFacturaDAO, ordenSurtidoPedidoDAO,
                        aspelPedidoDAO, aspelVendedorDAO, aspelFacturaDAO, aspelClienteDAO, aspelInformacionEnvioDAO, detallesFactura);

                rutaCfdiDAO.pdf = bytesPdf;
                ds.update(rutaCfdiDAO, new String[] {"pdf"});

                File fileFactura = new File(dir, "Factura_"+rutaDAO.ruta+"_"+rutaFacturaDAO.factura+".pdf");
                log.debug("fileFactura: "+fileFactura.getAbsolutePath());

                FileOutputStream fos = new FileOutputStream(fileFactura);
                fos.write(bytesPdf);
                fos.close();
            }

            PdfReader reader = new PdfReader(new ByteArrayInputStream(rutaCfdiDAO.pdf));

            int currentPdfReaderPage = 1;
            while (currentPdfReaderPage <= reader.getNumberOfPages()) {
                document.newPage();
                PdfImportedPage pdfImportedPage = writer.getImportedPage(reader, currentPdfReaderPage);
                content.addTemplate(pdfImportedPage, 0, 0);
                currentPdfReaderPage++;
            }

            reader.close();
        }

        document.close();
        byte[] bytesPdf = out.toByteArray();

        File filePD = new File(dir, "PaqueteDocumental_"+rutaDAO.ruta+".pdf");
        log.debug("filePD: "+filePD.getAbsolutePath());

        FileOutputStream fos = new FileOutputStream(filePD);
        fos.write(bytesPdf);
        fos.close();

        // Fecha Paquete Documental
        rutaDAO.fechapaquetedocumental = new Date();
        ds.update(rutaDAO, new String[] {"fechapaquetedocumental"});

        return filePD;
    }
}
