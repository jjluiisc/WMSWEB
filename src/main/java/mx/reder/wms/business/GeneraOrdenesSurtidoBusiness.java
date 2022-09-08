package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.util.ArrayList;
import java.util.Date;
import mx.reder.wms.collection.ASPELPedidosCollection;
import mx.reder.wms.collection.ASPELPedidosDetallesCollection;
import mx.reder.wms.dao.GenericDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoBitacoraDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDetalleDAO;
import mx.reder.wms.dao.entity.PedidoBitacoraDAO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class GeneraOrdenesSurtidoBusiness {
    static Logger log = Logger.getLogger(GeneraOrdenesSurtidoBusiness.class);
    private DatabaseServices ds;
    private DatabaseServices dsAspel;

    private ArrayList surtidos;

    public void setDatabaseServices(DatabaseServices ds) {
        this.ds = ds;
    }
    
    public void setDatabaseAspelServices(DatabaseServices dsAspel) {
        this.dsAspel = dsAspel;
    }

    public void genera(String registros, String compania, String usuario) throws Exception {
        surtidos = new ArrayList();
        String[] tokens = registros.split("\\|");
        for (String token : tokens) {
            if (token.isEmpty())
                continue;
            String[] tokensD = token.split(";");
            if (tokensD.length<1)
                continue;

            String _cve_doc = tokensD[0];

            ASPELPedidosCollection aspelPedidosCollection = new ASPELPedidosCollection();
            aspelPedidosCollection.setEmpresa(compania);
            ArrayList array = dsAspel.collection(aspelPedidosCollection, aspelPedidosCollection.getSQL("CVE_DOC = '"+_cve_doc+"'"));
            if (array.isEmpty())
                throw new WebException("Este Pedido no existe ["+_cve_doc+"]");

            aspelPedidosCollection = (ASPELPedidosCollection)array.get(0);

            if (aspelPedidosCollection.STATUS.compareTo("O")!=0)
                throw new WebException("Este Pedido ["+_cve_doc+"] no tiene el status O.");

            generaOrdenesSurtidoPedido(aspelPedidosCollection, compania, usuario);

            //
            ASPELPedidoDAO aspelPedidoDAO = new ASPELPedidoDAO();
            aspelPedidoDAO.setEmpresa(compania);
            aspelPedidoDAO.CVE_DOC = aspelPedidosCollection.CVE_DOC;
            aspelPedidoDAO.STATUS = Constantes.ESTADO_ASPEL_SURTIENDO;

            dsAspel.update(aspelPedidoDAO, new String[] {"STATUS"});

            PedidoBitacoraDAO pedidoBitacoraDAO = new PedidoBitacoraDAO();
            pedidoBitacoraDAO.id = null;
            pedidoBitacoraDAO.compania = compania;
            pedidoBitacoraDAO.pedido = aspelPedidosCollection.CVE_DOC;
            pedidoBitacoraDAO.status = Constantes.ESTADO_PORSURTIR;
            pedidoBitacoraDAO.fechabitacora = new Date();
            pedidoBitacoraDAO.usuario = usuario;

            ds.insert(pedidoBitacoraDAO);
        }
    }

    private void generaOrdenesSurtidoPedido(ASPELPedidosCollection aspelPedidosCollection, String compania, String usuario) throws Exception {
        ArrayList<ASPELPedidosDetallesCollection> corrugados = new ArrayList<>();
        ArrayList<ASPELPedidosDetallesCollection> liquidos = new ArrayList<>();
        ArrayList<ASPELPedidosDetallesCollection> surtido = new ArrayList<>();

        ASPELPedidosDetallesCollection aspelPedidosDetallesCollection = new ASPELPedidosDetallesCollection();
        aspelPedidosDetallesCollection.setEmpresa(compania);
        aspelPedidosDetallesCollection.CVE_DOC = aspelPedidosCollection.CVE_DOC;
        ArrayList detalles = dsAspel.collection(aspelPedidosDetallesCollection, aspelPedidosDetallesCollection.getSQL());

        for (Object object : detalles) {
            aspelPedidosDetallesCollection = (ASPELPedidosDetallesCollection)object;
            log.debug(Reflector.toStringAllFields(aspelPedidosDetallesCollection));

            // Corrugados
            if (aspelPedidosDetallesCollection.CAMPLIB6>1.0) {
                double cajas = (double)((int)(aspelPedidosDetallesCollection.CANT / aspelPedidosDetallesCollection.CAMPLIB6));
                if (cajas > 0.0) {
                    double piezasCajas = Numero.redondea2(cajas * aspelPedidosDetallesCollection.CAMPLIB6);
                    double piezas = Numero.redondea2(aspelPedidosDetallesCollection.CANT - piezasCajas);

                    ASPELPedidosDetallesCollection aspelPedidosDetallesCollection1 = new ASPELPedidosDetallesCollection();
                    Reflector.copyAllFields(aspelPedidosDetallesCollection, aspelPedidosDetallesCollection1);
                    aspelPedidosDetallesCollection1.CANT = piezasCajas;
                    corrugados.add(aspelPedidosDetallesCollection1);

                    // No hay piezas sueltas, solo cajas
                    if (piezas==0.0)
                        continue;

                    aspelPedidosDetallesCollection.CANT = piezas;
                }
            }

            // Liquidos
            if (aspelPedidosDetallesCollection.CAMPLIB15!=null&&aspelPedidosDetallesCollection.CAMPLIB15.compareTo("L")==0) {
                liquidos.add(aspelPedidosDetallesCollection);
            } else {
                // Normales
                surtido.add(aspelPedidosDetallesCollection);
            }
        }

        if (corrugados.size()>0)
            generaOrdenSurtidoPedido(aspelPedidosCollection, corrugados, "CORRUGADOS", compania, usuario);
        if (liquidos.size()>0)
            generaOrdenSurtidoPedido(aspelPedidosCollection, liquidos, "LIQUIDOS", compania, usuario);
        if (surtido.size()>0)
            generaOrdenSurtidoPedido(aspelPedidosCollection, surtido, "SURTIDO", compania, usuario);
    }

    private void generaOrdenSurtidoPedido(ASPELPedidosCollection aspelPedidosCollection, ArrayList<ASPELPedidosDetallesCollection> detalles,
            String equipo, String compania, String usuario) throws Exception {
        
        OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO();
        ordenSurtidoPedidoDAO.compania = compania;
        ordenSurtidoPedidoDAO.flsurtido = GenericDAO.obtenerSiguienteFolio(ds, compania, Constantes.FOLIO_ORDENSURTIDO);
        ordenSurtidoPedidoDAO.pedido = aspelPedidosCollection.CVE_DOC;
        ordenSurtidoPedidoDAO.status = Constantes.ESTADO_PENDIENTE;
        ordenSurtidoPedidoDAO.fechastatus = new Date();
        ordenSurtidoPedidoDAO.usuario = usuario;
        ordenSurtidoPedidoDAO.equipo = equipo;
        ordenSurtidoPedidoDAO.surtidor = "";
        ordenSurtidoPedidoDAO.fechapedido = aspelPedidosCollection.FECHA_DOC;
        ordenSurtidoPedidoDAO.cliente = aspelPedidosCollection.CVE_CLPV;
        ordenSurtidoPedidoDAO.nombrecliente = aspelPedidosCollection.NOMBRE;
        ordenSurtidoPedidoDAO.vendedor = aspelPedidosCollection.CVE_VEND;
        ordenSurtidoPedidoDAO.ruta = aspelPedidosCollection.CAMPLIB1;
        ordenSurtidoPedidoDAO.fechasurtido = new Date();
        ordenSurtidoPedidoDAO.fechainicio = null;
        ordenSurtidoPedidoDAO.fechatermino = null;
        ordenSurtidoPedidoDAO.fechaconfirmada = null;
        ordenSurtidoPedidoDAO.fechafacturada = null;
        ordenSurtidoPedidoDAO.cantidad = 0.0;
        ordenSurtidoPedidoDAO.surtidas = 0.0;
        ordenSurtidoPedidoDAO.certificadas = 0.0;
        ordenSurtidoPedidoDAO.total = 0.0;
        ordenSurtidoPedidoDAO.detalles = 0;

        ds.insert(ordenSurtidoPedidoDAO);

        OrdenSurtidoPedidoBitacoraDAO ordenSurtidoPedidoBitacoraDAO = new OrdenSurtidoPedidoBitacoraDAO();
        Reflector.copyAllFields(ordenSurtidoPedidoDAO, ordenSurtidoPedidoBitacoraDAO);
        ordenSurtidoPedidoBitacoraDAO.id = null;
        ordenSurtidoPedidoBitacoraDAO.fechabitacora = new Date();
        ordenSurtidoPedidoBitacoraDAO.usuario = usuario;
        ordenSurtidoPedidoBitacoraDAO.status = Constantes.ESTADO_PENDIENTE;

        ds.insert(ordenSurtidoPedidoBitacoraDAO);

        int partida = 0;
        for(ASPELPedidosDetallesCollection aspelPedidosDetallesCollection : detalles) {
            OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
            ordenSurtidoPedidoDetalleDAO.compania = ordenSurtidoPedidoDAO.compania;
            ordenSurtidoPedidoDetalleDAO.flsurtido = ordenSurtidoPedidoDAO.flsurtido;
            ordenSurtidoPedidoDetalleDAO.partida = ++partida;
            ordenSurtidoPedidoDetalleDAO.pedido = aspelPedidosCollection.CVE_DOC;
            ordenSurtidoPedidoDetalleDAO.codigo = aspelPedidosDetallesCollection.CVE_ART;
            ordenSurtidoPedidoDetalleDAO.descripcion = aspelPedidosDetallesCollection.DESCR;
            ordenSurtidoPedidoDetalleDAO.ubicacion = aspelPedidosDetallesCollection.CTRL_ALM;
            ordenSurtidoPedidoDetalleDAO.cantidad = aspelPedidosDetallesCollection.CANT;
            ordenSurtidoPedidoDetalleDAO.surtidas = 0.0;
            ordenSurtidoPedidoDetalleDAO.certificadas = 0.0;
            ordenSurtidoPedidoDetalleDAO.precio = aspelPedidosDetallesCollection.PREC;
            ordenSurtidoPedidoDetalleDAO.total = aspelPedidosDetallesCollection.TOT_PARTIDA;
            ordenSurtidoPedidoDetalleDAO.preciopublico = aspelPedidosDetallesCollection.CAMPLIB5;
            ordenSurtidoPedidoDetalleDAO.iva = aspelPedidosDetallesCollection.IMPU4;

            ds.insert(ordenSurtidoPedidoDetalleDAO);

            ordenSurtidoPedidoDAO.cantidad += ordenSurtidoPedidoDetalleDAO.cantidad;
            ordenSurtidoPedidoDAO.surtidas += ordenSurtidoPedidoDetalleDAO.surtidas;
            ordenSurtidoPedidoDAO.total += ordenSurtidoPedidoDetalleDAO.total;
            ordenSurtidoPedidoDAO.detalles ++;
        }

        ds.update(ordenSurtidoPedidoDAO, new String[] {"cantidad", "surtidas", "total", "detalles"});

        surtidos.add(ordenSurtidoPedidoDAO);
        
    }

    public ArrayList getSurtidos() {
        return surtidos;
    }
}
