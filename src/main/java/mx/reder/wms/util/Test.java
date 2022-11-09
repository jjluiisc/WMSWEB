package mx.reder.wms.util;

import com.atcloud.dao.engine.DatabaseReflector;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.dao.engine.DatabaseTableColumn;
import com.atcloud.test.TestHttpServletRequest;
import com.atcloud.test.TestHttpServletResponse;
import com.atcloud.test.TestHttpSession;
import com.atcloud.test.TestServletContext;
import com.atcloud.util.CommonServices;
import com.atcloud.util.Fecha;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import mx.reder.wms.business.CartaPorteBusiness;
import mx.reder.wms.cfdi.EncriptacionFacade;
import mx.reder.wms.command.FacturaRutaCommand;
import mx.reder.wms.command.PaqueteDocumentalCommand;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.dao.entity.CertificadoSelloDigitalDAO;
import mx.reder.wms.reports.ReporteadorImp;
import mx.reder.wms.to.RutaFacturaCartaPorteTO;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Test {
    static Logger log = Logger.getLogger(Test.class);

    private boolean test = false;
    private String tabla_aspel = null;
    private String reporte = null;
    private String excel = null;
    private String ruta = null;
    private String paqueteDocumental = null;
    private String validaCSD = null;
    private String cartaPorte = null;

    private void parseArgs(String[] args) {
        for(int i=0; i<args.length; i++) {
            String arg = args[i];
            if (arg.compareTo("--test")==0)
                test = true;
            if (arg.compareTo("--reporte")==0)
                reporte = args[i+1];
            if (arg.compareTo("--excel")==0)
                excel = args[i+1];
            if (arg.compareTo("--tabla-aspel")==0)
                tabla_aspel = args[i+1];
            if (arg.compareTo("--ruta")==0)
                ruta = args[i+1];
            if (arg.compareTo("--paquete-documental")==0)
                paqueteDocumental = args[i+1];
            if (arg.compareTo("--valida-csd")==0)
                validaCSD = args[i+1];
            if (arg.compareTo("--carta-porte")==0)
                cartaPorte = args[i+1];
        }
    }

    public Test(String[] args) throws Exception {
        String log4jProp = System.getProperty("log4j");
        System.out.println("Initializing log4j with: "+log4jProp);
        PropertyConfigurator.configure(log4jProp);

        LogManager.getLogger("com.atcloud").setLevel(Level.DEBUG);
        LogManager.getLogger("mx.com.unirefacciones").setLevel(Level.DEBUG);

        parseArgs(args);

        if (tabla_aspel!=null) {
            log.debug("Iniciando Database Services ...");
            DatabaseDataSource dds = new DatabaseDataSource("reder");

            ArrayList columns = DatabaseReflector.getTableColumns(dds.getConnection(), null, tabla_aspel);
            for(Object object : columns) {
                DatabaseTableColumn dtc = (DatabaseTableColumn)object;
                //log.debug(dtc);

                String type = "";
                switch(dtc.type) {
                    case "VARCHAR":
                        type = "String";
                        break;
                    case "INTEGER":
                    case "SMALLINT":
                        type = "Integer";
                        break;
                    case "TIMESTAMP":
                        type = "Date";
                        break;
                    case "DOUBLE PRECISION":
                        type = "Double";
                        break;
                }
                System.out.println("    public "+type+" "+dtc.name+" = null;");
            }

            dds.close();
        }
        if (test) {
            /*try {
                DatabaseDataSource dds = new DatabaseDataSource("reder");
                Connection connection = dds.getConnection();
                DatabaseServices ds = new DatabaseServices(connection);

                dds.close();
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            }*/
        }
        if (excel!=null) {
            FileOutputStream fos = new FileOutputStream("reporte.xlsx");

            TestServletContext servletContext = new TestServletContext();
            servletContext.setContextPath("/opt/apache-tomcat-9.0.36/webapps/wms");

            TestHttpServletResponse response = new TestHttpServletResponse(fos);
            TestHttpServletRequest request = new TestHttpServletRequest();

            request.setParameter("usuario", "joelbecerram@gmail.com");
            request.setParameter("compania", "01");
            request.setParameter("reporte", "ABCProductos");
            request.setParameter("fechainicial", "2021-01-01");
            request.setParameter("fechafinal", "2021-06-01");

            CommonServices cs = new CommonServices();
            Connection connection = cs.getConnection("reder");
            DatabaseServices ds = new DatabaseServices(connection);

            //ExportadorExcelImp exportadorExcelImp = new ExportadorExcelImp();
            //exportadorExcelImp.setConnection(connection);
            //exportadorExcelImp.setDatabaseServices(ds);
            //exportadorExcelImp.setServletContext(servletContext);

            //exportadorExcelImp.exportaABCProductos(request, response);

            connection.close();

            fos.close();
        }
        if (reporte!=null) {
            FileOutputStream fos = new FileOutputStream("reporte.pdf");

            TestServletContext servletContext = new TestServletContext();
            servletContext.setContextPath("/opt/apache-tomcat-9.0.36/webapps/wms");

            TestHttpServletResponse response = new TestHttpServletResponse(fos);
            TestHttpServletRequest request = new TestHttpServletRequest();

            TestHttpSession session = new TestHttpSession();
            request.setSession(session);

            request.setParameter("usuario", "joelbecerram@gmail.com");
            request.setParameter("compania", "01");
            request.setParameter("flsurtido", "436");

            CommonServices cs = new CommonServices();
            Connection connection = cs.getConnection("reder");
            DatabaseServices ds = new DatabaseServices(connection);

            ReporteadorImp reporteadorImp = new ReporteadorImp();
            reporteadorImp.setServletContext(servletContext);
            reporteadorImp.setDatabaseServices(ds);

            reporteadorImp.reporteOrdenSurtidoPedidoCertificaTicket(request, response);

            connection.close();

            fos.close();
        }
        if (ruta!=null) {
            CommonServices cs = new CommonServices();
            Connection connection = cs.getConnection("reder");
            DatabaseServices ds = new DatabaseServices(connection);

            TestHttpServletResponse response = new TestHttpServletResponse(System.out);
            TestHttpServletRequest request = new TestHttpServletRequest();

            TestHttpSession session = new TestHttpSession();
            request.setSession(session);

            request.setParameter("usuario", "joelbecerram@gmail.com");
            request.setParameter("compania", "01");
            request.setParameter("ruta", ruta);

            try {
                FacturaRutaCommand command = new FacturaRutaCommand();
                command.execute(request, response, ds);
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                connection.close();
            }
        }
        if (paqueteDocumental!=null) {
            CommonServices cs = new CommonServices();
            Connection connection = cs.getConnection("reder");
            DatabaseServices ds = new DatabaseServices(connection);

            TestHttpServletResponse response = new TestHttpServletResponse(System.out);
            TestHttpServletRequest request = new TestHttpServletRequest();

            TestHttpSession session = new TestHttpSession();
            request.setSession(session);

            request.setParameter("usuario", "joelbecerram@gmail.com");
            request.setParameter("compania", "01");
            request.setParameter("ruta", paqueteDocumental);

            try {
                PaqueteDocumentalCommand command = new PaqueteDocumentalCommand();
                command.execute(request, response, ds);
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                connection.close();
            }
        }
        if (cartaPorte!=null) {
            CommonServices cs = new CommonServices();
            Connection connection = cs.getConnection("reder");
            DatabaseServices ds = new DatabaseServices(connection);

            Connection connectionA = cs.getConnection("REDER");
            DatabaseServices dsA = new DatabaseServices(connectionA);

            try {
                CartaPorteBusiness bussines = new CartaPorteBusiness();
                bussines.setDatabaseServices(ds);
                bussines.setDatabaseAspelServices(dsA);

                ArrayList<RutaFacturaCartaPorteTO> facturas = new ArrayList<>();

                RutaFacturaCartaPorteTO factura1 = new RutaFacturaCartaPorteTO();
                factura1.compania = "01";
                factura1.flsurtido = 664;
                factura1.fechahorallegada = Fecha.getFechaHora("2022-11-09 15:10:00");
                factura1.distanciarecorrida = 20.0d;
                facturas.add(factura1);

                RutaFacturaCartaPorteTO factura2 = new RutaFacturaCartaPorteTO();
                factura2.compania = "01";
                factura2.flsurtido = 665;
                factura2.fechahorallegada = Fecha.getFechaHora("2022-11-10 22:45:00");
                factura2.distanciarecorrida = 65.0d;
                facturas.add(factura2);

                bussines.cartaPorte("01", "    3", "   49", facturas);

            } catch(Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                connectionA.close();
                connection.close();
            }
        }
        if (validaCSD!=null) {
            CommonServices cs = new CommonServices();
            Connection connection = cs.getConnection("reder");
            DatabaseServices ds = new DatabaseServices(connection);

            try {
                String fecha = Fecha.getFechaHora();
                CertificadoSelloDigitalDAO certificadoSelloDigitalDAO = (CertificadoSelloDigitalDAO)ds.first(new CertificadoSelloDigitalDAO(),
                    "compania = '"+validaCSD+"' AND fechainicial <= '"+fecha+"' AND fechafinal >= '"+fecha+"'");
                if(certificadoSelloDigitalDAO==null)
                    throw new Exception("No existe CSD para la compania = '"+validaCSD+"' fecha = '"+fecha+"'");

                EncriptacionFacade.getInstance().validar(certificadoSelloDigitalDAO);

            } catch(Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                connection.close();
            }

        }
    }

    public static void main(String[] args) throws Exception {
        new Test(args);
    }
}
