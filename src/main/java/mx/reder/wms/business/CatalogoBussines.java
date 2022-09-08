/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseRecordEntity;
import com.atcloud.dao.engine.DatabaseServices;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.dao.entity.CVES_ALTERDAO;
import mx.reder.wms.dao.entity.CampoLibre;
import mx.reder.wms.dao.entity.LTPDDAO;
import mx.reder.wms.dao.entity.OrdenSurtidoPedidoDetalleDAO;
import mx.reder.wms.util.Configuracion;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Luis
 */
public class CatalogoBussines {
    static Logger log = Logger.getLogger(CatalogoBussines.class);
    //private File dataDir = null;
    //private ArrayList<File> files = new ArrayList<>();
    public boolean actualizaLotes = false;
    public boolean actualizaCampoLibre = false;
    
    public void parseArgs(String[] args) {
        for(int indx=0; indx<args.length; indx++) {
            if (args[indx].compareTo("--todos")==0) {
                actualizaLotes = true;
                actualizaCampoLibre =  true;
            }
            else if (args[indx].compareTo("--actualizaLotes")==0) {
                actualizaLotes = true;
            }
        }
    }
    
    //public void setDataDir(File dataDir) {
    //    this.dataDir = dataDir;
    //}

    public void actualizaCatalogos(String compania) throws Exception {
        if (actualizaLotes)
            actualizaCatalogosLotes(compania);
        if (actualizaCampoLibre)
            actualizaDetallesOrdenesSurtido(compania);
    }
    
    public void actualizaCatalogosLotes(String compania) throws Exception {
        log.debug("Conectando lotes ["+compania+"] ... ");
        
        DatabaseDataSource databaseDataSource = new DatabaseDataSource("WMS");
        Connection connection = databaseDataSource.getConnection();
        DatabaseServices ds = new DatabaseServices(connection);
        
        DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
        Connection connectionAspel = databaseDataSourceAspel.getConnection();
        DatabaseServices dsAspel = new DatabaseServices(connectionAspel);
        
        ds.update("DELETE FROM LTPD WHERE compania = '"+compania+"'");
        
        StringBuilder sqlLotes = new StringBuilder();
        sqlLotes.append("SELECT REG_LTPD, CVE_ART, LOTE, PEDIMENTO, CVE_ALM, CONVERT(DECIMAL(12,6),CANTIDAD) AS CANTIDAD, FCHULTMOV, FCHCADUC  FROM REDER20.dbo.LTPD"+compania+" ")
            .append("WHERE STATUS = 'A' AND CANTIDAD > 0 OR (CANTIDAD = 0 AND FCHULTMOV > DATEADD(DAY, -60, GETDATE()))");
            
        ArrayList<DatabaseRecordEntity> arrayLotes = dsAspel.collection(sqlLotes.toString());
        
        log.debug("Lotes "+sqlLotes.toString());    
        
        for(DatabaseRecordEntity record : arrayLotes) {
            LTPDDAO ltpd = new LTPDDAO();
            ltpd.REG_LTPD = record.getInt("REG_LTPD");
            ltpd.compania = compania;
            ltpd.CVE_ART = record.getString("CVE_ART");
            ltpd.LOTE = record.getString("LOTE");
            ltpd.PEDIMENTO = record.getString("PEDIMENTO");
            ltpd.CVE_ALM = record.getInt("CVE_ALM");
            if(record.get("FCHCADUC") != null)
                ltpd.FCHCADUC = record.getDate("FCHCADUC");
            if(record.get("FCHULTMOV") != null)
                ltpd.FCHULTMOV = record.getDate("FCHULTMOV");
            ltpd.CANTIDAD = record.getBigDecimal("CANTIDAD");        
            ds.insert(ltpd);
        }
        
        //Claves alternas
        
        ds.update("DELETE FROM CVES_ALTER WHERE compania = '"+compania+"'");
        
        StringBuilder sqlClaves = new StringBuilder();
        sqlClaves.append("SELECT CVE_ART, CVE_ALTER, TIPO, CVE_CLPV FROM REDER20.dbo.CVES_ALTER"+compania+" ");
            
        ArrayList<DatabaseRecordEntity> arrayClaves = dsAspel.collection(sqlClaves.toString());
        log.debug("Claves "+sqlClaves.toString());    
        
        for(DatabaseRecordEntity record : arrayClaves) {
            CVES_ALTERDAO clave = new CVES_ALTERDAO();
            clave.CVE_ART = record.getString("CVE_ART");
            clave.CVE_ALTER = record.getString("CVE_ALTER");
            clave.compania = compania;
            if(record.get("TIPO")!= null)
                clave.TIPO = record.getString("TIPO");
            if(record.get("CVE_CLPV")!= null)
                clave.CVE_CLPV = record.getString("CVE_CLPV");
            
            ds.insert(clave);
        }
        
        connectionAspel.close();
        databaseDataSourceAspel.close();
        
        connection.close();
        databaseDataSource.close();

        //
        //
        //
        log.debug("Fin carga lotes.");
    }
    
    public void actualizaDetallesOrdenesSurtido(String compania) throws Exception {
        log.debug("Conectando campos libres ["+compania+"] ... ");
        
        DatabaseDataSource databaseDataSource = new DatabaseDataSource("WMS");
        Connection connection = databaseDataSource.getConnection();
        DatabaseServices ds = new DatabaseServices(connection);
        
        DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
        Connection connectionAspel = databaseDataSourceAspel.getConnection();
        DatabaseServices dsAspel = new DatabaseServices(connectionAspel);
        
        StringBuilder sqlInve_clib = new StringBuilder();
        sqlInve_clib.append("SELECT il.CVE_PROD, il.CAMPLIB5 FROM REDER20.dbo.INVE_CLIB"+compania+" il  WHERE il.CAMPLIB5 IS NOT NULL ");
            
        ArrayList<DatabaseRecordEntity> arrayCamposLibres = dsAspel.collection(sqlInve_clib.toString());
        
        log.debug("Campos libres "+sqlInve_clib.toString()); 
        
        Map<String,CampoLibre> camposLibros = new HashMap<String, CampoLibre>();
        
        for(DatabaseRecordEntity record : arrayCamposLibres) {
            CampoLibre ltpd = new CampoLibre();
            ltpd.CVE_ART = record.getString("CVE_PROD");
            ltpd.PRECIO_PUBLICO = record.getDouble("CAMPLIB5");        
            camposLibros.put(ltpd.CVE_ART, ltpd);
        }
        
        //Ordenes de surtido
        
        StringBuilder sqlClaves = new StringBuilder();
        sqlClaves.append("SELECT compania, flsurtido, partida, pedido, codigo, preciopublico FROM OrdenSurtidoPedidoDetalle  WHERE  compania = '"+compania+"' AND (preciopublico = 0 OR preciopublico IS NULL) ");
            
        ArrayList<DatabaseRecordEntity> arrayClaves = ds.collection(sqlClaves.toString());
        log.debug("Detalles ordenes surtido "+sqlClaves.toString());    
        
        for(DatabaseRecordEntity record : arrayClaves) {
            String key = record.getString("codigo");
            CampoLibre ltpd = camposLibros.get(key);
            if(ltpd != null){
                
                OrdenSurtidoPedidoDetalleDAO ordenSurtidoPedidoDetalleDAO = new OrdenSurtidoPedidoDetalleDAO();
                ordenSurtidoPedidoDetalleDAO.compania = record.getString("compania");
                ordenSurtidoPedidoDetalleDAO.flsurtido = record.getInt("flsurtido");
                ordenSurtidoPedidoDetalleDAO.partida = record.getInt("partida"); 
                ordenSurtidoPedidoDetalleDAO.pedido = record.getString("pedido"); 
                ordenSurtidoPedidoDetalleDAO.codigo = record.getString("codigo"); 
                ordenSurtidoPedidoDetalleDAO.preciopublico = ltpd.PRECIO_PUBLICO;
                
                ds.update(ordenSurtidoPedidoDetalleDAO, new String[] {"preciopublico"});
            }
        }
        
        connectionAspel.close();
        databaseDataSourceAspel.close();
        
        connection.close();
        databaseDataSource.close();

        //
        //
        //
        log.debug("Fin actualizacion Campos libres.");
    }
    
    public static void main(String[] args) throws Exception {
        String log4jProp = System.getProperty("log4j");
        System.out.println("Initializing log4j with: "+log4jProp);
        PropertyConfigurator.configure(log4jProp);

        LogManager.getLogger("com.atcloud").setLevel(Level.INFO);
        LogManager.getLogger("mx.com.unirefacciones").setLevel(Level.DEBUG);

        try {
            CatalogoBussines catalogoBussines = new CatalogoBussines();
            catalogoBussines.parseArgs(args);

            String[] companias = {
                "01"
            };
            for(String compania : companias) {
                String catalogos = Configuracion.getInstance().getProperty(compania+".aspel.catalogos");
                log.debug("Actualizando Catalogos Compañia ["+compania+"], catalogos["+catalogos+"] ...");
                catalogoBussines.actualizaCatalogos(compania);
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}