package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Fecha;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import mx.reder.wms.cfdi.ComprobanteCFDImp;
import mx.reder.wms.cfdi.ExtraccionImp;
import mx.reder.wms.cfdi.RespuestaComprobante;
import mx.reder.wms.cfdi.TimbradoSWCFDImp;
import mx.reder.wms.cfdi.entity.ComprobanteCFD;
import mx.reder.wms.cfdi.entity.TimbradoCFD;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.dao.entity.ASPELBitacoraDAO;
import mx.reder.wms.dao.entity.ASPELCFDIDAO;
import mx.reder.wms.dao.entity.ASPELClienteDAO;
import mx.reder.wms.dao.entity.ASPELControlDAO;
import mx.reder.wms.dao.entity.ASPELCuentaPorCobrarDAO;
import mx.reder.wms.dao.entity.ASPELDocumentoSiguienteDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDetalleDAO;
import mx.reder.wms.dao.entity.ASPELFoliosDAO;
import mx.reder.wms.dao.entity.ASPELInformacionEnvioDAO;
import mx.reder.wms.dao.entity.ASPELMovimientoInventarioDAO;
import mx.reder.wms.dao.entity.ASPELObservacionesDocfDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDetalleDAO;
import mx.reder.wms.dao.entity.ASPELProductoAlmacenDAO;
import mx.reder.wms.dao.entity.ASPELProductoDAO;
import mx.reder.wms.dao.entity.ASPELVendedorDAO;
import mx.reder.wms.dao.entity.CompaniaDAO;
import mx.reder.wms.dao.entity.DireccionDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.PedidoBitacoraDAO;
import mx.reder.wms.to.ASPELFacturaDetalleTO;
import mx.reder.wms.to.FacturaOrdenSurtidoResponse;
import mx.reder.wms.to.FacturaRutaResponse;
import mx.reder.wms.util.Configuracion;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class FacturaRutaBusiness {
    static Logger log = Logger.getLogger(FacturaRutaBusiness.class);

    private DatabaseServices ds;
    private DatabaseServices dsAspel;

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }
    
    public void setDatabaseAspelServices(DatabaseServices dsAspel) {
        this.dsAspel = dsAspel;
    }

    public FacturaRutaResponse facturaRuta(String compania, String usuario, String ruta, String flsurtido) throws Exception {
        FacturaRutaResponse response = new FacturaRutaResponse();

        StringBuilder where = new StringBuilder();
        where.append("compania = '").append(compania)
                .append("' AND status = '").append(Constantes.ESTADO_CONFIRMADO)
                .append("' AND ruta = '").append(ruta).append("'");
        if (flsurtido!=null&&!flsurtido.isEmpty())
            where.append(" AND flsurtido = ").append(flsurtido);

        // Busca las pendientes por facturar
        response.ordenessurtido = ds.select(new OrdenSurtidoPedidoDAO(), where.toString());

        int size = response.ordenessurtido.size();

        // Llena los resultados, sin error vacios
        response.resultados = new ArrayList<>();
        for (int indx=0; indx<size; indx++) {
            ErrorTO errorTO = new ErrorTO();
            errorTO.error = false;

            response.resultados.add(errorTO);
        }

        for (int indx=0; indx<response.ordenessurtido.size(); indx++) {
            OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = response.ordenessurtido.get(indx);
            ErrorTO errorTO = response.resultados.get(indx);

            try {
                FacturaOrdenSurtidoResponse facturaOrdenSurtidoResponse = facturaOrdenSurtido(ordenSurtidoPedidoDAO.compania, ordenSurtidoPedidoDAO.flsurtido, usuario);

                ordenSurtidoPedidoDAO = facturaOrdenSurtidoResponse.ordenSurtidoPedidoDAO;
                errorTO.mensaje = facturaOrdenSurtidoResponse.aspelFacturaDAO.CVE_DOC;

            } catch(Exception e) {
                log.error(e.getMessage(), e);

                // Coloca el Error
                errorTO.error = true;
                errorTO.fromException(e);
            }

            response.ordenessurtido.set(indx, ordenSurtidoPedidoDAO);
            response.resultados.set(indx, errorTO);
        }
        
        return response;
    }

    private FacturaOrdenSurtidoResponse facturaOrdenSurtido(String compania, int flsurtido, String usuario) throws Exception {        
        FacturaOrdenSurtidoResponse response = new FacturaOrdenSurtidoResponse();
        response.ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        response.ordenSurtidoPedidoDAO.compania = compania;
        response.ordenSurtidoPedidoDAO.flsurtido = flsurtido;
        if (!ds.exists(response.ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+response.ordenSurtidoPedidoDAO+"]");

        if (response.ordenSurtidoPedidoDAO.status.compareTo(Constantes.ESTADO_CONFIRMADO)!=0)
            throw new WebException("El estado de la Orden de Surtido del Pedido ["+response.ordenSurtidoPedidoDAO.status+"] no es CO.");

        ASPELPedidoDAO aspelPedidoDAO = new ASPELPedidoDAO();
        aspelPedidoDAO.setEmpresa(compania);
        aspelPedidoDAO.CVE_DOC = response.ordenSurtidoPedidoDAO.pedido;
        if (!dsAspel.exists(aspelPedidoDAO))
            throw new WebException("No existe este Pedido ["+aspelPedidoDAO+"]");

        ds.beginTransaction();

        try {
            //
            // Almacen
            //
            int almacen = 1;
            String serieFactura = getSeriePedido(aspelPedidoDAO.CVE_DOC);
            log.debug("SerieFactura: "+serieFactura);

            // Checa disponibilidad de inventario de todos los detalles, en el almacen
            //checaDisponibilidadInventario(response, almacen);

            response.aspelFacturaDAO = creaFacturaASPEL(compania, aspelPedidoDAO, response.ordenSurtidoPedidoDAO, serieFactura, almacen);
            response.aspelFacturaDAO = timbraFactura(compania, response.aspelFacturaDAO);
            afectaInventario(compania, response.aspelFacturaDAO);
            generaCuentaPorCobrar(compania, response.aspelFacturaDAO);

            //
            // Bitacora y Estado de Pedido
            //
            bitacoraOrdenSurtido(response.ordenSurtidoPedidoDAO, usuario);
            estadoPedidoASPEL(response.ordenSurtidoPedidoDAO, aspelPedidoDAO, compania, usuario);

            //
            ds.commit();
        } catch(Exception e) {
            ds.rollback();
            throw e;
        }

        return response;
    }

    private void generaCuentaPorCobrar(String compania, ASPELFacturaDAO aspelFacturaDAO) throws Exception {
        ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
        aspelClienteDAO.setEmpresa(compania);
        aspelClienteDAO.CLAVE = aspelFacturaDAO.CVE_CLPV;
        if (!dsAspel.exists(aspelClienteDAO))
            throw new WebException("No existe este Cliente ["+aspelClienteDAO+"]");

        // Saldo del Cliente
        aspelClienteDAO.SALDO += aspelFacturaDAO.IMPORTE;
        dsAspel.update(aspelClienteDAO, new String[] {"SALDO"});

        //
        // Cuenta por Cobrar
        //
        ASPELCuentaPorCobrarDAO aspelCuentaPorCobrarDAO = new ASPELCuentaPorCobrarDAO();
        aspelCuentaPorCobrarDAO.setEmpresa(compania);

        aspelCuentaPorCobrarDAO.CVE_CLIE = aspelFacturaDAO.CVE_CLPV;
        aspelCuentaPorCobrarDAO.REFER = aspelFacturaDAO.CVE_DOC;
        aspelCuentaPorCobrarDAO.NUM_CPTO = 1;
        aspelCuentaPorCobrarDAO.NUM_CARGO = 1;
        aspelCuentaPorCobrarDAO.CVE_OBS = null;
        aspelCuentaPorCobrarDAO.NO_FACTURA = aspelFacturaDAO.CVE_DOC;
        aspelCuentaPorCobrarDAO.DOCTO = aspelFacturaDAO.CVE_DOC;
        aspelCuentaPorCobrarDAO.IMPORTE = aspelFacturaDAO.IMPORTE;
        aspelCuentaPorCobrarDAO.FECHA_APLI = aspelFacturaDAO.FECHA_DOC;
        aspelCuentaPorCobrarDAO.FECHA_VENC = aspelFacturaDAO.FECHA_VEN;
        aspelCuentaPorCobrarDAO.AFEC_COI = "A";
        aspelCuentaPorCobrarDAO.STRCVEVEND = aspelFacturaDAO.CVE_VEND;
        aspelCuentaPorCobrarDAO.NUM_MONED = aspelFacturaDAO.NUM_MONED;
        aspelCuentaPorCobrarDAO.TCAMBIO = aspelFacturaDAO.TIPCAMB;
        aspelCuentaPorCobrarDAO.IMPMON_EXT = Numero.redondea(aspelFacturaDAO.IMPORTE * aspelFacturaDAO.TIPCAMB);
        aspelCuentaPorCobrarDAO.FECHAELAB = aspelFacturaDAO.FECHAELAB;
        aspelCuentaPorCobrarDAO.CTLPOL = null;
        aspelCuentaPorCobrarDAO.CVE_FOLIO = null;
        aspelCuentaPorCobrarDAO.TIPO_MOV = "C";
        aspelCuentaPorCobrarDAO.CVE_BITA = null;
        aspelCuentaPorCobrarDAO.SIGNO = 1;
        aspelCuentaPorCobrarDAO.CVE_AUT = null;
        aspelCuentaPorCobrarDAO.USUARIO = 0;
        aspelCuentaPorCobrarDAO.ENTREGADA = "S";
        aspelCuentaPorCobrarDAO.FECHA_ENTREGA = aspelFacturaDAO.FECHA_DOC;
        aspelCuentaPorCobrarDAO.STATUS = "A";
        aspelCuentaPorCobrarDAO.REF_SIST = null;
        aspelCuentaPorCobrarDAO.UUID = aspelFacturaDAO.UUID;
        aspelCuentaPorCobrarDAO.VERSION_SINC = aspelFacturaDAO.VERSION_SINC;

        log.info(Reflector.toStringAllFields(aspelCuentaPorCobrarDAO));
        dsAspel.insert(aspelCuentaPorCobrarDAO);

        //
        // Bitacora
        //
        ASPELControlDAO aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(compania);

        // Folio de Bitacora
        aspelControlDAO.ID_TABLA = 62;
        if (!dsAspel.exists(aspelControlDAO))
            dsAspel.insert(aspelControlDAO);

        aspelControlDAO.ULT_CVE ++;
        dsAspel.update(aspelControlDAO);

        ASPELBitacoraDAO aspelBitacoraDAO = new ASPELBitacoraDAO();
        aspelBitacoraDAO.setEmpresa(compania);

        aspelBitacoraDAO.CVE_BITA = aspelControlDAO.ULT_CVE;
        aspelBitacoraDAO.CVE_CLIE = aspelCuentaPorCobrarDAO.CVE_CLIE;
        aspelBitacoraDAO.CVE_CAMPANIA = "_SAE_";
        aspelBitacoraDAO.CVE_ACTIVIDAD = "    2";
        aspelBitacoraDAO.FECHAHORA = new Date();
        aspelBitacoraDAO.CVE_USUARIO = 0;
        aspelBitacoraDAO.OBSERVACIONES = "No. [ "+aspelFacturaDAO.CVE_DOC+" "+Numero.getMoneda(aspelCuentaPorCobrarDAO.IMPMON_EXT)+" ] ";
        aspelBitacoraDAO.STATUS = "F";
        aspelBitacoraDAO.NOM_USUARIO = "SYSTEM";

        log.info(Reflector.toStringAllFields(aspelBitacoraDAO));
        dsAspel.insert(aspelBitacoraDAO);

        aspelCuentaPorCobrarDAO.CVE_BITA = aspelBitacoraDAO.CVE_BITA;
        dsAspel.update(aspelCuentaPorCobrarDAO, new String[] {"CVE_BITA"});
        
    }

    private void afectaInventario(String compania, ASPELFacturaDAO aspelFacturaDAO) throws Exception {
        ASPELControlDAO aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(compania);

        ASPELFacturaDetalleDAO aspelFacturaDetalleDAO = new ASPELFacturaDetalleDAO();
        aspelFacturaDetalleDAO.setEmpresa(compania);

        ArrayList<ASPELFacturaDetalleDAO> detalles = dsAspel.select(aspelFacturaDetalleDAO, aspelFacturaDAO.getWhere());
        for(ASPELFacturaDetalleDAO adfDAO : detalles) {
            aspelFacturaDetalleDAO = adfDAO;

            ASPELProductoDAO aspelProductoDAO = new ASPELProductoDAO();
            aspelProductoDAO.setEmpresa(compania);
            aspelProductoDAO.CVE_ART = aspelFacturaDetalleDAO.CVE_ART;

            if (!dsAspel.exists(aspelProductoDAO))
                throw new Exception("No existe este Producto ["+aspelProductoDAO+"]");

            log.info(Reflector.toStringAllFields(aspelProductoDAO));

            //
            // Existencia de Producto
            //
            aspelProductoDAO.EXIST -= aspelFacturaDetalleDAO.CANT;
            dsAspel.update(aspelProductoDAO, new String[] {"EXIST"});

            // Existencia MultiAlmacen
            ASPELProductoAlmacenDAO aspelProductoAlmacenDAO = new ASPELProductoAlmacenDAO();
            aspelProductoAlmacenDAO.setEmpresa(compania);

            aspelProductoAlmacenDAO.CVE_ART = aspelFacturaDetalleDAO.CVE_ART;
            aspelProductoAlmacenDAO.CVE_ALM = aspelFacturaDetalleDAO.NUM_ALM;
            if (!dsAspel.exists(aspelProductoAlmacenDAO))
                throw new Exception("No existe este Producto ["+aspelProductoDAO+"] en el MultiAlmacen ["+aspelProductoAlmacenDAO+"]");

            aspelProductoAlmacenDAO.EXIST -= aspelFacturaDetalleDAO.CANT;
            dsAspel.update(aspelProductoAlmacenDAO, new String[] {"EXIST"});

            // Folio de Movimiento
            aspelControlDAO.ID_TABLA = 32;
            if (!dsAspel.exists(aspelControlDAO))
                dsAspel.insert(aspelControlDAO);

            aspelControlDAO.ULT_CVE ++;
            dsAspel.update(aspelControlDAO);

            //
            // Movimiento de Inventario
            //
            ASPELMovimientoInventarioDAO aspelMovimientoInventarioDAO = new ASPELMovimientoInventarioDAO();
            aspelMovimientoInventarioDAO.setEmpresa(compania);

            aspelMovimientoInventarioDAO.CVE_ART = aspelFacturaDetalleDAO.CVE_ART;
            aspelMovimientoInventarioDAO.ALMACEN = aspelFacturaDetalleDAO.NUM_ALM;
            aspelMovimientoInventarioDAO.NUM_MOV = aspelFacturaDetalleDAO.NUM_MOV;
            aspelMovimientoInventarioDAO.CVE_CPTO = 51;
            aspelMovimientoInventarioDAO.FECHA_DOCU = aspelFacturaDAO.FECHA_DOC;
            aspelMovimientoInventarioDAO.TIPO_DOC = "F";
            aspelMovimientoInventarioDAO.REFER = aspelFacturaDAO.CVE_DOC;
            aspelMovimientoInventarioDAO.CLAVE_CLPV = aspelFacturaDAO.CVE_CLPV;
            aspelMovimientoInventarioDAO.VEND = aspelFacturaDAO.CVE_VEND;
            aspelMovimientoInventarioDAO.CANT = aspelFacturaDetalleDAO.CANT;
            aspelMovimientoInventarioDAO.CANT_COST = 0.0;
            aspelMovimientoInventarioDAO.PRECIO = aspelFacturaDetalleDAO.PREC;
            aspelMovimientoInventarioDAO.COSTO = aspelFacturaDetalleDAO.COST;
            aspelMovimientoInventarioDAO.AFEC_COI = null;
            aspelMovimientoInventarioDAO.CVE_OBS = null;
            aspelMovimientoInventarioDAO.REG_SERIE = aspelFacturaDetalleDAO.REG_SERIE;
            aspelMovimientoInventarioDAO.UNI_VENTA = aspelProductoDAO.UNI_MED;
            aspelMovimientoInventarioDAO.E_LTPD = aspelFacturaDetalleDAO.E_LTPD;
            aspelMovimientoInventarioDAO.EXIST_G = aspelProductoDAO.EXIST;
            aspelMovimientoInventarioDAO.EXISTENCIA = aspelProductoDAO.EXIST;
            aspelMovimientoInventarioDAO.TIPO_PROD = aspelProductoDAO.TIPO_ELE;
            aspelMovimientoInventarioDAO.FACTOR_CON = aspelProductoDAO.FAC_CONV;
            aspelMovimientoInventarioDAO.FECHAELAB = aspelFacturaDAO.FECHAELAB;
            aspelMovimientoInventarioDAO.CTLPOL = 0;
            aspelMovimientoInventarioDAO.CVE_FOLIO = String.valueOf(aspelControlDAO.ULT_CVE);
            aspelMovimientoInventarioDAO.SIGNO = -1;
            aspelMovimientoInventarioDAO.COSTEADO = "S";
            aspelMovimientoInventarioDAO.COSTO_PROM_INI = Numero.redondea(aspelProductoDAO.ULT_COSTO);
            aspelMovimientoInventarioDAO.COSTO_PROM_FIN = aspelMovimientoInventarioDAO.COSTO_PROM_INI;
            aspelMovimientoInventarioDAO.COSTO_PROM_GRAL = aspelMovimientoInventarioDAO.COSTO_PROM_INI;
            aspelMovimientoInventarioDAO.DESDE_INVE = "N";
            aspelMovimientoInventarioDAO.MOV_ENLAZADO = 0;

            log.info(Reflector.toStringAllFields(aspelMovimientoInventarioDAO));
            dsAspel.insert(aspelMovimientoInventarioDAO);
        }
        
    }

    private ASPELFacturaDAO timbraFactura(String compania, ASPELFacturaDAO aspelFacturaDAO) throws Exception {
        CompaniaDAO companiaDAO = new CompaniaDAO();
        companiaDAO.compania = compania;
        if (!ds.exists(companiaDAO))
            throw new WebException("No existe esta Compania ["+companiaDAO+"]");

        DireccionDAO direccionDAO = new DireccionDAO();
        direccionDAO.direccion = companiaDAO.direccion;
        if (!ds.exists(direccionDAO))
            throw new WebException("No existe esta Direccion ["+direccionDAO+"]");

        ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
        aspelClienteDAO.setEmpresa(compania);
        aspelClienteDAO.CLAVE = aspelFacturaDAO.CVE_CLPV;
        if (!dsAspel.exists(aspelClienteDAO))
            throw new WebException("No existe este Cliente ["+aspelClienteDAO+"]");

        log.debug(Reflector.toStringAllFields(aspelClienteDAO));
        log.debug("Forma de Pago ["+aspelClienteDAO.FORMADEPAGOSAT+"]");
        log.debug("Metodo de Pago ["+aspelClienteDAO.METODODEPAGO+"]");

        // Arreglo el Metodo de Pago
        if (aspelClienteDAO.METODODEPAGO==null||aspelClienteDAO.METODODEPAGO.matches("\\d+"))
            aspelClienteDAO.METODODEPAGO = "PPD";

        ASPELInformacionEnvioDAO aspelInformacionEnvioPDAO = new ASPELInformacionEnvioDAO();
        aspelInformacionEnvioPDAO.setEmpresa(compania);
        aspelInformacionEnvioPDAO.CVE_INFO = aspelFacturaDAO.DAT_ENVIO;
        if (!dsAspel.exists(aspelInformacionEnvioPDAO))
            throw new WebException("No existe la Direccion de Envio ["+aspelInformacionEnvioPDAO+"]");

        //
        //
        //

        ArrayList<ASPELFacturaDetalleTO> detalles = dsAspel.collection(new ASPELFacturaDetalleTO(),
                "SELECT pf.*, p.CVE_PRODSERV, p.CVE_UNIDAD, p.DESCR, p.UNI_MED "
                +"FROM REDER20.dbo.PAR_FACTF"+compania+" pf LEFT JOIN REDER20.dbo.INVE"+compania+" p ON pf.CVE_ART = p.CVE_ART "
                +"WHERE pf.CVE_DOC = '"+aspelFacturaDAO.CVE_DOC+"'");

        ComprobanteCFD comprobanteCFDImp = new ComprobanteCFDImp(companiaDAO, direccionDAO, aspelFacturaDAO,
                aspelClienteDAO, aspelInformacionEnvioPDAO, detalles);

        String urlTimbrado = Configuracion.getInstance().getProperty(compania+".timbrado.url");
        String userTimbrado = Configuracion.getInstance().getProperty(compania+".timbrado.usuario");
        String passwordTimbrado = Configuracion.getInstance().getProperty(compania+".timbrado.password");

        TimbradoCFD timbradoCFDImp = new TimbradoSWCFDImp(urlTimbrado, userTimbrado, passwordTimbrado);

        ExtraccionImp extraccionImp = new ExtraccionImp(compania);
        extraccionImp.setDatabaseServices(ds);
        RespuestaComprobante respuesta = extraccionImp.getComprobante(comprobanteCFDImp, timbradoCFDImp);

        Date fechaCertificacion = respuesta.getTfd().getTimbreFiscalDigital().getFechaTimbrado().getTime();

        ASPELCFDIDAO aspelCFDIDAO = new ASPELCFDIDAO();
        aspelCFDIDAO.setEmpresa(compania);

        aspelCFDIDAO.TIPO_DOC = aspelFacturaDAO.TIP_DOC;
        aspelCFDIDAO.CVE_DOC = aspelFacturaDAO.CVE_DOC;
        aspelCFDIDAO.VERSION = "1.1";
        aspelCFDIDAO.UUID = respuesta.getTfd().getTimbreFiscalDigital().getUUID();
        aspelCFDIDAO.NO_SERIE = respuesta.getCd().getComprobante().getNoCertificado();
        aspelCFDIDAO.FECHA_CERT = Fecha.getFecha(fechaCertificacion)+"T"+Fecha.getHora(fechaCertificacion);
        aspelCFDIDAO.FECHA_CANCELA = null;
        aspelCFDIDAO.XML_DOC = new String(respuesta.getXml());
        aspelCFDIDAO.XML_DOC_CANCELA = null;
        aspelCFDIDAO.DESGLOCEIMP1 = "N";
        aspelCFDIDAO.DESGLOCEIMP2 = "N";
        aspelCFDIDAO.DESGLOCEIMP3 = "N";
        aspelCFDIDAO.DESGLOCEIMP4 = "S";
        aspelCFDIDAO.MSJ_CANC = null;
        aspelCFDIDAO.PENDIENTE = "N";
        aspelCFDIDAO.CVE_USUARIO = 0;

        log.debug(Reflector.toStringAllFields(aspelCFDIDAO));

        dsAspel.save(aspelCFDIDAO);

        // UUID de Factura
        aspelFacturaDAO.UUID = aspelCFDIDAO.UUID;
        aspelFacturaDAO.VERSION_SINC = fechaCertificacion;
        dsAspel.update(aspelFacturaDAO, new String[] {"UUID", "VERSION_SINC"});

        return aspelFacturaDAO;
    }

    private ASPELFacturaDAO creaFacturaASPEL(String compania, ASPELPedidoDAO aspelPedidoDAO, OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO, String serieFactura, int almacen) throws Exception {
        ASPELInformacionEnvioDAO aspelInformacionEnvioPDAO = new ASPELInformacionEnvioDAO();
        aspelInformacionEnvioPDAO.setEmpresa(compania);
        aspelInformacionEnvioPDAO.CVE_INFO = aspelPedidoDAO.DAT_ENVIO;
        if (!dsAspel.exists(aspelInformacionEnvioPDAO))
            throw new WebException("No existe la Direccion de Envio ["+aspelInformacionEnvioPDAO+"]");

        ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
        aspelClienteDAO.setEmpresa(compania);
        aspelClienteDAO.CLAVE = aspelPedidoDAO.CVE_CLPV;
        if (!dsAspel.exists(aspelClienteDAO))
            throw new WebException("No existe este Cliente ["+aspelClienteDAO+"]");

        ASPELVendedorDAO aspelVendedorDAO = new ASPELVendedorDAO();
        aspelVendedorDAO.setEmpresa(compania);
        aspelVendedorDAO.CVE_VEND = aspelPedidoDAO.CVE_VEND;
        if (!dsAspel.exists(aspelVendedorDAO))
            throw new WebException("No existe este Vendedor ["+aspelVendedorDAO+"]");

        ASPELFoliosDAO aspelFoliosDAO = new ASPELFoliosDAO();
        aspelFoliosDAO.setEmpresa(compania);

        aspelFoliosDAO = (ASPELFoliosDAO)dsAspel.first(aspelFoliosDAO, "TIP_DOC = 'F' AND SERIE = '"+serieFactura+"'");
        if (aspelFoliosDAO==null)
            throw new WebException("No existe Foleador para TIP_DOC = 'F' y SERIE = '"+serieFactura+"'");

        aspelFoliosDAO.setEmpresa(compania);
        aspelFoliosDAO.ULT_DOC ++;
        aspelFoliosDAO.FECH_ULT_DOC = new Date();

        log.info(Reflector.toStringAllFields(aspelFoliosDAO));
        dsAspel.update(aspelFoliosDAO, new String[] {"ULT_DOC", "FECH_ULT_DOC"});

        //
        // Factura
        //
        ASPELFacturaDAO aspelFacturaDAO = new ASPELFacturaDAO();
        aspelFacturaDAO.setEmpresa(compania);

        aspelFacturaDAO.TIP_DOC = "F";
        aspelFacturaDAO.CVE_DOC = aspelFoliosDAO.SERIE+aspelFoliosDAO.ULT_DOC;
        aspelFacturaDAO.CVE_CLPV = aspelClienteDAO.CLAVE;
        aspelFacturaDAO.STATUS = "E";
        aspelFacturaDAO.DAT_MOSTR = 0;
        aspelFacturaDAO.CVE_VEND = aspelVendedorDAO.CVE_VEND;
        aspelFacturaDAO.CVE_PEDI = aspelPedidoDAO.CVE_PEDI;
        aspelFacturaDAO.FECHA_DOC = Fecha.getDate(Fecha.getFecha()+" 00:00:00");
        aspelFacturaDAO.FECHA_ENT = aspelFacturaDAO.FECHA_DOC;
        aspelFacturaDAO.FECHA_VEN = aspelFacturaDAO.FECHA_DOC;
        aspelFacturaDAO.FECHA_CANCELA = null;
        aspelFacturaDAO.CONDICION = null;
        aspelFacturaDAO.CVE_OBS = 0;
        aspelFacturaDAO.NUM_ALMA = almacen;
        aspelFacturaDAO.ACT_CXC = "S";
        aspelFacturaDAO.ACT_COI = "N";
        aspelFacturaDAO.ENLAZADO = "O";
        aspelFacturaDAO.TIP_DOC_E = "P";
        aspelFacturaDAO.NUM_MONED = 1;
        aspelFacturaDAO.TIPCAMB = 1.0;
        aspelFacturaDAO.NUM_PAGOS = 1;
        aspelFacturaDAO.FECHAELAB = new Date();
        aspelFacturaDAO.PRIMERPAGO = 0.0;
        aspelFacturaDAO.RFC = aspelClienteDAO.RFC;
        aspelFacturaDAO.CTLPOL = 0;
        aspelFacturaDAO.ESCFD = "T";
        aspelFacturaDAO.AUTORIZA = 0;
        aspelFacturaDAO.SERIE = aspelFoliosDAO.SERIE;
        aspelFacturaDAO.FOLIO = aspelFoliosDAO.ULT_DOC;
        aspelFacturaDAO.AUTOANIO = "";
        aspelFacturaDAO.CONTADO = "N";
        aspelFacturaDAO.CVE_BITA = 0;
        aspelFacturaDAO.BLOQ = "N";
        aspelFacturaDAO.FORMAENVIO = "A";
        aspelFacturaDAO.METODODEPAGO = aspelClienteDAO.METODODEPAGO;
        aspelFacturaDAO.NUMCTAPAGO = aspelClienteDAO.NUMCTAPAGO;
        aspelFacturaDAO.TIP_DOC_ANT = aspelPedidoDAO.TIP_DOC;
        aspelFacturaDAO.DOC_ANT = aspelPedidoDAO.CVE_DOC;
        aspelFacturaDAO.TIP_DOC_SIG = null;
        aspelFacturaDAO.DOC_SIG = null;
        aspelFacturaDAO.UUID = "";
        aspelFacturaDAO.VERSION_SINC = new Date();
        aspelFacturaDAO.FORMADEPAGOSAT = aspelClienteDAO.FORMADEPAGOSAT;
        aspelFacturaDAO.USO_CFDI = aspelClienteDAO.USO_CFDI;

        // Cliente Extranjero
        //if (pedidoDAO.clavepais!=null&&!pedidoDAO.clavepais.isBlank())
        //    aspelFacturaDAO.NUM_MONED = 2;

        log.info(Reflector.toStringAllFields(aspelFacturaDAO));
        dsAspel.insert(aspelFacturaDAO);

        // Enlazo el Pedido
        aspelPedidoDAO.STATUS = "E";
        aspelPedidoDAO.ENLAZADO = "P";
        aspelPedidoDAO.TIP_DOC_E = aspelFacturaDAO.TIP_DOC;
        aspelPedidoDAO.TIP_DOC_SIG = aspelFacturaDAO.TIP_DOC;
        aspelPedidoDAO.DOC_SIG = aspelFacturaDAO.CVE_DOC;
        dsAspel.update(aspelPedidoDAO, new String[] {"STATUS", "ENLAZADO", "TIP_DOC_E", "TIP_DOC_SIG", "DOC_SIG"});

        //
        // Observaciones
        //
        ASPELControlDAO aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(compania);

        // Folio de Observaciones
        aspelControlDAO.ID_TABLA = 56;
        if (!dsAspel.exists(aspelControlDAO))
            dsAspel.insert(aspelControlDAO);

        aspelControlDAO.ULT_CVE ++;
        dsAspel.update(aspelControlDAO);

        ASPELObservacionesDocfDAO aspelObservacionesDocfDAO = new ASPELObservacionesDocfDAO();
        aspelObservacionesDocfDAO.setEmpresa(compania);

        aspelObservacionesDocfDAO.CVE_OBS = aspelControlDAO.ULT_CVE;
        aspelObservacionesDocfDAO.STR_OBS = "";

        log.info(Reflector.toStringAllFields(aspelObservacionesDocfDAO));
        dsAspel.insert(aspelObservacionesDocfDAO);

        aspelFacturaDAO.CVE_OBS = aspelObservacionesDocfDAO.CVE_OBS;
        dsAspel.update(aspelFacturaDAO, new String[] {"CVE_OBS"});

        //
        // Bitacora
        //
        aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(compania);

        // Folio de Bitacora
        aspelControlDAO.ID_TABLA = 62;
        if (!dsAspel.exists(aspelControlDAO))
            dsAspel.insert(aspelControlDAO);

        aspelControlDAO.ULT_CVE ++;
        dsAspel.update(aspelControlDAO);

        ASPELBitacoraDAO aspelBitacoraDAO = new ASPELBitacoraDAO();
        aspelBitacoraDAO.setEmpresa(compania);

        aspelBitacoraDAO.CVE_BITA = aspelControlDAO.ULT_CVE;
        aspelBitacoraDAO.CVE_CLIE = aspelClienteDAO.CLAVE;
        aspelBitacoraDAO.CVE_CAMPANIA = "_SAE_";
        aspelBitacoraDAO.CVE_ACTIVIDAD = "    2";
        aspelBitacoraDAO.FECHAHORA = new Date();
        aspelBitacoraDAO.CVE_USUARIO = 0;
        aspelBitacoraDAO.OBSERVACIONES = "No. [ "+aspelFacturaDAO.CVE_DOC+" ] ";
        aspelBitacoraDAO.STATUS = "F";
        aspelBitacoraDAO.NOM_USUARIO = "SYSTEM";

        log.info(Reflector.toStringAllFields(aspelBitacoraDAO));
        dsAspel.insert(aspelBitacoraDAO);

        aspelFacturaDAO.CVE_BITA = aspelBitacoraDAO.CVE_BITA;
        dsAspel.update(aspelFacturaDAO, new String[] {"CVE_BITA"});

        //
        // Info de Envio de Pedido
        //
        aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(compania);

        // Folio de Informacion de Envio
        aspelControlDAO.ID_TABLA = 70;
        if (!dsAspel.exists(aspelControlDAO))
            dsAspel.insert(aspelControlDAO);

        aspelControlDAO.ULT_CVE ++;
        dsAspel.update(aspelControlDAO);

        ASPELInformacionEnvioDAO aspelInformacionEnvioDAO = new ASPELInformacionEnvioDAO();
        aspelInformacionEnvioDAO.setEmpresa(compania);

        Reflector.copyAllFields(aspelInformacionEnvioPDAO, aspelInformacionEnvioDAO);
        aspelInformacionEnvioDAO.CVE_INFO = aspelControlDAO.ULT_CVE;

        log.info(Reflector.toStringAllFields(aspelInformacionEnvioDAO));
        dsAspel.insert(aspelInformacionEnvioDAO);

        aspelFacturaDAO.DAT_ENVIO = aspelInformacionEnvioDAO.CVE_INFO;
        dsAspel.update(aspelFacturaDAO, new String[] {"DAT_ENVIO"});


        //
        // Detalles de Factura
        //

        int iPartida = 0;
        double totImporte = 0.0;
        double totIeps = 0.0;
        double totIva = 0.0;
        double totDescuento = 0.0;

        ArrayList<OrdenSurtidoPedidoCertificaDAO> detallesCertifica = ds.select(new OrdenSurtidoPedidoCertificaDAO(), ordenSurtidoPedidoDAO.getWhere());
        for(OrdenSurtidoPedidoCertificaDAO ordenSurtidoPedidoCertificaDAO : detallesCertifica) {

            ASPELProductoDAO aspelProductoDAO = new ASPELProductoDAO();
            aspelProductoDAO.setEmpresa(compania);
            aspelProductoDAO.CVE_ART = ordenSurtidoPedidoCertificaDAO.codigo;

            if (!dsAspel.exists(aspelProductoDAO))
                throw new Exception("No existe este Producto ["+aspelProductoDAO+"]");

            log.info(Reflector.toStringAllFields(aspelProductoDAO));

            // Folio de Movimiento
            aspelControlDAO.ID_TABLA = 44;
            if (!dsAspel.exists(aspelControlDAO))
                dsAspel.insert(aspelControlDAO);

            aspelControlDAO.ULT_CVE ++;
            dsAspel.update(aspelControlDAO);

            iPartida ++;

            // Detalle de Pedido
            ASPELPedidoDetalleDAO aspelPedidoDetalleDAO = new ASPELPedidoDetalleDAO();
            aspelPedidoDetalleDAO.setEmpresa(compania);
            aspelPedidoDetalleDAO = (ASPELPedidoDetalleDAO)dsAspel.first(aspelPedidoDetalleDAO,
                    "CVE_DOC = '"+aspelPedidoDAO.CVE_DOC+"' AND CVE_ART = '"+aspelProductoDAO.CVE_ART+"'");
            if (aspelPedidoDetalleDAO==null)
                throw new WebException("No encontre el Detalle de Pedido ASPEL ["+aspelPedidoDAO.CVE_DOC+";"+aspelProductoDAO.CVE_ART+"].");

            // La perdio en el first(), la coloco de nuevo
            aspelPedidoDetalleDAO.setEmpresa(compania);

            // Detalle de Factura
            ASPELFacturaDetalleDAO aspelFacturaDetalleDAO = new ASPELFacturaDetalleDAO();
            aspelFacturaDetalleDAO.setEmpresa(compania);

            aspelFacturaDetalleDAO.CVE_DOC = aspelFacturaDAO.CVE_DOC;
            aspelFacturaDetalleDAO.NUM_PAR = iPartida;
            aspelFacturaDetalleDAO.CVE_ART = aspelProductoDAO.CVE_ART;
            aspelFacturaDetalleDAO.CANT = Numero.redondea(ordenSurtidoPedidoCertificaDAO.certificadas);
            aspelFacturaDetalleDAO.PXS = aspelFacturaDetalleDAO.CANT;
            aspelFacturaDetalleDAO.PREC = Numero.redondea(aspelPedidoDetalleDAO.PREC);
            aspelFacturaDetalleDAO.COST = Numero.redondea(aspelProductoDAO.ULT_COSTO);
            aspelFacturaDetalleDAO.IMPU1 = Numero.redondea(aspelPedidoDetalleDAO.IMPU1);
                                                     // PORCENTAJE DE IEPS
            aspelFacturaDetalleDAO.IMPU2 = 0.0;      // PORCENTAJE DE ISR
            aspelFacturaDetalleDAO.IMPU3 = 0.0;      // PORCENTAJE DE RETENCION IVA
            aspelFacturaDetalleDAO.IMPU4 = Numero.redondea(aspelPedidoDetalleDAO.IMPU4);
                                                     // PORCENTAJE DE IVA
            aspelFacturaDetalleDAO.IMP1APLA = 0;
            aspelFacturaDetalleDAO.IMP2APLA = 0;
            aspelFacturaDetalleDAO.IMP3APLA = 0;
            aspelFacturaDetalleDAO.IMP4APLA = 0;
            aspelFacturaDetalleDAO.TOTIMP1 = Numero.redondea(aspelFacturaDetalleDAO.CANT * aspelFacturaDetalleDAO.PREC * (aspelFacturaDetalleDAO.IMPU1 / 100.0));
            totIeps += aspelFacturaDetalleDAO.TOTIMP1;
                                                     // TOTAL EN MONTO DE IEPS
            aspelFacturaDetalleDAO.TOTIMP2 = 0.0;    // TOTAL EN MONTO DE RET. ISR
            aspelFacturaDetalleDAO.TOTIMP3 = 0.0;    // TOTAL EN MONTO DE RET. IVA
            aspelFacturaDetalleDAO.TOTIMP4 = Numero.redondea(aspelFacturaDetalleDAO.CANT * aspelFacturaDetalleDAO.PREC * (aspelFacturaDetalleDAO.IMPU4 / 100.0));
            totIva += aspelFacturaDetalleDAO.TOTIMP4;
                                                     // TOTAL EN MONTO DE IVA DE LA PARTIDA
            aspelFacturaDetalleDAO.DESC1 = 0.0;      // PORCENTAJE DE DESCUENTO 1 POR PARTIDA
            aspelFacturaDetalleDAO.DESC2 = 0.0;      // PORCENTAJE DE DESCUENTO 2 POR PARTIDA
            aspelFacturaDetalleDAO.DESC3 = 0.0;      // PORCENTAJE DE DESCUENTO 3 POR PARTIDA
            aspelFacturaDetalleDAO.COMI = Numero.redondea2(aspelPedidoDetalleDAO.COMI);
                                                     // PORCENTAJE DE COMISION
            aspelFacturaDetalleDAO.APAR = aspelFacturaDetalleDAO.CANT;
            aspelFacturaDetalleDAO.ACT_INV = "S";
            aspelFacturaDetalleDAO.NUM_ALM = aspelFacturaDAO.NUM_ALMA;
            aspelFacturaDetalleDAO.POLIT_APLI = "";
            aspelFacturaDetalleDAO.TIP_CAM = 1.0;
            aspelFacturaDetalleDAO.UNI_VENTA = aspelProductoDAO.UNI_MED;
            aspelFacturaDetalleDAO.TIPO_PROD = aspelProductoDAO.TIPO_ELE;
            aspelFacturaDetalleDAO.CVE_OBS = 0;
            aspelFacturaDetalleDAO.REG_SERIE = 0;
            aspelFacturaDetalleDAO.E_LTPD = 0;
            aspelFacturaDetalleDAO.TIPO_ELEM = "N";
            aspelFacturaDetalleDAO.NUM_MOV = aspelControlDAO.ULT_CVE;
            aspelFacturaDetalleDAO.TOT_PARTIDA = Numero.redondea(aspelFacturaDetalleDAO.CANT * aspelFacturaDetalleDAO.PREC);
            totImporte += aspelFacturaDetalleDAO.TOT_PARTIDA;
            aspelFacturaDetalleDAO.IMPRIMIR = "";
            aspelFacturaDetalleDAO.UUID = aspelProductoDAO.UUID;
            aspelFacturaDetalleDAO.VERSION_SINC = aspelProductoDAO.VERSION_SINC;
            aspelFacturaDetalleDAO.MAN_IEPS = aspelProductoDAO.MAN_IEPS;
            aspelFacturaDetalleDAO.APL_MAN_IMP = aspelProductoDAO.APL_MAN_IMP;
            aspelFacturaDetalleDAO.CUOTA_IEPS = aspelProductoDAO.CUOTA_IEPS;
            aspelFacturaDetalleDAO.APL_MAN_IEPS = aspelProductoDAO.APL_MAN_IEPS;
            aspelFacturaDetalleDAO.MTO_PORC = null;
            aspelFacturaDetalleDAO.MTO_CUOTA = null;
            aspelFacturaDetalleDAO.CVE_ESQ = aspelProductoDAO.CVE_ESQIMPU;
            aspelFacturaDetalleDAO.DESCR_ART = aspelProductoDAO.DESCR;

            //
            // Descuento
            //
            if (aspelPedidoDetalleDAO.DESC1>=0) {
                aspelFacturaDetalleDAO.DESC1 = Numero.redondea(aspelPedidoDetalleDAO.DESC1);
                double partidaDescuento = Numero.redondea(aspelFacturaDetalleDAO.TOT_PARTIDA * (aspelFacturaDetalleDAO.DESC1 / 100.0));
                totDescuento += partidaDescuento;

                totIva -= aspelFacturaDetalleDAO.TOTIMP4;

                aspelFacturaDetalleDAO.TOTIMP4 = Numero.redondea((aspelFacturaDetalleDAO.TOT_PARTIDA - partidaDescuento) * (aspelFacturaDetalleDAO.IMPU4 / 100.0));
                totIva += aspelFacturaDetalleDAO.TOTIMP4;
            }

            log.info(Reflector.toStringAllFields(aspelFacturaDetalleDAO));
            dsAspel.insert(aspelFacturaDetalleDAO);

            //
            // Piezas por Surtir
            //
            double facturadas = Numero.redondea(aspelFacturaDetalleDAO.CANT);

            aspelProductoDAO.PEND_SURT -= facturadas;
            dsAspel.update(aspelProductoDAO, new String[] {"PEND_SURT"});

            aspelPedidoDetalleDAO.PXS -= facturadas;
            dsAspel.update(aspelPedidoDetalleDAO, new String[] {"PXS"});

            // Documento Anterior Enlazado
            ASPELDocumentoSiguienteDAO aspelDocumentoAnteriorDAO = new ASPELDocumentoSiguienteDAO();
            aspelDocumentoAnteriorDAO.setEmpresa(compania);
            aspelDocumentoAnteriorDAO.TIP_DOC = aspelFacturaDAO.TIP_DOC;
            aspelDocumentoAnteriorDAO.CVE_DOC = aspelFacturaDAO.CVE_DOC;
            aspelDocumentoAnteriorDAO.ANT_SIG = "A";
            aspelDocumentoAnteriorDAO.TIP_DOC_E = "P";
            aspelDocumentoAnteriorDAO.CVE_DOC_E = aspelPedidoDAO.CVE_DOC;
            aspelDocumentoAnteriorDAO.PARTIDA = aspelFacturaDetalleDAO.NUM_PAR;
            aspelDocumentoAnteriorDAO.PART_E = aspelPedidoDetalleDAO.NUM_PAR;
            aspelDocumentoAnteriorDAO.CANT_E = facturadas;
            dsAspel.insert(aspelDocumentoAnteriorDAO);

            // Documento Siguiente Enlazado
            ASPELDocumentoSiguienteDAO aspelDocumentoSiguienteDAO = new ASPELDocumentoSiguienteDAO();
            aspelDocumentoSiguienteDAO.setEmpresa(compania);
            aspelDocumentoSiguienteDAO.TIP_DOC = "P";
            aspelDocumentoSiguienteDAO.CVE_DOC = aspelPedidoDAO.CVE_DOC;
            aspelDocumentoSiguienteDAO.ANT_SIG = "S";
            aspelDocumentoSiguienteDAO.TIP_DOC_E = aspelFacturaDAO.TIP_DOC;
            aspelDocumentoSiguienteDAO.CVE_DOC_E = aspelFacturaDAO.CVE_DOC;
            aspelDocumentoSiguienteDAO.PARTIDA = aspelPedidoDetalleDAO.NUM_PAR;
            aspelDocumentoSiguienteDAO.PART_E = aspelFacturaDetalleDAO.NUM_PAR;
            aspelDocumentoSiguienteDAO.CANT_E = facturadas;
            dsAspel.insert(aspelDocumentoSiguienteDAO);
        }

        // Si no hay piezas por surtir, el ENLAZADO debe de cambiar a T
        double PXS = 0;
        ASPELPedidoDetalleDAO aspelPedidoDetalleDAO = new ASPELPedidoDetalleDAO();
        aspelPedidoDetalleDAO.setEmpresa(compania);

        ArrayList<ASPELPedidoDetalleDAO> detallesP = dsAspel.select(aspelPedidoDetalleDAO, aspelPedidoDAO.getWhere());
        for (int indx=0; indx<detallesP.size(); indx++) {
            aspelPedidoDetalleDAO = detallesP.get(indx);
            PXS += aspelPedidoDetalleDAO.PXS;
        }
        if (PXS==0) {
            aspelPedidoDAO.ENLAZADO = "T";
            dsAspel.update(aspelPedidoDAO, new String[] {"ENLAZADO"});
        }

        //
        // Totales de Factura
        //

        aspelFacturaDAO.DES_TOT_PORC = 0.0;
        aspelFacturaDAO.DES_FIN_PORC = 0.0;
        aspelFacturaDAO.DES_TOT = totDescuento;  // DESCUENTO EN MONTO APLICADO POR VOLUMEN
        aspelFacturaDAO.DES_FIN = 0.0;           // DESCUENTO EN PESOS APLICADO POR  PAGO DE CONTADO

        aspelFacturaDAO.CAN_TOT = totImporte;    // MONTO DEL PEDIDO SIN IVA Y SIN DESCUENTOS
        aspelFacturaDAO.IMP_TOT1 = totIeps;      // TOTAL DE IEPS
        aspelFacturaDAO.IMP_TOT2 = 0.0;          // TOTAL DE RETENCION IVA
        aspelFacturaDAO.IMP_TOT3 = 0.0;          // TOTAL  DE RETENCION ISR
        aspelFacturaDAO.IMP_TOT4 = totIva;       // TOTAL DE IVA
        aspelFacturaDAO.COM_TOT = Numero.redondea2(aspelPedidoDAO.COM_TOT);
                                                 // COMISION GENERADA
        aspelFacturaDAO.IMPORTE = totImporte - totDescuento + totIva + totIeps;
                                                 // MONTO TOTAL MENOS DESCUENTO
        aspelFacturaDAO.COM_TOT_PORC = Numero.redondea2(aspelPedidoDAO.COM_TOT_PORC);
                                                 // PORCENTAJE DE LA COMISIÓN

        log.info(Reflector.toStringAllFields(aspelFacturaDAO));
        dsAspel.update(aspelFacturaDAO, new String[] {"CAN_TOT", "IMP_TOT1", "IMP_TOT2", "IMP_TOT3", "IMP_TOT4",
            "DES_TOT_PORC", "DES_FIN_PORC", "DES_TOT", "DES_FIN", "COM_TOT", "IMPORTE", "COM_TOT_PORC"});

        //
        return aspelFacturaDAO;
    }

    private void bitacoraOrdenSurtido(OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO, String usuario) throws Exception {
        ordenSurtidoPedidoDAO.fechafacturada = new Date();
        ordenSurtidoPedidoDAO.status = Constantes.ESTADO_FACTURADO;
        ordenSurtidoPedidoDAO.fechastatus = new Date();
        ordenSurtidoPedidoDAO.usuario = usuario;

        ds.update(ordenSurtidoPedidoDAO, new String[] {"fechafacturada", "status", "fechastatus", "usuario"});

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoBitacoraDAO.id = null;
        ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO.usuario = usuario;
        ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_FACTURADO;

        ds.insert(ordenSurtidoPedidoBitacoraDAO);
    }

    private void estadoPedidoASPEL(OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO, ASPELPedidoDAO aspelPedidoDAO, String compania, String usuario) throws Exception {
        //
        // Estado del Pedido en ASPEL
        //
        String wherePedido = "compania = '"+compania+"' AND pedido = '"+ordenSurtidoPedidoDAO.pedido+"'";
        int count = ds.count(ordenSurtidoPedidoDAO, wherePedido);
        int countFA = ds.count(ordenSurtidoPedidoDAO, wherePedido+" AND status IN ('CA','FA')");

        if (count==countFA) {
            //
            aspelPedidoDAO.STATUS = Constantes.ESTADO_ASPEL_EMITIDO;

            dsAspel.update(aspelPedidoDAO, new String[] {"STATUS"});

            PedidoBitacoraDAO pedidoBitacoraDAO = new PedidoBitacoraDAO();
            pedidoBitacoraDAO.id = null;
            pedidoBitacoraDAO.compania = compania;
            pedidoBitacoraDAO.pedido = aspelPedidoDAO.CVE_DOC;
            pedidoBitacoraDAO.status = Constantes.ESTADO_PENDIENTE;
            pedidoBitacoraDAO.fechabitacora = new Date();
            pedidoBitacoraDAO.usuario = usuario;

            ds.insert(pedidoBitacoraDAO);
        }       
        
    }

    private String getSeriePedido(String CVE_DOC) {
        StringBuilder serie = new StringBuilder();

        char[] chars = CVE_DOC.toCharArray();
        for (char c : chars) {
            if (Character.isDigit(c))
                break;
            serie.append(c);
        }

        return serie.toString();
    }
}
