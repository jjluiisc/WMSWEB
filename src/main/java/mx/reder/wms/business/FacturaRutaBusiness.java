package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Fecha;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import mx.gob.sat.cfd.x4.ComprobanteDocument;
import mx.gob.sat.timbreFiscalDigital.TimbreFiscalDigitalDocument;
import mx.reder.wms.cfdi.RespuestaCancelacionPAC;
import mx.reder.wms.cfdi.RespuestaComprobante;
import mx.reder.wms.cfdi.TimbradoExceptionSAT;
import mx.reder.wms.cfdi.TimbradoSWCFDImp;
import mx.reder.wms.cfdi.entity.ComprobanteCFD;
import mx.reder.wms.cfdi.entity.InformacionAduaneraCFD;
import mx.reder.wms.cfdi.entity.TimbradoCFD;
import mx.reder.wms.cfdi.imp.ComprobanteCFDImp;
import mx.reder.wms.cfdi.imp.DocumentoImp;
import mx.reder.wms.cfdi.imp.ExtraccionImp;
import mx.reder.wms.cfdi.imp.ReceptorImp;
import mx.reder.wms.dao.entity.ASPELBitacoraDAO;
import mx.reder.wms.dao.entity.ASPELCFDIDAO;
import mx.reder.wms.dao.entity.ASPELClienteDAO;
import mx.reder.wms.dao.entity.ASPELControlDAO;
import mx.reder.wms.dao.entity.ASPELCuentaPorCobrarDAO;
import mx.reder.wms.dao.entity.ASPELDocumentoSiguienteDAO;
import mx.reder.wms.dao.entity.ASPELEnlaceLotePedimentoDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDetalleDAO;
import mx.reder.wms.dao.entity.ASPELFoliosDAO;
import mx.reder.wms.dao.entity.ASPELInformacionEnvioDAO;
import mx.reder.wms.dao.entity.ASPELLotePedimentoDAO;
import mx.reder.wms.dao.entity.ASPELMovimientoInventarioDAO;
import mx.reder.wms.dao.entity.ASPELObservacionesDocfDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDetalleDAO;
import mx.reder.wms.dao.entity.ASPELProductoAlmacenDAO;
import mx.reder.wms.dao.entity.ASPELProductoDAO;
import mx.reder.wms.dao.entity.CertificadoSelloDigitalDAO;
import mx.reder.wms.dao.entity.CompaniaDAO;
import mx.reder.wms.dao.entity.DireccionDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoCertificaDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.PedidoBitacoraDAO;
import mx.reder.wms.dao.entity.RutaCfdiDAO;
import mx.reder.wms.dao.entity.RutaDAO;
import mx.reder.wms.dao.entity.RutaFacturaDAO;
import mx.reder.wms.to.ASPELFacturaDetalleTO;
import mx.reder.wms.to.ASPELFacturaDetallePedimentoTO;
import mx.reder.wms.to.FacturaRutaResponse;
import mx.reder.wms.to.LotePedimentoTO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class FacturaRutaBusiness {
    static Logger log = Logger.getLogger(FacturaRutaBusiness.class);

    private DatabaseServices ds;
    private DatabaseServices dsA;

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }

    public void setDatabaseAspelServices(DatabaseServices dsA) {
        this.dsA = dsA;
    }

    public FacturaRutaResponse facturaRuta(String compania, String usuario, String ruta, String flsurtido) throws Exception {
        //
        // Busca la Ruta abierta, si no existe o esta cerrada, crea una nueva ruta
        //
        RutaDAO rutaDAO = (RutaDAO)ds.first(new RutaDAO(), "compania = '"+compania+"' AND ruta = '"+ruta+"' AND fechacierre IS NULL", "id DESC");
        if (rutaDAO==null) {
            rutaDAO = new RutaDAO();
            rutaDAO.id = null;
            rutaDAO.compania = compania;
            rutaDAO.ruta = ruta;
            rutaDAO.status = Constantes.ESTADO_PENDIENTE;
            rutaDAO.fechastatus = new Date();
            rutaDAO.usuario = usuario;
            rutaDAO.fechacreacion = new Date();
            rutaDAO.fechafacturada = null;
            rutaDAO.fechapaquetedocumental = null;
            rutaDAO.fechacierre = null;

            ds.insert(rutaDAO);

            rutaDAO.id = (Integer)ds.aggregate(rutaDAO, "MAX", "id");
        }

        FacturaRutaResponse response = new FacturaRutaResponse();
        response.ruta = rutaDAO;
        response.facturas = new ArrayList<>();
        response.errores = new ArrayList<>();

        log.debug(Reflector.toStringAllFields(rutaDAO));

        // Busca las Ordens de Surtido de esta Ruta.
        StringBuilder where = new StringBuilder();
        where.append("compania = '").append(compania)
                .append("' AND status = '").append(Constantes.ESTADO_CONFIRMADO)
                .append("' AND ruta = '").append(ruta).append("'");
        if (flsurtido!=null&&!flsurtido.isEmpty())
            where.append(" AND flsurtido = ").append(flsurtido);

        ArrayList<OrdenSurtidoPedidoDAO> array = ds.select(new OrdenSurtidoPedidoDAO(), where.toString());
        for (OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO : array) {
            //
            // Busca si ya esta la RutaFactura
            //
            RutaFacturaDAO rutaFacturaDAO = new RutaFacturaDAO(ordenSurtidoPedidoDAO.compania, ordenSurtidoPedidoDAO.flsurtido);
            if (!ds.exists(rutaFacturaDAO)) {
                rutaFacturaDAO.idruta = rutaDAO.id;
                rutaFacturaDAO.parada = 0;
                rutaFacturaDAO.status = Constantes.ESTADO_PENDIENTE;
                rutaFacturaDAO.fechastatus = new Date();
                rutaFacturaDAO.usuario = usuario;
                rutaFacturaDAO.factura = null;
                rutaFacturaDAO.fechafacturacion = null;
                rutaFacturaDAO.mensaje = null;

                ds.insert(rutaFacturaDAO);
            }

            log.debug(Reflector.toStringAllFields(rutaFacturaDAO));

            response.facturas.add(rutaFacturaDAO);
        }

        //
        //
        // Solo pendientes, para no reintentar E0 o E1,
        // por el momento en las pruebas no se necesita reintentar estas
        ///response.facturas = ds.select(new RutaFacturaDAO(), "idruta = "+rutaDAO.id+" AND status = 'PE'");
        //
        //

        //
        // Procedo a facturar las FacturaRuta con estado PE, o con error E0 o E1
        //
        for (RutaFacturaDAO rutaFacturaDAO : response.facturas) {
            try  {
                if (rutaFacturaDAO.status.compareTo("PE")==0
                        ||rutaFacturaDAO.status.compareTo("E0")==0
                        ||rutaFacturaDAO.status.compareTo("E1")==0
                        ) {
                    ASPELFacturaDAO aspelFacturaDAO = facturaOrdenSurtido(rutaFacturaDAO, usuario);

                    // Estado de ruta Factura
                    rutaFacturaDAO.factura = aspelFacturaDAO.CVE_DOC;
                    rutaFacturaDAO.fechafacturacion = new Date();
                    rutaFacturaDAO.status = Constantes.ESTADO_FACTURADO;
                    rutaFacturaDAO.fechastatus = new Date();
                    rutaFacturaDAO.usuario = usuario;
                    rutaFacturaDAO.mensaje = "";

                    ds.update(rutaFacturaDAO, new String[] {"factura", "fechafacturacion", "status", "fechastatus", "usuario", "mensaje"});
                }
            } catch(Exception e) {
                log.error(e.getMessage(), e);

                ErrorTO errorTO = new ErrorTO();
                errorTO.fromException(e);

                response.errores.add(errorTO);

                rutaFacturaDAO.status = "E0";
                rutaFacturaDAO.mensaje = e.getMessage();

                if (e instanceof TimbradoExceptionSAT) {
                    TimbradoExceptionSAT tes = (TimbradoExceptionSAT)e;
                    rutaFacturaDAO.status = "E1";
                    rutaFacturaDAO.mensaje = tes.getMessage();
                }

                // Estado de ruta Factura
                rutaFacturaDAO.fechastatus = new Date();
                rutaFacturaDAO.usuario = usuario;

                ds.update(rutaFacturaDAO, new String[] {"status", "fechastatus", "usuario", "mensaje"});
            }
        }

        // Verifico si todas se facturaron
        int count = ds.count(new RutaFacturaDAO(), "compania = '"+rutaDAO.compania+"' AND idruta = "+rutaDAO.id+" AND status != 'CA'");
        int countFA = ds.count(new RutaFacturaDAO(), "compania = '"+rutaDAO.compania+"' AND idruta = "+rutaDAO.id+" AND status = 'FA'");
        if (count==countFA) {
            rutaDAO.fechafacturada = new Date();
            ds.update(rutaDAO, new String[] {"fechafacturada"});

            rutaDAO.status = Constantes.ESTADO_FACTURADO;
        } else {
            ds.updateNULL(rutaDAO, "fechafacturada");

            rutaDAO.status = Constantes.ESTADO_PENDIENTE;
        }
        rutaDAO.fechastatus = new Date();
        rutaDAO.usuario = usuario;

        ds.update(rutaDAO, new String[] {"status", "fechastatus", "usaurio"});

        return response;
    }

    private ASPELFacturaDAO facturaOrdenSurtido(RutaFacturaDAO rutaFacturaDAO, String usuario) throws Exception {
        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO  = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = rutaFacturaDAO.compania;
        ordenSurtidoPedidoDAO.flsurtido = rutaFacturaDAO.flsurtido;
        if (!ds.exists(ordenSurtidoPedidoDAO))
            throw new WebException("No existe esta Orden de Surtido de Pedido ["+ordenSurtidoPedidoDAO+"]");

        if (ordenSurtidoPedidoDAO.status.compareTo(Constantes.ESTADO_CONFIRMADO)!=0)
            throw new WebException("El estado de la Orden de Surtido del Pedido ["+ordenSurtidoPedidoDAO.status+"] no es CO.");

        ASPELPedidoDAO aspelPedidoDAO = new ASPELPedidoDAO();
        aspelPedidoDAO.setEmpresa(rutaFacturaDAO.compania);
        aspelPedidoDAO.CVE_DOC = ordenSurtidoPedidoDAO.pedido;
        if (!dsA.exists(aspelPedidoDAO))
            throw new WebException("No existe este Pedido ["+aspelPedidoDAO+"]");

        dsA.beginTransaction();

        ASPELFacturaDAO aspelFacturaDAO = null;

        try {
            //
            // Almacen
            //
            int almacen = 1;
            rutaFacturaDAO.serie = getSeriePedido(aspelPedidoDAO.CVE_DOC);
            log.debug("Almacen: "+almacen+" SerieFactura: "+rutaFacturaDAO.serie);

            ds.update(rutaFacturaDAO, new String[] {"serie"});

            // Checa disponibilidad de inventario de todos los detalles, en el almacen
            checaDisponibilidadInventario(rutaFacturaDAO.compania, ordenSurtidoPedidoDAO, almacen);

            aspelFacturaDAO = creaFacturaASPEL(rutaFacturaDAO.compania, aspelPedidoDAO, ordenSurtidoPedidoDAO,
                    usuario, rutaFacturaDAO.serie, almacen);
            afectaInventario(rutaFacturaDAO.compania, aspelFacturaDAO);
            generaCuentaPorCobrar(rutaFacturaDAO.compania, aspelFacturaDAO);
            generaCFDI(rutaFacturaDAO, aspelFacturaDAO);

            estadoOrdenSurtidoPedido(ordenSurtidoPedidoDAO, aspelPedidoDAO, usuario);

            //
            dsA.commit();
        } catch(Exception e) {
            dsA.rollback();
            throw e;
        }

        return aspelFacturaDAO;
    }

    private void estadoOrdenSurtidoPedido(OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO, ASPELPedidoDAO aspelPedidoDAO, String usuario) throws Exception {
        //
        // Estatus de Orden de Surtido Pedido
        //
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

        //
        // Estado del Pedido en ASPEL
        //
        String wherePedido = "compania = '"+ordenSurtidoPedidoDAO.compania+"' AND pedido = '"+ordenSurtidoPedidoDAO.pedido+"'";
        int count = ds.count(ordenSurtidoPedidoDAO, wherePedido);
        int countFA = ds.count(ordenSurtidoPedidoDAO, wherePedido+" AND status IN ('CA','FA')");

        if (count==countFA) {
            //
            aspelPedidoDAO.STATUS = Constantes.ESTADO_ASPEL_EMITIDO;

            dsA.update(aspelPedidoDAO, new String[] {"STATUS"});

            PedidoBitacoraDAO pedidoBitacoraDAO = new PedidoBitacoraDAO();
            pedidoBitacoraDAO.id = null;
            pedidoBitacoraDAO.compania = ordenSurtidoPedidoDAO.compania;
            pedidoBitacoraDAO.pedido = aspelPedidoDAO.CVE_DOC;
            pedidoBitacoraDAO.status = Constantes.ESTADO_FACTURADO;
            pedidoBitacoraDAO.fechabitacora = new Date();
            pedidoBitacoraDAO.usuario = usuario;

            ds.insert(pedidoBitacoraDAO);
        }
    }

    private void generaCuentaPorCobrar(String empresa, ASPELFacturaDAO aspelFacturaDAO) throws Exception {
        ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
        aspelClienteDAO.setEmpresa(empresa);
        aspelClienteDAO.CLAVE = aspelFacturaDAO.CVE_CLPV;
        if (!dsA.exists(aspelClienteDAO))
            throw new WebException("No existe este Cliente ["+aspelClienteDAO+"]");

        aspelClienteDAO.LIMCRED = Numero.redondea(aspelClienteDAO.LIMCRED);
        aspelClienteDAO.SALDO = Numero.redondea(aspelClienteDAO.SALDO);

        if (aspelClienteDAO.LIMCRED > 0.0) {
            double saldoDisponible = Numero.redondea(aspelClienteDAO.LIMCRED - aspelClienteDAO.SALDO);
            if (aspelFacturaDAO.IMPORTE > saldoDisponible)
                throw new WebException("Este Cliente ["+aspelClienteDAO+"] no tiene Saldo Disponible para esta Factura. "
                        +"Limite de Credito ["+Numero.getMoneda(aspelClienteDAO.LIMCRED)+"] Saldo ["+Numero.getMoneda(aspelClienteDAO.SALDO)
                        +"] Saldo Disponible ["+Numero.getMoneda(saldoDisponible)+"] Importe Factura ["+Numero.getMoneda(aspelFacturaDAO.IMPORTE)+"]");
        }

        // Saldo del Cliente
        aspelClienteDAO.SALDO += aspelFacturaDAO.IMPORTE;
        dsA.update(aspelClienteDAO, new String[] {"SALDO"});

        //
        // Cuenta por Cobrar
        //
        ASPELCuentaPorCobrarDAO aspelCuentaPorCobrarDAO = new ASPELCuentaPorCobrarDAO();
        aspelCuentaPorCobrarDAO.setEmpresa(empresa);

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
        dsA.insert(aspelCuentaPorCobrarDAO);

        //
        // Bitacora
        //
        ASPELControlDAO aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(empresa);

        // Folio de Bitacora
        aspelControlDAO.ID_TABLA = 62;
        if (!dsA.exists(aspelControlDAO))
            dsA.insert(aspelControlDAO);

        aspelControlDAO.ULT_CVE ++;
        dsA.update(aspelControlDAO);

        ASPELBitacoraDAO aspelBitacoraDAO = new ASPELBitacoraDAO();
        aspelBitacoraDAO.setEmpresa(empresa);

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
        dsA.insert(aspelBitacoraDAO);

        aspelCuentaPorCobrarDAO.CVE_BITA = aspelBitacoraDAO.CVE_BITA;

        dsA.update(aspelCuentaPorCobrarDAO, new String[] {"CVE_BITA"});
    }

    private void afectaInventario(String empresa, ASPELFacturaDAO aspelFacturaDAO) throws Exception {
        ASPELControlDAO aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(empresa);

        ASPELFacturaDetalleDAO aspelFacturaDetalleDAO = new ASPELFacturaDetalleDAO();
        aspelFacturaDetalleDAO.setEmpresa(empresa);

        ArrayList<ASPELFacturaDetalleDAO> detalles = dsA.select(aspelFacturaDetalleDAO, aspelFacturaDAO.getWhere());
        for(ASPELFacturaDetalleDAO adfDAO : detalles) {
            aspelFacturaDetalleDAO = adfDAO;

            ASPELProductoDAO aspelProductoDAO = new ASPELProductoDAO();
            aspelProductoDAO.setEmpresa(empresa);
            aspelProductoDAO.CVE_ART = aspelFacturaDetalleDAO.CVE_ART;

            if (!dsA.exists(aspelProductoDAO))
                throw new Exception("No existe este Producto ["+aspelProductoDAO+"]");

            log.info(Reflector.toStringAllFields(aspelProductoDAO));

            //
            // Existencia de Producto
            //
            aspelProductoDAO.EXIST -= aspelFacturaDetalleDAO.CANT;
            dsA.update(aspelProductoDAO, new String[] {"EXIST"});

            log.debug("Nueva Existencia: "+aspelProductoDAO.EXIST);

            // Existencia MultiAlmacen
            ASPELProductoAlmacenDAO aspelProductoAlmacenDAO = new ASPELProductoAlmacenDAO();
            aspelProductoAlmacenDAO.setEmpresa(empresa);

            aspelProductoAlmacenDAO.CVE_ART = aspelFacturaDetalleDAO.CVE_ART;
            aspelProductoAlmacenDAO.CVE_ALM = aspelFacturaDetalleDAO.NUM_ALM;
            if (!dsA.exists(aspelProductoAlmacenDAO))
                throw new Exception("No existe este Producto ["+aspelProductoDAO+"] en el MultiAlmacen ["+aspelProductoAlmacenDAO+"]");

            log.info(Reflector.toStringAllFields(aspelProductoAlmacenDAO));

            aspelProductoAlmacenDAO.EXIST -= aspelFacturaDetalleDAO.CANT;
            dsA.update(aspelProductoAlmacenDAO, new String[] {"EXIST"});

            log.debug("Nueva Existencia Multialmacen: "+aspelProductoAlmacenDAO.EXIST);

            // Folio de Movimiento
            aspelControlDAO.ID_TABLA = 32;
            if (!dsA.exists(aspelControlDAO))
                dsA.insert(aspelControlDAO);

            aspelControlDAO.ULT_CVE ++;
            dsA.update(aspelControlDAO);

            //
            // Movimiento de Inventario
            //
            ASPELMovimientoInventarioDAO aspelMovimientoInventarioDAO = new ASPELMovimientoInventarioDAO();
            aspelMovimientoInventarioDAO.setEmpresa(empresa);

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
            aspelMovimientoInventarioDAO.EXISTENCIA = aspelProductoAlmacenDAO.EXIST;
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
            dsA.insert(aspelMovimientoInventarioDAO);
        }
    }

    private void generaCFDI(RutaFacturaDAO rutaFacturaDAO, ASPELFacturaDAO aspelFacturaDAO) throws Exception {
        ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
        aspelClienteDAO.setEmpresa(rutaFacturaDAO.compania);
        aspelClienteDAO.CLAVE = aspelFacturaDAO.CVE_CLPV;
        if (!dsA.exists(aspelClienteDAO))
            throw new WebException("No existe este Cliente ["+aspelClienteDAO+"]");

        log.debug(Reflector.toStringAllFields(aspelClienteDAO));
        log.debug("Uso CFDI ["+aspelClienteDAO.USO_CFDI+"]");
        log.debug("Forma de Pago ["+aspelClienteDAO.FORMADEPAGOSAT+"]");
        log.debug("Metodo de Pago ["+aspelClienteDAO.METODODEPAGO+"]");

        // Arreglo el Metodo de Pago
        if (aspelClienteDAO.METODODEPAGO==null||aspelClienteDAO.METODODEPAGO.matches("\\d+"))
            aspelClienteDAO.METODODEPAGO = "PPD";

        ASPELInformacionEnvioDAO aspelInformacionEnvioDAO = new ASPELInformacionEnvioDAO();
        aspelInformacionEnvioDAO.setEmpresa(rutaFacturaDAO.compania);
        aspelInformacionEnvioDAO.CVE_INFO = aspelFacturaDAO.DAT_ENVIO;
        if (!dsA.exists(aspelInformacionEnvioDAO))
            throw new WebException("No existe la Direccion de Envio ["+aspelInformacionEnvioDAO+"]");

        //
        //
        //
        ArrayList<ASPELFacturaDetalleTO> detalles = dsA.collection(new ASPELFacturaDetalleTO(),
                "SELECT pf.CVE_DOC, pf.NUM_PAR, pf.CVE_ART, pf.CANT, pf.PXS, pf.PREC, pf.COST, pf.IMPU1, pf.IMPU2, pf.IMPU3, pf.IMPU4,"
                +"pf.IMP1APLA, pf.IMP2APLA, pf.IMP3APLA, pf.IMP4APLA, pf.TOTIMP1, pf.TOTIMP2, pf.TOTIMP3, pf.TOTIMP4, pf.DESC1, pf.DESC2, pf.DESC3,"
                +"pf.COMI, pf.APAR, pf.ACT_INV, pf.NUM_ALM, pf.POLIT_APLI, pf.TIP_CAM, pf.UNI_VENTA, pf.TIPO_PROD, pf.CVE_OBS, pf.REG_SERIE, pf.E_LTPD,"
                +"pf.TIPO_ELEM, pf.NUM_MOV, pf.TOT_PARTIDA, pf.IMPRIMIR, pf.MAN_IEPS, pf.APL_MAN_IMP, pf.CUOTA_IEPS, pf.APL_MAN_IEPS, pf.MTO_PORC,"
                +"pf.MTO_CUOTA, pf.CVE_ESQ, pf.DESCR_ART, pf.UUID, pf.VERSION_SINC, p.CVE_PRODSERV, p.CVE_UNIDAD, p.DESCR, p.UNI_MED, "
                +"COALESCE(pl.CAMPLIB5, 0) AS PREPUB "
                +"FROM PAR_FACTF"+rutaFacturaDAO.compania+" pf LEFT JOIN INVE"+rutaFacturaDAO.compania+" p ON pf.CVE_ART = p.CVE_ART "
                +"LEFT JOIN INVE_CLIB"+rutaFacturaDAO.compania+" pl ON pf.CVE_ART = pl.CVE_PROD "
                +"WHERE pf.CVE_DOC = '"+aspelFacturaDAO.CVE_DOC+"'");

        for (int indx=0; indx<detalles.size(); indx++) {
            ASPELFacturaDetalleTO aspelFacturaDetalleTO = detalles.get(indx);

            ArrayList<InformacionAduaneraCFD> pedimentos = dsA.collection(new ASPELFacturaDetallePedimentoTO(),
                    "SELECT pf.CVE_DOC, pf.NUM_PAR, pf.CVE_ART, pf.CANT, elp.E_LTPD, elp.REG_LTPD, elp.CANTIDAD, elp.PXRS, "
                    +"lp.LOTE, lp.PEDIMENTO, lp.CVE_ALM, lp.FCHCADUC, lp.FCHADUANA "
                    +"FROM PAR_FACTF"+rutaFacturaDAO.compania+" pf "
                    +"LEFT JOIN ENLACE_LTPD"+rutaFacturaDAO.compania+" elp ON pf.E_LTPD = elp.E_LTPD "
                    +"LEFT JOIN LTPD"+rutaFacturaDAO.compania+" lp ON elp.REG_LTPD = lp.REG_LTPD "
                    +"WHERE pf.CVE_DOC = '"+aspelFacturaDetalleTO.CVE_DOC+"' AND pf.NUM_PAR = "+aspelFacturaDetalleTO.NUM_PAR);
            if (!pedimentos.isEmpty()) {
                aspelFacturaDetalleTO.setInformacionAduanera(pedimentos.get(0));

                detalles.set(indx, aspelFacturaDetalleTO);
            }
        }
        log.debug("detalles ["+detalles.size()+"]");

        CompaniaDAO companiaDAO = (CompaniaDAO)ds.first(new CompaniaDAO(), "compania = '"+rutaFacturaDAO.compania+"'");

        DireccionDAO direccionDAO = new DireccionDAO();
        direccionDAO.direccion = companiaDAO.direccion;
        if (!ds.exists(direccionDAO))
            throw new WebException("No existe esta Direccion ["+direccionDAO+"]");

        // La clave en el RFC del receptor del CFDI debe ser la misma que la registrada para el emisor.
        ReceptorImp receptorCFD = new ReceptorImp();
        receptorCFD.nombre = companiaDAO.razonsocial;
        receptorCFD.rfc = companiaDAO.rfc;
        receptorCFD.regimenFiscal = companiaDAO.regimenfiscal;
        receptorCFD.usoCFDI = aspelClienteDAO.USO_CFDI;
        receptorCFD.formaDePago = aspelClienteDAO.FORMADEPAGOSAT;
        receptorCFD.metodoDePago = aspelClienteDAO.METODODEPAGO;
        receptorCFD.numCtaPago = null;
        receptorCFD.calle = direccionDAO.calle;
        receptorCFD.noExterior = direccionDAO.noexterior;
        receptorCFD.noInterior = direccionDAO.nointerior;
        receptorCFD.colonia = direccionDAO.colonia;
        receptorCFD.municipio = direccionDAO.poblacion;
        receptorCFD.estado = direccionDAO.entidadfederativa;
        receptorCFD.pais = direccionDAO.pais;
        receptorCFD.codigoPostal = direccionDAO.codigopostal;

        DocumentoImp documentoCFD = new DocumentoImp();
        documentoCFD.serie = aspelFacturaDAO.TIP_DOC;
        documentoCFD.folio = aspelFacturaDAO.CVE_DOC;
        documentoCFD.fecha = aspelFacturaDAO.FECHAELAB;
        documentoCFD.tipoComprobante = "I";
        documentoCFD.moneda = "MXN";
        documentoCFD.exportacion = "01";
        documentoCFD.cfdiRelacionados = null;
        documentoCFD.informacionGlobal = null;

        //if (companiaDAO.diferenciahoraria!=0)
        //    documentoCFD.fecha = Fecha.addDate(documentoCFD.fecha, Calendar.HOUR, companiaDAO.diferenciahoraria);
        //
        //
        //
        receptorCFD.usoCFDI = "G01";
        receptorCFD.metodoDePago = "PPD";
        receptorCFD.formaDePago = "99";
        receptorCFD.rfc = "GNE130422S71";
        receptorCFD.nombre = "GNEW";
        receptorCFD.regimenFiscal = "601";
        receptorCFD.codigoPostal = "06700";
        //
        //
        //

        ComprobanteCFD comprobanteCFDImp = new ComprobanteCFDImp(companiaDAO, direccionDAO, documentoCFD,
                receptorCFD, receptorCFD, receptorCFD, detalles, null, null);

        //TimbradoCFD timbradoCFDImp = TimbradoFactory.getInstance().makeTimbradoCFD(ds, companiaDAO.compania);
        HashMap<String, String> propiedades = new HashMap<>();
        propiedades.put("url", "http://services.test.sw.com.mx");
        propiedades.put("usuario", "joelbecerram@gmail.com");
        propiedades.put("password", "CRC+SW");
        TimbradoCFD timbradoCFDImp = new TimbradoSWCFDImp(propiedades);

        String fecha = Fecha.getFechaHora(documentoCFD.getFecha());
        CertificadoSelloDigitalDAO certificadoSelloDigitalDAO = (CertificadoSelloDigitalDAO)ds.first(new CertificadoSelloDigitalDAO(),
            "compania = '"+companiaDAO.compania+"' AND fechainicial <= '"+fecha+"' AND fechafinal >= '"+fecha+"'");
        if(certificadoSelloDigitalDAO==null)
            throw new Exception("No existe CSD para la compania = '"+companiaDAO.compania+"' fecha = '"+fecha+"'");

        //
        // Cancela el anterior que esta Activo
        //
        RutaCfdiDAO rutaCfdiDAOII = (RutaCfdiDAO)ds.first(new RutaCfdiDAO(), "compania = '"+rutaFacturaDAO.compania+"' AND flsurtido = "+rutaFacturaDAO.flsurtido+" AND status = 'A'");
        if (rutaCfdiDAOII!=null) {
            log.debug("Cancelando CFDI anterior ["+rutaCfdiDAOII+";"+rutaCfdiDAOII.uuid+"] ...");

            CertificadoSelloDigitalDAO certificadoSelloDigitalDAOII = new CertificadoSelloDigitalDAO(rutaCfdiDAOII.compania, rutaCfdiDAOII.nocertificado);
            if (!ds.exists(certificadoSelloDigitalDAOII))
                throw new WebException("No existe el Certificado ["+certificadoSelloDigitalDAOII
                        +"] necesario para cancelar el CFDI anterior ["+rutaCfdiDAOII+";"+rutaCfdiDAOII.uuid+"]");

            RespuestaCancelacionPAC respuestaCancelacionPAC =
                    timbradoCFDImp.cancelaCFDI(rutaCfdiDAOII.rfcemisor, rutaCfdiDAOII.rfcreceptor, rutaCfdiDAOII.total, rutaCfdiDAOII.uuid, "02", "",
                        certificadoSelloDigitalDAOII.nocertificado, certificadoSelloDigitalDAOII.password,
                        new String(Base64.getEncoder().encode(certificadoSelloDigitalDAOII.getArchivoCer())),
                        new String(Base64.getEncoder().encode(certificadoSelloDigitalDAOII.getArchivoKey())));

            rutaCfdiDAOII.fechacancelacion = new Date();
            rutaCfdiDAOII.acusecancelacion = respuestaCancelacionPAC.getXmlAcuse();

            ds.update(rutaCfdiDAOII, new String[] {"fechacancelacion", "acusecancelacion"});
        }
        //
        //
        //

        ExtraccionImp extraccionImp = new ExtraccionImp();
        extraccionImp.setCertificadoSelloDigital(certificadoSelloDigitalDAO);
        RespuestaComprobante respuesta = extraccionImp.getComprobante(comprobanteCFDImp, timbradoCFDImp);
        ComprobanteDocument.Comprobante comprobante = respuesta.getCd().getComprobante();
        TimbreFiscalDigitalDocument.TimbreFiscalDigital timbreFiscalDigital = respuesta.getTfd().getTimbreFiscalDigital();
        String xml = new String(respuesta.getXml(), "UTF-8");

        log.debug("fechatimbrado: "+timbreFiscalDigital.getFechaTimbrado().getTime());
        //
        //
        //
        ASPELCFDIDAO aspelCFDIDAO = new ASPELCFDIDAO();
        aspelCFDIDAO.setEmpresa(rutaFacturaDAO.compania);

        aspelCFDIDAO.TIPO_DOC = aspelFacturaDAO.TIP_DOC;
        aspelCFDIDAO.CVE_DOC = aspelFacturaDAO.CVE_DOC;
        aspelCFDIDAO.VERSION = "1.1";
        aspelCFDIDAO.UUID = timbreFiscalDigital.getUUID().toUpperCase();
        aspelCFDIDAO.NO_SERIE = certificadoSelloDigitalDAO.nocertificado;
        aspelCFDIDAO.FECHA_CERT = timbreFiscalDigital.getFechaTimbrado().toString();
        aspelCFDIDAO.FECHA_CANCELA = "";
        aspelCFDIDAO.XML_DOC = formatXmlASPEL("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+xml);
        aspelCFDIDAO.XML_DOC_CANCELA = null;
        aspelCFDIDAO.DESGLOCEIMP1 = "S";
        aspelCFDIDAO.DESGLOCEIMP2 = "N";
        aspelCFDIDAO.DESGLOCEIMP3 = "N";
        aspelCFDIDAO.DESGLOCEIMP4 = "S";
        aspelCFDIDAO.MSJ_CANC = null;
        aspelCFDIDAO.PENDIENTE = "N";
        aspelCFDIDAO.CVE_USUARIO = 0;
        aspelCFDIDAO.MOTIVO_CANC = null;
        aspelCFDIDAO.UUID_REL = null;

        log.debug(Reflector.toStringAllFields(aspelCFDIDAO));

        dsA.save(aspelCFDIDAO);

        aspelFacturaDAO.METODODEPAGO = "PPD";
        aspelFacturaDAO.ESCFD = "T";
        aspelFacturaDAO.UUID = aspelCFDIDAO.UUID;

        dsA.update(aspelFacturaDAO, new String[] {"METODODEPAGO", "ESCFD", "UUID"});

        // Inserta el CFDI
        RutaCfdiDAO rutaCfdiDAO = new RutaCfdiDAO();
        rutaCfdiDAO.id = null;
        rutaCfdiDAO.compania = rutaFacturaDAO.compania;
        rutaCfdiDAO.flsurtido = rutaFacturaDAO.flsurtido;
        rutaCfdiDAO.idruta = rutaFacturaDAO.idruta;
        rutaCfdiDAO.status = "A";
        rutaCfdiDAO.fechastatus = new Date();
        rutaCfdiDAO.nocertificado = certificadoSelloDigitalDAO.nocertificado;
        rutaCfdiDAO.uuid = timbreFiscalDigital.getUUID();
        rutaCfdiDAO.fechatimbre = timbreFiscalDigital.getFechaTimbrado().getTime();
        rutaCfdiDAO.rfcemisor = comprobante.getEmisor().getRfc();
        rutaCfdiDAO.rfcreceptor = comprobante.getReceptor().getRfc();
        rutaCfdiDAO.total = comprobante.getTotal().doubleValue();
        rutaCfdiDAO.xml = xml;
        rutaCfdiDAO.cadenaoriginal = respuesta.getCadenaoriginal();
        rutaCfdiDAO.qr = respuesta.getQr();
        rutaCfdiDAO.fechacancelacion = null;
        rutaCfdiDAO.acusecancelacion = null;

        ds.insert(rutaCfdiDAO);
    }

    private String formatXmlASPEL(String xml) {
        xml = xml.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");

        int begin = xml.indexOf("<cfdi:Emisor");
        int end = xml.indexOf("/>", begin+1);
        if (begin!=-1)
            xml = xml.substring(0, end)+"></cfdi:Emisor>"+xml.substring(end+2);

        begin = xml.indexOf("<cfdi:Receptor");
        end = xml.indexOf("/>", begin+1);
        if (begin!=-1)
            xml = xml.substring(0, end)+"></cfdi:Receptor>"+xml.substring(end+2);

        begin = 0;
        while (true) {
            begin = xml.indexOf("<cfdi:Traslado ", begin);
            if (begin==-1)
                break;
            end = xml.indexOf("/>", begin+1);
            xml = xml.substring(0, end)+"></cfdi:Traslado>"+xml.substring(end+2);
            begin = end + 2;
        }

        xml = xml.replace("xsi:schemaLocation=\"http://www.sat.gob.mx/TimbreFiscalDigital http://www.sat.gob.mx/sitio_internet/cfd/TimbreFiscalDigital/TimbreFiscalDigitalv11.xsd\"",
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sat.gob.mx/TimbreFiscalDigital http://www.sat.gob.mx/sitio_internet/cfd/TimbreFiscalDigital/TimbreFiscalDigitalv11.xsd\"");

        return xml;
    }

    private ASPELFacturaDAO creaFacturaASPEL(String empresa, ASPELPedidoDAO aspelPedidoDAO,
            OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO, String usuario, String serieFactura, int almacen) throws Exception {

        ASPELClienteDAO aspelClienteDAO = new ASPELClienteDAO();
        aspelClienteDAO.setEmpresa(empresa);
        aspelClienteDAO.CLAVE = aspelPedidoDAO.CVE_CLPV;
        if (!dsA.exists(aspelClienteDAO))
            throw new WebException("No existe este Cliente Destino ["+aspelClienteDAO+"].");

        if (aspelClienteDAO.STATUS.compareTo("A")!=0)
            throw new WebException("Este Cliente ["+aspelClienteDAO+"] no tiene estado 'A' (Activo)");

        ASPELInformacionEnvioDAO aspelInformacionEnvioPDAO = new ASPELInformacionEnvioDAO();
        aspelInformacionEnvioPDAO.setEmpresa(empresa);
        aspelInformacionEnvioPDAO.CVE_INFO = aspelPedidoDAO.DAT_ENVIO;

        //
        // Si no hay informacion destino, los datos de destino son del Cliente
        //
        if (!dsA.exists(aspelInformacionEnvioPDAO)) {
            aspelInformacionEnvioPDAO.CVE_CONS = aspelClienteDAO.CLAVE;
            aspelInformacionEnvioPDAO.NOMBRE = aspelClienteDAO.NOMBRE;
            aspelInformacionEnvioPDAO.CALLE = aspelClienteDAO.CALLE;
            aspelInformacionEnvioPDAO.NUMINT = aspelClienteDAO.NUMINT;
            aspelInformacionEnvioPDAO.NUMEXT = aspelClienteDAO.NUMEXT;
            aspelInformacionEnvioPDAO.CRUZAMIENTOS = aspelClienteDAO.CRUZAMIENTOS;
            aspelInformacionEnvioPDAO.CRUZAMIENTOS2 = aspelClienteDAO.CRUZAMIENTOS2;
            aspelInformacionEnvioPDAO.COLONIA = aspelClienteDAO.COLONIA;
            aspelInformacionEnvioPDAO.CODIGO = aspelClienteDAO.CODIGO;
            aspelInformacionEnvioPDAO.POB = aspelClienteDAO.LOCALIDAD;
            aspelInformacionEnvioPDAO.ESTADO = aspelClienteDAO.ESTADO;
            aspelInformacionEnvioPDAO.PAIS = aspelClienteDAO.PAIS;
            aspelInformacionEnvioPDAO.MUNICIPIO = aspelClienteDAO.MUNICIPIO;
            aspelInformacionEnvioPDAO.REFERDIR = aspelClienteDAO.REFERDIR;
            aspelInformacionEnvioPDAO.CURP = aspelClienteDAO.CURP;
            aspelInformacionEnvioPDAO.CVE_ZONA = aspelClienteDAO.CVE_ZONA;
            aspelInformacionEnvioPDAO.CVE_OBS = null;
            aspelInformacionEnvioPDAO.STRNOGUIA = "";
            aspelInformacionEnvioPDAO.STRMODOENV = "";
            aspelInformacionEnvioPDAO.FECHA_ENV =  aspelPedidoDAO.FECHA_DOC;
            aspelInformacionEnvioPDAO.NOMBRE_RECEP = "";
            aspelInformacionEnvioPDAO.NO_RECEP = "";
            aspelInformacionEnvioPDAO.FECHA_RECEP = null;

            log.info(Reflector.toStringAllFields(aspelInformacionEnvioPDAO));
        }

        ASPELFoliosDAO aspelFoliosDAO = new ASPELFoliosDAO();
        aspelFoliosDAO.setEmpresa(empresa);

        aspelFoliosDAO = (ASPELFoliosDAO)dsA.first(aspelFoliosDAO, "TIP_DOC = 'F' AND SERIE = '"+serieFactura+"'");
        if (aspelFoliosDAO==null)
            throw new WebException("No existe Foleador para TIP_DOC = 'F' y SERIE = '"+serieFactura+"'");

        aspelFoliosDAO.setEmpresa(empresa);
        aspelFoliosDAO.ULT_DOC ++;
        aspelFoliosDAO.FECH_ULT_DOC = new Date();

        log.info(Reflector.toStringAllFields(aspelFoliosDAO));
        dsA.update(aspelFoliosDAO, new String[] {"ULT_DOC", "FECH_ULT_DOC"});

        //
        // Factura
        //
        ASPELFacturaDAO aspelFacturaDAO = new ASPELFacturaDAO();
        aspelFacturaDAO.setEmpresa(empresa);

        aspelFacturaDAO.TIP_DOC = "F";
        aspelFacturaDAO.CVE_DOC = aspelFoliosDAO.SERIE+aspelFoliosDAO.ULT_DOC;
        aspelFacturaDAO.CVE_CLPV = aspelClienteDAO.CLAVE;
        aspelFacturaDAO.STATUS = "E";
        aspelFacturaDAO.DAT_MOSTR = 0;
        aspelFacturaDAO.CVE_VEND = aspelPedidoDAO.CVE_VEND;
        aspelFacturaDAO.CVE_PEDI = aspelPedidoDAO.CVE_PEDI;
        aspelFacturaDAO.FECHA_DOC = Fecha.getDate(Fecha.getFecha()+" 00:00:00");
        aspelFacturaDAO.FECHA_ENT = aspelFacturaDAO.FECHA_DOC;
        aspelFacturaDAO.FECHA_VEN = Fecha.addDate(aspelFacturaDAO.FECHA_DOC, Calendar.DATE, aspelClienteDAO.DIASCRED);
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
        dsA.insert(aspelFacturaDAO);

        // Enlazo el Pedido
        aspelPedidoDAO.STATUS = "E";
        aspelPedidoDAO.ENLAZADO = "P";
        aspelPedidoDAO.TIP_DOC_E = aspelFacturaDAO.TIP_DOC;
        aspelPedidoDAO.TIP_DOC_SIG = aspelFacturaDAO.TIP_DOC;
        aspelPedidoDAO.DOC_SIG = aspelFacturaDAO.CVE_DOC;
        dsA.update(aspelPedidoDAO, new String[] {"STATUS", "ENLAZADO", "TIP_DOC_E", "TIP_DOC_SIG", "DOC_SIG"});

        //
        // Observaciones
        //
        ASPELControlDAO aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(empresa);

        // Folio de Observaciones
        aspelControlDAO.ID_TABLA = 56;
        if (!dsA.exists(aspelControlDAO))
            dsA.insert(aspelControlDAO);

        aspelControlDAO.ULT_CVE ++;
        dsA.update(aspelControlDAO);

        ASPELObservacionesDocfDAO aspelObservacionesDocfDAO = new ASPELObservacionesDocfDAO();
        aspelObservacionesDocfDAO.setEmpresa(empresa);

        aspelObservacionesDocfDAO.CVE_OBS = aspelControlDAO.ULT_CVE;
        aspelObservacionesDocfDAO.STR_OBS = "";

        log.info(Reflector.toStringAllFields(aspelObservacionesDocfDAO));
        dsA.insert(aspelObservacionesDocfDAO);

        aspelFacturaDAO.CVE_OBS = aspelObservacionesDocfDAO.CVE_OBS;
        dsA.update(aspelFacturaDAO, new String[] {"CVE_OBS"});

        //
        // Bitacora
        //
        aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(empresa);

        // Folio de Bitacora
        aspelControlDAO.ID_TABLA = 62;
        if (!dsA.exists(aspelControlDAO))
            dsA.insert(aspelControlDAO);

        aspelControlDAO.ULT_CVE ++;
        dsA.update(aspelControlDAO);

        ASPELBitacoraDAO aspelBitacoraDAO = new ASPELBitacoraDAO();
        aspelBitacoraDAO.setEmpresa(empresa);

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
        dsA.insert(aspelBitacoraDAO);

        aspelFacturaDAO.CVE_BITA = aspelBitacoraDAO.CVE_BITA;
        dsA.update(aspelFacturaDAO, new String[] {"CVE_BITA"});

        //
        // Info de Envio de Factura
        //
        aspelControlDAO = new ASPELControlDAO();
        aspelControlDAO.setEmpresa(empresa);

        // Folio de Informacion de Envio
        aspelControlDAO.ID_TABLA = 70;
        if (!dsA.exists(aspelControlDAO))
            dsA.insert(aspelControlDAO);

        aspelControlDAO.ULT_CVE ++;
        dsA.update(aspelControlDAO);

        ASPELInformacionEnvioDAO aspelInformacionEnvioDAO = new ASPELInformacionEnvioDAO();
        aspelInformacionEnvioDAO.setEmpresa(empresa);

        Reflector.copyAllFields(aspelInformacionEnvioPDAO, aspelInformacionEnvioDAO);
        aspelInformacionEnvioDAO.CVE_INFO = aspelControlDAO.ULT_CVE;

        log.info(Reflector.toStringAllFields(aspelInformacionEnvioDAO));
        dsA.insert(aspelInformacionEnvioDAO);

        aspelFacturaDAO.DAT_ENVIO = aspelInformacionEnvioDAO.CVE_INFO;
        dsA.update(aspelFacturaDAO, new String[] {"DAT_ENVIO"});

        //
        // Detalles de Factura
        //

        int iPartida = 0;
        double totFacturadas = 0.0d;
        double totImporte = 0.0;
        double totIeps = 0.0;
        double totIva = 0.0;
        double totDescuento = 0.0;

        ArrayList<OrdenSurtidoPedidoCertificaDAO> detallesOS = ds.select(new OrdenSurtidoPedidoCertificaDAO(), ordenSurtidoPedidoDAO.getWhere());
        for(OrdenSurtidoPedidoCertificaDAO ordenSurtidoPedidoCertificaDAO : detallesOS) {
            // Solo con piezas certificadas
            if (ordenSurtidoPedidoCertificaDAO.certificadas==0.0)
                continue;

            ASPELProductoDAO aspelProductoDAO = new ASPELProductoDAO();
            aspelProductoDAO.setEmpresa(empresa);
            aspelProductoDAO.CVE_ART = ordenSurtidoPedidoCertificaDAO.codigo;

            if (!dsA.exists(aspelProductoDAO))
                throw new Exception("No existe este Producto ["+aspelProductoDAO+"]");

            log.info(Reflector.toStringAllFields(aspelProductoDAO));

            ArrayList<LotePedimentoTO> lotesPedimentos = getLotesPedimentosParaDescontar(empresa, aspelProductoDAO, almacen, ordenSurtidoPedidoCertificaDAO);
            if (lotesPedimentos==null||lotesPedimentos.isEmpty())
                throw new Exception("No encontre Lotes Pedimentos para descontar de este Producto ["+aspelProductoDAO+"] y almacen ["+almacen+"]");

            // Folio de Movimiento
            aspelControlDAO.ID_TABLA = 44;
            if (!dsA.exists(aspelControlDAO))
                dsA.insert(aspelControlDAO);

            aspelControlDAO.ULT_CVE ++;
            dsA.update(aspelControlDAO);

            iPartida ++;
            totFacturadas += Numero.redondea(ordenSurtidoPedidoCertificaDAO.certificadas);

            // Detalle de Pedido
            ASPELPedidoDetalleDAO aspelPedidoDetalleDAO = new ASPELPedidoDetalleDAO();
            aspelPedidoDetalleDAO.setEmpresa(empresa);
            aspelPedidoDetalleDAO = (ASPELPedidoDetalleDAO)dsA.first(aspelPedidoDetalleDAO,
                    "CVE_DOC = '"+aspelPedidoDAO.CVE_DOC+"' AND CVE_ART = '"+aspelProductoDAO.CVE_ART+"'");
            if (aspelPedidoDetalleDAO==null)
                throw new WebException("No encontre el Detalle de Pedido ASPEL ["+aspelPedidoDAO.CVE_DOC+";"+aspelProductoDAO.CVE_ART+"].");

            // La perdio en el first(), la coloco de nuevo
            aspelPedidoDetalleDAO.setEmpresa(empresa);

            // Detalle de Factura
            ASPELFacturaDetalleDAO aspelFacturaDetalleDAO = new ASPELFacturaDetalleDAO();
            aspelFacturaDetalleDAO.setEmpresa(empresa);

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
            aspelFacturaDetalleDAO.APAR = 0.0d;
            aspelFacturaDetalleDAO.ACT_INV = "S";
            aspelFacturaDetalleDAO.NUM_ALM = aspelFacturaDAO.NUM_ALMA;
            aspelFacturaDetalleDAO.POLIT_APLI = "0";
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
            aspelFacturaDetalleDAO.IMPRIMIR = "S";
            aspelFacturaDetalleDAO.UUID = aspelProductoDAO.UUID;
            aspelFacturaDetalleDAO.VERSION_SINC = aspelProductoDAO.VERSION_SINC;
            aspelFacturaDetalleDAO.MAN_IEPS = aspelProductoDAO.MAN_IEPS;
            aspelFacturaDetalleDAO.APL_MAN_IMP = aspelProductoDAO.APL_MAN_IMP;
            aspelFacturaDetalleDAO.CUOTA_IEPS = aspelProductoDAO.CUOTA_IEPS;
            aspelFacturaDetalleDAO.APL_MAN_IEPS = aspelProductoDAO.APL_MAN_IEPS;
            aspelFacturaDetalleDAO.MTO_PORC = 0.0;
            aspelFacturaDetalleDAO.MTO_CUOTA = 0.0;
            aspelFacturaDetalleDAO.CVE_ESQ = aspelProductoDAO.CVE_ESQIMPU;
            aspelFacturaDetalleDAO.DESCR_ART = null;
            aspelFacturaDetalleDAO.PREC_NETO = 0.0d;
            aspelFacturaDetalleDAO.CVE_PRODSERV = aspelProductoDAO.CVE_PRODSERV;
            aspelFacturaDetalleDAO.CVE_UNIDAD = aspelProductoDAO.CVE_UNIDAD;

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
            dsA.insert(aspelFacturaDetalleDAO);

            //
            // Lote y Pedimento
            //
            if (!lotesPedimentos.isEmpty()) {
                // Folio del Enlace de Lote Pedimento
                aspelControlDAO.ID_TABLA = 67;
                if (!dsA.exists(aspelControlDAO))
                    dsA.insert(aspelControlDAO);

                aspelControlDAO.ULT_CVE ++;
                dsA.update(aspelControlDAO);

                aspelFacturaDetalleDAO.E_LTPD = aspelControlDAO.ULT_CVE;

                dsA.update(aspelFacturaDetalleDAO, new String[] {"E_LTPD"});

                for (LotePedimentoTO lotePedimentoTO : lotesPedimentos) {
                    // Descuento la Cantidad del Lote Pedimento
                    ASPELLotePedimentoDAO aspelLotePedimentoDAO = new ASPELLotePedimentoDAO();
                    aspelLotePedimentoDAO.setEmpresa(empresa);
                    aspelLotePedimentoDAO.REG_LTPD = lotePedimentoTO.REG_LTPD;

                    if (!dsA.exists(aspelLotePedimentoDAO))
                        throw new WebException("No encontre este Lote Pedimento ["+aspelLotePedimentoDAO+"].");

                    if (aspelLotePedimentoDAO.STATUS.compareTo("A")!=0)
                        throw new WebException("El estado del Lote ["+aspelLotePedimentoDAO+"] no es A.");

                    aspelLotePedimentoDAO.CANTIDAD = Numero.redondea(aspelLotePedimentoDAO.CANTIDAD - lotePedimentoTO.CANTIDAD);
                    if (aspelLotePedimentoDAO.CANTIDAD<0.0)
                        throw new WebException("La cantidad que queda en el Lote ["+aspelLotePedimentoDAO+"] es negativa ["+aspelLotePedimentoDAO.CANTIDAD+"].");

                    dsA.update(aspelLotePedimentoDAO, new String[] {"CANTIDAD"});

                    // Enlace de Lote Pedimento
                    ASPELEnlaceLotePedimentoDAO aspelEnlaceLotePedimentoDAO = new ASPELEnlaceLotePedimentoDAO();
                    aspelEnlaceLotePedimentoDAO.setEmpresa(empresa);
                    aspelEnlaceLotePedimentoDAO.E_LTPD = aspelControlDAO.ULT_CVE;
                    aspelEnlaceLotePedimentoDAO.REG_LTPD = aspelLotePedimentoDAO.REG_LTPD;
                    aspelEnlaceLotePedimentoDAO.CANTIDAD = Numero.redondea(lotePedimentoTO.CANTIDAD);
                    aspelEnlaceLotePedimentoDAO.PXRS = Numero.redondea(lotePedimentoTO.CANTIDAD);

                    dsA.insert(aspelEnlaceLotePedimentoDAO);
                }
            }

            //
            // Piezas Facturadas
            //
            double facturadas = Numero.redondea(aspelFacturaDetalleDAO.CANT);

            aspelProductoDAO.PEND_SURT -= facturadas;
            dsA.update(aspelProductoDAO, new String[] {"PEND_SURT"});

            aspelPedidoDetalleDAO.PXS -= facturadas;
            dsA.update(aspelPedidoDetalleDAO, new String[] {"PXS"});

            // Documento Anterior Enlazado
            ASPELDocumentoSiguienteDAO aspelDocumentoAnteriorDAO = new ASPELDocumentoSiguienteDAO();
            aspelDocumentoAnteriorDAO.setEmpresa(empresa);
            aspelDocumentoAnteriorDAO.TIP_DOC = aspelFacturaDAO.TIP_DOC;
            aspelDocumentoAnteriorDAO.CVE_DOC = aspelFacturaDAO.CVE_DOC;
            aspelDocumentoAnteriorDAO.ANT_SIG = "A";
            aspelDocumentoAnteriorDAO.TIP_DOC_E = "P";
            aspelDocumentoAnteriorDAO.CVE_DOC_E = aspelPedidoDAO.CVE_DOC;
            aspelDocumentoAnteriorDAO.PARTIDA = aspelFacturaDetalleDAO.NUM_PAR;
            aspelDocumentoAnteriorDAO.PART_E = aspelPedidoDetalleDAO.NUM_PAR;
            aspelDocumentoAnteriorDAO.CANT_E = facturadas;
            dsA.insert(aspelDocumentoAnteriorDAO);

            // Documento Siguiente Enlazado
            ASPELDocumentoSiguienteDAO aspelDocumentoSiguienteDAO = new ASPELDocumentoSiguienteDAO();
            aspelDocumentoSiguienteDAO.setEmpresa(empresa);
            aspelDocumentoSiguienteDAO.TIP_DOC = "P";
            aspelDocumentoSiguienteDAO.CVE_DOC = aspelPedidoDAO.CVE_DOC;
            aspelDocumentoSiguienteDAO.ANT_SIG = "S";
            aspelDocumentoSiguienteDAO.TIP_DOC_E = aspelFacturaDAO.TIP_DOC;
            aspelDocumentoSiguienteDAO.CVE_DOC_E = aspelFacturaDAO.CVE_DOC;
            aspelDocumentoSiguienteDAO.PARTIDA = aspelPedidoDetalleDAO.NUM_PAR;
            aspelDocumentoSiguienteDAO.PART_E = aspelFacturaDetalleDAO.NUM_PAR;
            aspelDocumentoSiguienteDAO.CANT_E = facturadas;
            dsA.insert(aspelDocumentoSiguienteDAO);
        }

        // Si no hay piezas por surtir, el ENLAZADO debe de cambiar a T
        double PXS = 0;
        ASPELPedidoDetalleDAO aspelPedidoDetalleDAO = new ASPELPedidoDetalleDAO();
        aspelPedidoDetalleDAO.setEmpresa(empresa);

        ArrayList<ASPELPedidoDetalleDAO> detallesP = dsA.select(aspelPedidoDetalleDAO, aspelPedidoDAO.getWhere());
        for (int indx=0; indx<detallesP.size(); indx++) {
            aspelPedidoDetalleDAO = detallesP.get(indx);
            PXS += aspelPedidoDetalleDAO.PXS;
        }
        if (PXS==0) {
            aspelPedidoDAO.ENLAZADO = "T";
            dsA.update(aspelPedidoDAO, new String[] {"ENLAZADO"});
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
        dsA.update(aspelFacturaDAO, new String[] {"CAN_TOT", "IMP_TOT1", "IMP_TOT2", "IMP_TOT3", "IMP_TOT4",
            "DES_TOT_PORC", "DES_FIN_PORC", "DES_TOT", "DES_FIN", "COM_TOT", "IMPORTE", "COM_TOT_PORC"});

        //
        return aspelFacturaDAO;
    }

    private void checaDisponibilidadInventario(String empresa, OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO, int almacen) throws Exception {
        StringBuilder errores = new StringBuilder();

        ArrayList<OrdenSurtidoPedidoCertificaDAO> detallesOS = ds.select(new OrdenSurtidoPedidoCertificaDAO(), ordenSurtidoPedidoDAO.getWhere());
        for(OrdenSurtidoPedidoCertificaDAO ordenSurtidoPedidoCertificaDAO : detallesOS) {
            // Solo con piezas surtidas
            if (ordenSurtidoPedidoCertificaDAO.certificadas==0.0)
                continue;

            ASPELProductoDAO aspelProductoDAO = new ASPELProductoDAO();
            aspelProductoDAO.setEmpresa(empresa);
            aspelProductoDAO.CVE_ART = ordenSurtidoPedidoCertificaDAO.codigo;

            if (!dsA.exists(aspelProductoDAO)) {
                errores.append("No existe este Producto [").append(aspelProductoDAO).append("]");
            }

            log.info(Reflector.toStringAllFields(aspelProductoDAO));

            //
            // Existencia de Producto
            //
            double EXIST = Numero.redondea(aspelProductoDAO.EXIST - ordenSurtidoPedidoCertificaDAO.certificadas);
            if (EXIST<0.0) {
                errores.append("No hay Existencia suficiente [").append(aspelProductoDAO.EXIST).append("] en el Producto [").append(aspelProductoDAO)
                        .append("] para facturar [").append(ordenSurtidoPedidoCertificaDAO.certificadas).append("] piezas.");
            }

            // Existencia MultiAlmacen
            ASPELProductoAlmacenDAO aspelProductoAlmacenDAO = new ASPELProductoAlmacenDAO();
            aspelProductoAlmacenDAO.setEmpresa(empresa);

            aspelProductoAlmacenDAO.CVE_ART = aspelProductoDAO.CVE_ART;
            aspelProductoAlmacenDAO.CVE_ALM = almacen;
            if (!dsA.exists(aspelProductoAlmacenDAO)) {
                errores.append("No existe este Producto [").append(aspelProductoDAO).append("] en el MultiAlmacen [").append(aspelProductoAlmacenDAO).append("]");
            }

            EXIST = Numero.redondea(aspelProductoAlmacenDAO.EXIST - ordenSurtidoPedidoCertificaDAO.certificadas);
            if (EXIST<0.0) {
                errores.append("No hay Existencia suficiente [").append(aspelProductoAlmacenDAO.EXIST).append("] en el Producto [").append(aspelProductoDAO)
                        .append("] en el MULTIALMACEN [").append(aspelProductoAlmacenDAO)
                        .append("] para facturar [").append(ordenSurtidoPedidoCertificaDAO.certificadas).append("] piezas.");
            }

            // Lotes
            if (aspelProductoDAO.CON_LOTE!=null
                    &&aspelProductoDAO.CON_LOTE.compareTo("S")==0) {
                ASPELLotePedimentoDAO aspelLotePedimentoDAO = new ASPELLotePedimentoDAO();
                aspelLotePedimentoDAO.setEmpresa(empresa);
                ArrayList<ASPELLotePedimentoDAO> pedimentos = dsA.select(aspelLotePedimentoDAO,
                        "CVE_ART = '"+aspelProductoDAO.CVE_ART+"' AND CVE_ALM = "+almacen+" AND STATUS = 'A' AND LOTE = '"+ordenSurtidoPedidoCertificaDAO.lote+"'",
                        "FCHCADUC, CANTIDAD DESC");
                if (pedimentos.isEmpty()) {
                    errores.append("No encontre Lotes para este producto [").append(aspelProductoDAO)
                            .append("].");
                } else {
                    // Suma las Cantidades que hay en los Lotes con status = 'A'
                    Double CANTIDAD = 0.0d;
                    for (int indx=0; indx<pedimentos.size(); indx++) {
                        aspelLotePedimentoDAO = pedimentos.get(indx);
                        aspelLotePedimentoDAO.setEmpresa(empresa);

                        CANTIDAD = Numero.redondea(CANTIDAD + aspelLotePedimentoDAO.CANTIDAD);
                    }
                    if (CANTIDAD<ordenSurtidoPedidoCertificaDAO.certificadas) {
                        errores.append("No hay Cantidad suficiente [").append(CANTIDAD).append("] en el Lote [")
                                .append(ordenSurtidoPedidoCertificaDAO.lote).append("] del MULTIALMACEN [").append(almacen)
                                .append("] de este Producto [").append(aspelProductoDAO)
                                .append("] para facturar [").append(ordenSurtidoPedidoCertificaDAO.certificadas).append("] piezas.");
                    }
                }
            }
            else {
                errores.append("Este Producto [").append(aspelProductoDAO)
                        .append("] en el MULTIALMACEN [").append(almacen)
                        .append("] no tiene control de LOTES.");
            }
        }

        if (errores.length()>0)
            throw new WebException(errores.toString());
    }

    private ArrayList<LotePedimentoTO> getLotesPedimentosParaDescontar(String empresa, ASPELProductoDAO aspelProductoDAO, int almacen, OrdenSurtidoPedidoCertificaDAO ordenSurtidoPedidoCertificaDAO) throws Exception {
        ArrayList <LotePedimentoTO> lotesPedimentos = new ArrayList<>();

        // Lotes
        if (aspelProductoDAO.CON_LOTE!=null
                &&aspelProductoDAO.CON_LOTE.compareTo("S")==0) {
            ASPELLotePedimentoDAO aspelLotePedimentoDAO = new ASPELLotePedimentoDAO();
            aspelLotePedimentoDAO.setEmpresa(empresa);
            ArrayList<ASPELLotePedimentoDAO> pedimentos = dsA.select(aspelLotePedimentoDAO,
                    "CVE_ART = '"+aspelProductoDAO.CVE_ART+"' AND CVE_ALM = "+almacen+" AND STATUS = 'A' AND LOTE = '"+ordenSurtidoPedidoCertificaDAO.lote+"'",
                    "FCHCADUC, CANTIDAD DESC");
            if (pedimentos.isEmpty()) {
                throw new WebException("No encontre Lotes para este producto ["+aspelProductoDAO+"] lote buscado ["+ordenSurtidoPedidoCertificaDAO.lote+"].");
            } else {
                double certificadas = ordenSurtidoPedidoCertificaDAO.certificadas;
                // Revisa las cantidades de los Lotes con status = 'A'
                for (int indx=0; indx<pedimentos.size(); indx++) {
                    if (certificadas==0.0d)
                        break;

                    aspelLotePedimentoDAO = pedimentos.get(indx);
                    aspelLotePedimentoDAO.setEmpresa(empresa);

                    aspelLotePedimentoDAO.CANTIDAD = Numero.redondea(aspelLotePedimentoDAO.CANTIDAD);

                    if (aspelLotePedimentoDAO.CANTIDAD>0.0d) {
                        Double CANTIDAD = aspelLotePedimentoDAO.CANTIDAD < certificadas ?
                            aspelLotePedimentoDAO.CANTIDAD : certificadas;
                        CANTIDAD = Numero.redondea(CANTIDAD);

                        LotePedimentoTO lotePedimentoTO = new LotePedimentoTO();
                        lotePedimentoTO.REG_LTPD = aspelLotePedimentoDAO.REG_LTPD;
                        lotePedimentoTO.CANTIDAD = CANTIDAD;
                        lotesPedimentos.add(lotePedimentoTO);

                        log.debug("Tomo ["+lotePedimentoTO.CANTIDAD+"] cantidad de este LotePedimento ["+lotePedimentoTO.REG_LTPD
                                +"] producto y almacen ["+aspelProductoDAO.CVE_ART+";"+almacen+"], certificadas ["+certificadas+"]");

                        certificadas = Numero.redondea(certificadas - CANTIDAD);
                    }
                }
            }
        }

        return lotesPedimentos;
    }

    private String getSeriePedido(String CVE_DOC) {
        StringBuilder serie = new StringBuilder();
        char[] chars = CVE_DOC.toCharArray();
        for (char c : chars) {
            if (Character.isDigit(c))
                break;
            serie.append(c);
        }
        String _serie = serie.toString();        
        if (_serie.compareTo("F")==0)
            return "BG";
        else if (_serie.compareTo("N")==0)
            return "NN";  
        else
            return _serie;
    }
}
