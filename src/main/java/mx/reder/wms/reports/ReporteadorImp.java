package mx.reder.wms.reports;

import com.atcloud.dao.engine.DatabaseRecordEntity;
import java.io.BufferedOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.servlets.ReporteadorClass;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.log4j.Logger;

public class ReporteadorImp implements ReporteadorClass {
    static Logger log = Logger.getLogger(ReporteadorImp.class.getName());

    private DatabaseServices databaseServices;
    private ServletContext servletContext;

    public void reporteOrdenSurtidoPedidoCertificaTicket(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String flsurtido = request.getParameter("flsurtido");
        String contenedor = request.getParameter("contenedor");

        ArrayList<DatabaseRecordEntity> contenedores =
                //databaseServices.collection("SELECT DISTINCT contenedor "
                //        +"FROM OrdenSurtidoPedidoCertifica WHERE compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND certificadas > 0");
                databaseServices.collection("SELECT DISTINCT contenedor "
                        +"FROM OrdenSurtidoPedidoContenedor WHERE compania = '"+compania+"' AND flsurtido = "+flsurtido+" ");
        String leyendacontenedor = "";
        if (contenedor==null) {
            leyendacontenedor = "Total de Contenedores: "+contenedores.size();
        } else {
            int index = 0;
            for (DatabaseRecordEntity record : contenedores) {
                index ++;
                if (record.getString("contenedor").compareTo(contenedor)==0)
                    break;
            }
            leyendacontenedor = "Contenedor "+index+" / "+contenedores.size();
        }

        String file = OrdenSurtidoPedidoCertificaDS.class.getResource("OrdenSurtidoPedidoCertifica.jrxml").getFile();
        file = file.replaceAll("%20", " ");

        OrdenSurtidoPedidoCertificaDS ds = new OrdenSurtidoPedidoCertificaDS();
        ds.getDataTicket(databaseServices, compania, flsurtido, contenedor);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("logo", getLogo(compania));
        parameters.put("leyendacontenedor", leyendacontenedor);
        parameters.put("tipoFactura", ds.getTipoFactura());
        parameters.put("nombreVendedor", ds.getNombreVendedor());
        parameters.put("claveVendedor", ds.ordenSurtidoPedidoDAO.vendedor);
        parameters.put("totalLetras", ds.getTotalLetras());
        SimpleDateFormat format_simple = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format_large = new SimpleDateFormat("EEEEE dd MMMMM yyyy");
        parameters.put("fechaCredito", format_simple.format(ds.getFechaCredito()));
        parameters.put("fechaTicket", format_large.format(new java.util.Date()));
        parameters.put("fechaTicket1", format_simple.format(new java.util.Date()));
        parameters.put("credito", (ds.getDiasCredito() > 0 ? "CREDITO":"CONTADO"));
        parameters.put("referencia", ds.getReferencia());
   

        JasperReport jasperReport = JasperCompileManager.compileReport(file);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);
        BufferedOutputStream output = new BufferedOutputStream(response.getOutputStream());
        JasperExportManager.exportReportToPdfStream(jasperPrint, output);
    }
    
    public void reporteOrdenSurtidoPedidoCertificaDetalleTicket(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String flsurtido = request.getParameter("flsurtido");
        String contenedor = request.getParameter("contenedor");

        ArrayList<DatabaseRecordEntity> contenedores =
                //databaseServices.collection("SELECT DISTINCT contenedor "
                //        +"FROM OrdenSurtidoPedidoCertifica WHERE compania = '"+compania+"' AND flsurtido = "+flsurtido+" AND certificadas > 0");
                databaseServices.collection("SELECT DISTINCT contenedor "
                        +"FROM OrdenSurtidoPedidoContenedor WHERE compania = '"+compania+"' AND flsurtido = "+flsurtido+" ");
        String leyendacontenedor = "";
        if (contenedor==null) {
            leyendacontenedor = "Total de Contenedores: "+contenedores.size();
        } else {
            int index = 0;
            for (DatabaseRecordEntity record : contenedores) {
                index ++;
                if (record.getString("contenedor").compareTo(contenedor)==0)
                    break;
            }
            leyendacontenedor = "Contenedor "+index+" / "+contenedores.size();
        }

        String file = OrdenSurtidoPedidoCertificaDS.class.getResource("OrdenSurtidoPedidoCertificaDetalle.jrxml").getFile();
        file = file.replaceAll("%20", " ");

        OrdenSurtidoPedidoCertificaDS ds = new OrdenSurtidoPedidoCertificaDS();
        ds.getDataTicket(databaseServices, compania, flsurtido, contenedor);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("logo", getLogo(compania));
        parameters.put("leyendacontenedor", leyendacontenedor);
        parameters.put("tipoFactura", ds.getTipoFactura());
        parameters.put("nombreVendedor", ds.getNombreVendedor());
        parameters.put("claveVendedor", ds.ordenSurtidoPedidoDAO.vendedor);
        parameters.put("totalLetras", ds.getTotalLetras());
        SimpleDateFormat format_simple = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format_large = new SimpleDateFormat("EEEEE dd MMMMM yyyy");
        parameters.put("fechaCredito", format_simple.format(ds.getFechaCredito()));
        parameters.put("fechaTicket", format_large.format(new java.util.Date()));
        parameters.put("fechaTicket1", format_simple.format(new java.util.Date()));
        parameters.put("credito", (ds.getDiasCredito() > 0 ? "CREDITO":"CONTADO"));
        parameters.put("referencia", ds.getReferencia());
   

        JasperReport jasperReport = JasperCompileManager.compileReport(file);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);
        BufferedOutputStream output = new BufferedOutputStream(response.getOutputStream());
        JasperExportManager.exportReportToPdfStream(jasperPrint, output);
    }

    private String getLogo(String compania) {
        File fileLogo = new File(servletContext.getRealPath("/")+"/../wms-static/img/logo"+compania+".png");
        if (fileLogo.exists())
            return fileLogo.getAbsolutePath();
        return servletContext.getRealPath("/")+"/img/logo.png";
    }

    @Override
    public void setDatabaseServices(DatabaseServices databaseServices) {
        this.databaseServices = databaseServices;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setConnection(Connection connection) {
    }
}
