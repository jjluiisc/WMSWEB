package mx.reder.wms.reports;

import com.atcloud.dao.engine.DatabaseRecordEntity;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Letras;
import com.atcloud.util.Numero;
import com.atcloud.util.Reflector;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.dao.entity.ASPELClienteDAO;
import mx.reder.wms.dao.entity.ASPELInformacionEnvioDAO;
import mx.reder.wms.dao.entity.ASPELPedidoDAO;
import mx.reder.wms.dao.entity.CompaniaDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDAO;
import mx.reder.wms.dao.entity.UsuarioDAO;
import mx.reder.wms.to.OrdenSurtidoPedidoCertificaTO;
import mx.reder.wms.to.DireccionTO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.log4j.Logger;

public class OrdenSurtidoPedidoCertificaDS extends ReporteadorDS {
    static Logger log = Logger.getLogger(OrdenSurtidoPedidoCertificaDS.class.getName());

    public DireccionTO direccionTO = null;
    public DireccionTO direccionEnvioTO = null;
    public OrdenSurtidoPedidoDAO ordenSurtidoPedidoDAO = null;
    public UsuarioDAO usuarioDAO = null;
    public ASPELClienteDAO aspelClienteDAO = null;
    public ASPELPedidoDAO aspelPedidoDAO = null;
    private String tipoFactura = "";
    private String nombreVendedor = "";
    private int diasCredito = 0;
    private String totalLetras = "";
    private Date fechaCredito = null;
    private String referencia = "";

    public void getDataTicket(DatabaseServices ds, String compania, String flsurtido, String contenedor) throws Exception {
        String EsCertifica = "0";
        double totalTotal = 0.0;
        
        ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO(compania, Numero.getIntFromString(flsurtido));
        ds.exists(ordenSurtidoPedidoDAO);
        
        if(ordenSurtidoPedidoDAO.pedido.startsWith("BG") || 
                ordenSurtidoPedidoDAO.pedido.startsWith("BO") || 
                ordenSurtidoPedidoDAO.pedido.startsWith("BX") || 
                ordenSurtidoPedidoDAO.pedido.startsWith("EFX") || 
                ordenSurtidoPedidoDAO.pedido.startsWith("EFO")){
                setTipoFactura("FA");
        }else if(ordenSurtidoPedidoDAO.pedido.startsWith("NN") || 
                ordenSurtidoPedidoDAO.pedido.startsWith("NO") || 
                ordenSurtidoPedidoDAO.pedido.startsWith("NX") || 
                ordenSurtidoPedidoDAO.pedido.startsWith("ENO") || 
                ordenSurtidoPedidoDAO.pedido.startsWith("ENX")){
                setTipoFactura("NV");
        }
        
        DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
        Connection connectionAspel = databaseDataSourceAspel.getConnection();
        DatabaseServices dsAspel = new DatabaseServices(connectionAspel);
        
        StringBuilder sqlVendedor = new StringBuilder();
        sqlVendedor.append("SELECT NOMBRE FROM REDER20.dbo.VEND"+compania+"  WHERE CVE_VEND =  '"+ ordenSurtidoPedidoDAO.vendedor +"'");
        ArrayList<DatabaseRecordEntity> arrayVendedor = dsAspel.collection(sqlVendedor.toString());

        for(DatabaseRecordEntity record : arrayVendedor) {
            if(record.get("NOMBRE")!= null)
                setNombreVendedor(record.getString("NOMBRE"));
        }
        //SELECT ISNULL(DIASCRED,0) FROM CLIE01
        StringBuilder sqlClaves = new StringBuilder();
        sqlClaves.append("SELECT L.CAMPLIB10, ISNULL(C.DIASCRED,0) AS DIASCRED, ISNULL(C.REFERDIR,'') AS REFERDIR FROM REDER20.dbo.CLIE"+compania+" C INNER JOIN REDER20.dbo.CLIE_CLIB"+compania+" L  ON C.CLAVE = L.CVE_CLIE WHERE L.CVE_CLIE =  '"+ ordenSurtidoPedidoDAO.cliente +"'");
        ArrayList<DatabaseRecordEntity> arrayClaves = dsAspel.collection(sqlClaves.toString());
        
        for(DatabaseRecordEntity record : arrayClaves) {
            if(record.get("CAMPLIB10")!= null)
                EsCertifica = record.getString("CAMPLIB10");     
            
            setDiasCredito(record.getInt("DIASCRED"));
            setReferencia(record.getString("REFERDIR"));
        }
        
        connectionAspel.close();
        databaseDataSourceAspel.close();
        
        ////////////////////////////////////////////////////////////////////////
        
        StringBuilder sql = new StringBuilder();
        
        /*if(EsCertifica.equals("0")){
            
            sql.append("SELECT ospc.compania, ospc.flsurtido, ospc.partida, CONVERT(INT,1) AS idlote, ospc.idcontenedor, ospc.codigo, osd.descripcion, ")
                .append("ospc.contenedor, ospc.lote, ospc.fecaducidad, ospc.surtidas AS certificadas, osd.preciopublico, osd.precio, osd.total ")
                .append("FROM OrdenSurtidoPedidoContenedor ospc ")
                .append("LEFT OUTER JOIN OrdenSurtidoPedidoDetalle osd ON ospc.codigo = osd.codigo AND ospc.partida = osd.partida AND ospc.flsurtido = osd.flsurtido AND ospc.compania = osd.compania ");
            sql.append("WHERE ospc.compania = '").append(compania).append("' ")
                .append("AND ospc.flsurtido = ").append(flsurtido).append(" ");
            if (contenedor!=null)
                sql.append("AND ospc.contenedor = '").append(contenedor).append("' ");
            sql.append("ORDER BY ospc.compania, ospc.flsurtido, ospc.partida, ospc.idcontenedor");
            
        }else{*/
            
            sql.append("SELECT ospc.compania, ospc.flsurtido, ospc.partida, ospc.idlote, ospc.idcontenedor, ospc.codigo, ospc.descripcion, ")
                .append("ospc.contenedor, ospc.lote, ospc.fecaducidad, ospc.certificadas, osd.preciopublico, osd.precio, (ospc.certificadas * osd.precio) AS total, osd.iva ")
                .append("FROM OrdenSurtidoPedidoCertifica ospc ")
                .append("LEFT OUTER JOIN OrdenSurtidoPedidoDetalle osd ON ospc.codigo = osd.codigo AND ospc.partida = osd.partida AND ospc.flsurtido = osd.flsurtido AND ospc.compania = osd.compania ");
            sql.append("WHERE ospc.compania = '").append(compania).append("' ")
                .append("AND ospc.flsurtido = ").append(flsurtido).append(" ");
            if (contenedor!=null)
                sql.append("AND ospc.contenedor = '").append(contenedor).append("' ");
            sql.append("AND ospc.certificadas > 0 ");
            sql.append("ORDER BY ospc.compania, ospc.flsurtido, ospc.partida, ospc.idcontenedor");
            
        //}

        datos = ds.collection(new OrdenSurtidoPedidoCertificaTO(), sql.toString());
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, getDiasCredito());
        setFechaCredito(c.getTime());
        
        for(Object object : datos){
            OrdenSurtidoPedidoCertificaTO ospc = (OrdenSurtidoPedidoCertificaTO) object;
            totalTotal += ospc.total;
        }
        Letras letras = new Letras();
        setTotalLetras(letras.letras(totalTotal));
        
        companiaDAO = new CompaniaDAO(compania);
        ds.exists(companiaDAO);

        sql = new StringBuilder();
        sql.append("SELECT d.direccion, d.calle, d.noexterior, d.nointerior, d.colonia, d.poblacion, d.entidadfederativa, d.pais, d.codigopostal, ")
            .append("c.nombre AS nombrecolonia, p.nombre AS nombrepoblacion, e.nombre AS nombreentidadfederativa, pa.nombre AS nombrepais ")
            .append("FROM Direccion d ")
            .append("INNER JOIN Colonia c ON c.colonia = d.colonia ")
            .append("INNER JOIN Poblacion p ON p.poblacion = d.poblacion ")
            .append("INNER JOIN EntidadFederativa e ON e.entidadfederativa = d.entidadfederativa ")
            .append("INNER JOIN Pais pa ON pa.pais = d.pais ");
        sql.append("WHERE d.direccion = '").append(companiaDAO.direccion).append("' ");

        ArrayList direcciones = ds.collection(new DireccionTO(), sql.toString());
        if (direcciones.isEmpty())
            direccionTO = new DireccionTO();
        else
            direccionTO = (DireccionTO)direcciones.get(0);

        //ordenSurtidoPedidoDAO = new OrdenSurtidoPedidoDAO(compania, Numero.getIntFromString(flsurtido));
        //ds.exists(ordenSurtidoPedidoDAO);

        usuarioDAO = new UsuarioDAO(ordenSurtidoPedidoDAO.usuario);
        ds.exists(usuarioDAO);

        aspelClienteDAO = new ASPELClienteDAO();
        aspelClienteDAO.setEmpresa(compania);
        aspelClienteDAO.CLAVE = ordenSurtidoPedidoDAO.cliente;
        ds.exists(aspelClienteDAO);

        aspelPedidoDAO = new ASPELPedidoDAO();
        aspelPedidoDAO.setEmpresa(compania);
        aspelPedidoDAO.CVE_DOC = ordenSurtidoPedidoDAO.pedido;
        ds.exists(aspelPedidoDAO);


        direccionEnvioTO = null;
        // La direccion del Pedido
        /*if (aspelPedidoDAO.DAT_ENVIO!=null&&aspelPedidoDAO.DAT_ENVIO!=0) {
            direccionEnvioTO = new DireccionTO();

            ASPELInformacionEnvioDAO aspelInformacionEnvioPDAO = new ASPELInformacionEnvioDAO();
            aspelInformacionEnvioPDAO.setEmpresa(compania);
            aspelInformacionEnvioPDAO.CVE_INFO = aspelPedidoDAO.DAT_ENVIO;
            ds.exists(aspelInformacionEnvioPDAO);

            direccionEnvioTO.direccion = "";
            direccionEnvioTO.calle = aspelInformacionEnvioPDAO.CALLE;
            direccionEnvioTO.noexterior = aspelInformacionEnvioPDAO.NUMEXT;
            direccionEnvioTO.nointerior = aspelInformacionEnvioPDAO.NUMINT;
            direccionEnvioTO.colonia = aspelInformacionEnvioPDAO.COLONIA;
            direccionEnvioTO.poblacion = aspelInformacionEnvioPDAO.MUNICIPIO;
            direccionEnvioTO.entidadfederativa = aspelInformacionEnvioPDAO.ESTADO;
            direccionEnvioTO.pais = aspelInformacionEnvioPDAO.PAIS;
            direccionEnvioTO.codigopostal = aspelInformacionEnvioPDAO.CODIGO;
            direccionEnvioTO.nombrecolonia = aspelInformacionEnvioPDAO.COLONIA;
            direccionEnvioTO.nombrepoblacion = aspelInformacionEnvioPDAO.MUNICIPIO;
            direccionEnvioTO.nombreentidadfederativa = aspelInformacionEnvioPDAO.ESTADO;
            direccionEnvioTO.nombrepais = aspelInformacionEnvioPDAO.PAIS;
        }*/
        // La Direccion de Envio del Cliente
        if (aspelClienteDAO.CALLE_ENVIO!=null&&!aspelClienteDAO.CALLE_ENVIO.isEmpty()) {
            direccionEnvioTO = new DireccionTO();
            direccionEnvioTO.direccion = "";
            direccionEnvioTO.calle = aspelClienteDAO.CALLE_ENVIO;
            direccionEnvioTO.noexterior = aspelClienteDAO.NUMEXT_ENVIO;
            direccionEnvioTO.nointerior = aspelClienteDAO.NUMINT_ENVIO;
            direccionEnvioTO.colonia = aspelClienteDAO.COLONIA_ENVIO;
            direccionEnvioTO.poblacion = aspelClienteDAO.MUNICIPIO_ENVIO;
            direccionEnvioTO.entidadfederativa = aspelClienteDAO.ESTADO_ENVIO;
            direccionEnvioTO.pais = aspelClienteDAO.PAIS_ENVIO;
            direccionEnvioTO.codigopostal = aspelClienteDAO.CODIGO_ENVIO;
            direccionEnvioTO.nombrecolonia = aspelClienteDAO.COLONIA_ENVIO;
            direccionEnvioTO.nombrepoblacion = aspelClienteDAO.MUNICIPIO_ENVIO;
            direccionEnvioTO.nombreentidadfederativa = aspelClienteDAO.ESTADO_ENVIO;
            direccionEnvioTO.nombrepais = aspelClienteDAO.PAIS_ENVIO;
        }
        // La Direccion Fiscal del Cliente
        if (direccionEnvioTO==null) {
            direccionEnvioTO = new DireccionTO();
            direccionEnvioTO.direccion = "";
            direccionEnvioTO.calle = aspelClienteDAO.CALLE;
            direccionEnvioTO.noexterior = aspelClienteDAO.NUMEXT;
            direccionEnvioTO.nointerior = aspelClienteDAO.NUMINT;
            direccionEnvioTO.colonia = aspelClienteDAO.COLONIA;
            direccionEnvioTO.poblacion = aspelClienteDAO.MUNICIPIO;
            direccionEnvioTO.entidadfederativa = aspelClienteDAO.ESTADO;
            direccionEnvioTO.pais = aspelClienteDAO.PAIS;
            direccionEnvioTO.codigopostal = aspelClienteDAO.CODIGO;
            direccionEnvioTO.nombrecolonia = aspelClienteDAO.COLONIA;
            direccionEnvioTO.nombrepoblacion = aspelClienteDAO.MUNICIPIO;
            direccionEnvioTO.nombreentidadfederativa = aspelClienteDAO.ESTADO;
            direccionEnvioTO.nombrepais = aspelClienteDAO.PAIS;
        }
        // La Direccion de la Compania
        if (direccionEnvioTO==null) {
            direccionEnvioTO = new DireccionTO();
            Reflector.copyAllFields(direccionTO, direccionEnvioTO);
        }
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        String name = jrField.getName();

        try {

            Field field = detalleTO.getClass().getField(name);
            Object value = field.get(detalleTO);
            return value;

        } catch(NoSuchFieldException e) {

            try {

                if (name.endsWith("cliente")) {
                    name = name.substring(0, name.length() - "cliente".length());
                    Field field = ASPELClienteDAO.class.getField(name);
                    Object value = field.get(aspelClienteDAO);
                    return value;
                } else if (name.endsWith("ordensurtidopedido")) {
                    name = name.substring(0, name.length() - "ordensurtidopedido".length());
                    Field field = OrdenSurtidoPedidoDAO.class.getField(name);
                    Object value = field.get(ordenSurtidoPedidoDAO);
                    return value;
                } else if (name.endsWith("pedido")) {
                    name = name.substring(0, name.length() - "pedido".length());
                    Field field = ASPELPedidoDAO.class.getField(name);
                    Object value = field.get(aspelPedidoDAO);
                    return value;
                } else if (name.endsWith("usuario")) {
                    name = name.substring(0, name.length() - "usuario".length());
                    Field field = UsuarioDAO.class.getField(name);
                    Object value = field.get(usuarioDAO);
                    return value;
                } else if (name.endsWith("direccionenvio")) {
                    name = name.substring(0, name.length() - "direccionenvio".length());
                    Field field = DireccionTO.class.getField(name);
                    Object value = field.get(direccionEnvioTO);
                    return value;
                } else if (name.endsWith("direccion")) {
                    name = name.substring(0, name.length() - "direccion".length());
                    Field field = DireccionTO.class.getField(name);
                    Object value = field.get(direccionTO);
                    return value;
                } else if (name.endsWith("compania")) {
                    name = name.substring(0, name.length() - "compania".length());
                    Field field = CompaniaDAO.class.getField(name);
                    Object value = field.get(companiaDAO);
                    return value;
                }

            } catch(Exception eex) {
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getTipoFactura() {
        return tipoFactura;
    }

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }

    public int getDiasCredito() {
        return diasCredito;
    }

    public void setDiasCredito(int diasCredito) {
        this.diasCredito = diasCredito;
    }

    public String getTotalLetras() {
        return totalLetras;
    }

    public void setTotalLetras(String totalLetras) {
        this.totalLetras = totalLetras;
    }

    public Date getFechaCredito() {
        return fechaCredito;
    }

    public void setFechaCredito(Date fechaCredito) {
        this.fechaCredito = fechaCredito;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    
    
    
}
