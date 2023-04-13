package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseRecordEntity;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Numero;
import com.atcloud.web.WebException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import mx.reder.wms.dao.GenericDAO;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.dao.entity.CompaniaDAO;
import mx.reder.wms.dao.entity.InventarioCapturaDAO;
import mx.reder.wms.dao.entity.InventarioConteoDAO;
import mx.reder.wms.dao.entity.InventarioDAO;
import mx.reder.wms.dao.entity.InventarioDetalleDAO;
import mx.reder.wms.dao.entity.InventarioUbicacionDAO;
import mx.reder.wms.dao.entity.LTPDDAO;
import mx.reder.wms.dao.entity.ProductoDAO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

public class InventarioBussines {
    static Logger log = Logger.getLogger(InventarioBussines.class.getName());

    public InventarioDAO buscaInventario(DatabaseServices ds, String compania) throws Exception {
        ArrayList<InventarioDAO> array = ds.select(new InventarioDAO(),
                "compania = '"+compania+"' AND status = '"+Constantes.ESTADO_PENDIENTE+"'", "flinventario DESC");
        if (array.isEmpty())
            return null;
        if (array.size()>1)
            throw new WebException("Existe mas de un Inventario PE de la Compania ["+compania+"].");

        return (InventarioDAO)array.get(0);
    }

    public InventarioDAO creaInventario(DatabaseServices ds, String compania, String descripcion) throws Exception {
        ArrayList<InventarioDAO> array = ds.select(new InventarioDAO(),
                "compania = '"+compania+"' AND status = '"+Constantes.ESTADO_PENDIENTE+"'", "flinventario DESC");
        if (!array.isEmpty())
            throw new WebException("Ya existe un Inventario [PE] en esta Compania.");

        int folio = GenericDAO.obtenerSiguienteFolio(ds, compania, Constantes.FOLIO_INVENTARIO);

        InventarioDAO inventarioDAO = new InventarioDAO();
        inventarioDAO.compania = compania;
        inventarioDAO.flinventario = folio;
        inventarioDAO.descripcion = descripcion;
        inventarioDAO.feinicio = new Date();
        inventarioDAO.fetermino = null;
        inventarioDAO.status = Constantes.ESTADO_PENDIENTE;
        inventarioDAO.fase = null;
        inventarioDAO.fefase = null;
        inventarioDAO.feprimerconteo = null;
        inventarioDAO.fesegundoconteo = null;
        inventarioDAO.fetercerconteo = null;

        ds.insert(inventarioDAO);

        return inventarioDAO;
    }

    public void cargaInventario(DatabaseServices ds, InventarioDAO inventarioDAO, String almacen, String laboratorio, String productos) throws Exception {
        // Valido el estado del inventario
        if (!(inventarioDAO.fase==null||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_CARGAINICIAL)==0))
            throw new WebException("Solo se puede hacer la carga del Inventario en la fase inicial o con el estado "+Constantes.ESTADO_INVENTARIO_CARGAINICIAL+".");

        // Actualizo el Catalogo de Productos
        CompaniaDAO companiaDAO = new CompaniaDAO();
        companiaDAO.compania = inventarioDAO.compania;
        if (!ds.exists(companiaDAO))
            throw new WebException("No existe esta compania "+companiaDAO+".");

        if (almacen==null)
            almacen = "";
        almacen = almacen.trim();
        if (laboratorio==null)
            laboratorio = "";
        laboratorio = laboratorio.trim();
        if (productos==null)
            productos = "";
        productos = productos.trim();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.CVE_ART, p.DESCR, p.UNI_MED, p.LIN_PROD, p.CTRL_ALM, p.COSTO_PROM, p.EXIST, l.CAMPLIB11 ")
            .append("FROM REDER20.dbo.INVE").append(companiaDAO.compania).append(" p ")
            .append("LEFT OUTER JOIN REDER20.dbo.MULT").append(companiaDAO.compania).append(" a ON p.CVE_ART = a.CVE_ART ")
            .append("LEFT OUTER JOIN REDER20.dbo.INVE_CLIB").append(companiaDAO.compania).append(" l ON p.CVE_ART = l.CVE_PROD ")    
            .append("WHERE 1 = 1 ");
        if (!almacen.isEmpty()) {
            sql.append("AND a.CVE_ALM = '").append(almacen).append("' ");
        }
        if (!laboratorio.isEmpty()) {            
            sql.append("AND l.CAMPLIB11 IN (");
            String[] tokensl = laboratorio.split(",");
            for(String token : tokensl) {
                if(token.isEmpty())
                    continue;
                sql.append("'").append(token).append("',");
            }
            if (tokensl.length>0)
                sql.deleteCharAt(sql.length() - 1);
            
            sql.append(") ");         
        }
        if (!productos.isEmpty()) {
            sql.append("AND p.CVE_ART IN (");
            String[] tokensl = productos.split(",");
            for(String token : tokensl) {
                if(token.isEmpty())
                    continue;
                sql.append("'").append(token).append("',");
            }
            if (tokensl.length>0)
                sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        log.debug(sql.toString());

        DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
        Connection connectionAspel = databaseDataSourceAspel.getConnection();
        DatabaseServices dsAspel = new DatabaseServices(connectionAspel);

        ArrayList<DatabaseRecordEntity> arrayExistencias = dsAspel.collection(sql.toString());

        log.debug("Son ["+arrayExistencias.size()+"] registros de Inventario");

        // Los subo al inventario
        for(DatabaseRecordEntity record : arrayExistencias) {
            String codigo = record.getString("CVE_ART").trim();
            String descripcion = getString(record.getString("DESCR")).trim();
            if (descripcion!=null&&descripcion.length()>120)
                descripcion = descripcion.substring(0,120);
            String unidadMedida = getString(record.getString("UNI_MED")).trim();
            String ubicacion = getString(record.getString("CTRL_ALM")).trim();
            //String linea = getString(record.getString("LIN_PROD")).trim();
            String campLib11 = getString(record.getString("CAMPLIB11")).trim();
            BigDecimal existencia = Numero.getBigDecimal(getDouble(record.get("EXIST")));
            BigDecimal costoPromedio = Numero.getBigDecimal(getDouble(record.get("COSTO_PROM")));

            ProductoDAO productoDAO = new ProductoDAO(inventarioDAO.compania, codigo);
            if (!ds.exists(productoDAO)) {
                productoDAO.descripcion = descripcion;
                productoDAO.unidadmedida = unidadMedida;
                productoDAO.linea = 0;
                productoDAO.categoria = 0;
                productoDAO.marca = "";
                productoDAO.capa = 0;
                productoDAO.existencia = BigDecimal.ZERO;
                productoDAO.costo = costoPromedio;
                productoDAO.modificacion = new Date();

                ds.insert(productoDAO);
            }

            boolean nuevo = false;
            InventarioDetalleDAO inventarioDetalleDAO = (InventarioDetalleDAO)ds.first(new InventarioDetalleDAO(),
                    "compania = '"+inventarioDAO.compania+"' AND flinventario = "+inventarioDAO.flinventario+" AND codigo = '"+codigo+"'");
            if (inventarioDetalleDAO==null) {
                nuevo = true;
                inventarioDetalleDAO = new InventarioDetalleDAO();
                inventarioDetalleDAO.fldinventario = null;
            }

            inventarioDetalleDAO.compania = inventarioDAO.compania;
            inventarioDetalleDAO.flinventario = inventarioDAO.flinventario;
            inventarioDetalleDAO.codigo = codigo;
            inventarioDetalleDAO.descripcion = descripcion;
            inventarioDetalleDAO.unidadmedida = unidadMedida;
            inventarioDetalleDAO.existencia = existencia;
            inventarioDetalleDAO.costo = costoPromedio;
            inventarioDetalleDAO.ubicacion = ubicacion;
            inventarioDetalleDAO.laboratorio = campLib11;

            try {
                if (nuevo) {
                    ds.insert(inventarioDetalleDAO);
                } else {
                    // Cannot update identity column 'fldinventario'.
                    String where = inventarioDetalleDAO.getWhere();
                    inventarioDetalleDAO.fldinventario = null;

                    ds.update(inventarioDetalleDAO, where);
                }
            } catch(Exception e) {
                log.error("Error al cargar el producto ["+codigo+"] en el Inventario.");
                log.error(e.getMessage(), e);
                throw e;
            }
        }
        
        ds.update("DELETE FROM LTPD WHERE compania = '"+inventarioDAO.compania+"'");
        
        StringBuilder sqlLotes = new StringBuilder();
        sqlLotes.append("SELECT REG_LTPD, CVE_ART, LOTE, PEDIMENTO, CVE_ALM, CONVERT(DECIMAL(12,6),CANTIDAD) AS CANTIDAD, FCHULTMOV, FCHCADUC  FROM REDER20.dbo.LTPD"+inventarioDAO.compania+" ")
            .append("WHERE STATUS = 'A' AND CANTIDAD > 0 OR (CANTIDAD = 0 AND FCHULTMOV > DATEADD(DAY, -60, GETDATE()))");
            
        ArrayList<DatabaseRecordEntity> arrayLotes = dsAspel.collection(sqlLotes.toString());
        
        for(DatabaseRecordEntity record : arrayLotes) {
            LTPDDAO ltpd = new LTPDDAO();
            ltpd.REG_LTPD = record.getInt("REG_LTPD");
            ltpd.compania = inventarioDAO.compania;
            ltpd.CVE_ART = record.getString("CVE_ART");
            ltpd.LOTE = record.getString("LOTE");
            ltpd.PEDIMENTO = record.getString("PEDIMENTO");
            ltpd.CVE_ALM = record.getInt("CVE_ALM");
            ltpd.FCHCADUC = record.getDate("FCHCADUC");
            ltpd.FCHULTMOV = record.getDate("FCHULTMOV");
            ltpd.CANTIDAD = record.getBigDecimal("CANTIDAD");        
            ds.insert(ltpd);
        }
        
        connectionAspel.close();
        databaseDataSourceAspel.close();

        // Parsea las ubicaciones de este Inventario
        ds.delete(new InventarioUbicacionDAO(), inventarioDAO.getWhere());

        InventarioUbicacionDAO inventarioUbicacionDAO = new InventarioUbicacionDAO();
        inventarioUbicacionDAO.compania = inventarioDAO.compania;
        inventarioUbicacionDAO.flinventario = inventarioDAO.flinventario;

        // Ubicaciones del Inventario
        ArrayList<DatabaseRecordEntity> arrayUbicaciones = ds.collection("SELECT DISTINCT ubicacion FROM InventarioDetalle WHERE "+inventarioDAO.getWhere());
        for(DatabaseRecordEntity record : arrayUbicaciones) {
            String ubicacionid = record.getString("ubicacion");
            String[] tokens = ubicacionid.split("\\s+");
            for(String ubicacion : tokens) {
                if (ubicacion.isEmpty())
                    continue;
                inventarioUbicacionDAO.ubicacion = ubicacion;
                if (!ds.exists(inventarioUbicacionDAO))
                    ds.insert(inventarioUbicacionDAO);
            }
        }

        // Cambia el status del inventario
        inventarioDAO.fase = Constantes.ESTADO_INVENTARIO_CARGAINICIAL;
        inventarioDAO.fefase = new Date();

        ds.update(inventarioDAO, new String[]{"fase", "fefase"});
    }

    public void activaPrimerConteo(DatabaseServices ds, InventarioDAO inventarioDAO) throws Exception {
        // Valido el estado del inventario
        if (inventarioDAO.fase==null)
            throw new WebException("No se ha realizado la Carga del Inventario ["+Constantes.ESTADO_INVENTARIO_CARGAINICIAL+"].");
        if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_CARGAINICIAL)==0))
            throw new WebException("Solo se puede hacer el Primer Conteo del Inventario con el estado ["+Constantes.ESTADO_INVENTARIO_CARGAINICIAL+"].");

        // Paso el 1er conteo
        ds.update("DELETE FROM InventarioConteo WHERE "+inventarioDAO.getWhere());
        ds.update("INSERT INTO InventarioConteo "
               +"SELECT fldinventario, compania, flinventario, codigo, descripcion, costo, existencia, 0, 0, 0, 0, 'PE', '', laboratorio "
               +"FROM InventarioDetalle WHERE compania = '"+inventarioDAO.compania+"' AND flinventario = "+inventarioDAO.flinventario);

        // Cambia el status del inventario
        inventarioDAO.fase = Constantes.ESTADO_INVENTARIO_1ERCONTEO;
        inventarioDAO.fefase = new Date();
        inventarioDAO.feprimerconteo = new Date();

        ds.update(inventarioDAO, new String[]{"fase", "fefase", "feprimerconteo"});
    }

    public void activaSegundoConteo(DatabaseServices ds, InventarioDAO inventarioDAO) throws Exception {
        // Valido el estado del inventario
        if (inventarioDAO.fase==null)
            throw new WebException("No se ha realizado la Carga del Inventario ["+Constantes.ESTADO_INVENTARIO_CARGAINICIAL+"].");
        if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_1ERCONTEO)==0))
            throw new WebException("Solo se puede hacer el 2do Conteo del Inventario con el estado ["+Constantes.ESTADO_INVENTARIO_1ERCONTEO+"].");

        int count = ds.count(new InventarioCapturaDAO(), inventarioDAO.getWhere()+" AND status = '"+Constantes.ESTADO_PENDIENTE+"'");
        if (count>0)
            throw new WebException("Existe(n) ("+count+") Capturas de Inventario ["+Constantes.ESTADO_PENDIENTE+"] Pendientes.");
        count = ds.count(new InventarioConteoDAO(), inventarioDAO.getWhere()+" AND status = 'AN'");
        if (count>0)
            throw new WebException("Existe(n) ("+count+") Conteo(s) de Inventario [AN] en Analisis de Diferencias.");

        // Paso el 2do conteo -- * No se debe cambiar
        // ds.update("UPDATE InventarioConteo SET existencia2 = existencia1, existenciac = existencia1 WHERE "+inventarioDAO.getWhere());
        // ds.update("UPDATE InventarioConteoUbicacion SET existencia2 = existencia1 WHERE "+inventarioDAO.getWhere());
        ds.update("UPDATE InventarioConteo SET existenciac = 0 WHERE "+inventarioDAO.getWhere());

        // Cambia el status del inventario
        inventarioDAO.fase = Constantes.ESTADO_INVENTARIO_2DOCONTEO;
        inventarioDAO.fefase = new Date();
        inventarioDAO.fesegundoconteo = new Date();

        ds.update(inventarioDAO, new String[]{"fase", "fefase", "fesegundoconteo"});
    }

    public void activaTercerConteo(DatabaseServices ds, InventarioDAO inventarioDAO) throws Exception {
        // Valido el estado del inventario
        if (inventarioDAO.fase==null)
            throw new WebException("No se ha realizado la Carga del Inventario ["+Constantes.ESTADO_INVENTARIO_CARGAINICIAL+"].");
        if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_2DOCONTEO)==0))
            throw new WebException("Solo se puede hacer el 3er Conteo del Inventario con el estado ["+Constantes.ESTADO_INVENTARIO_2DOCONTEO+"].");

        int count = ds.count(new InventarioCapturaDAO(), inventarioDAO.getWhere()+" AND status = '"+Constantes.ESTADO_PENDIENTE+"'");
        if (count>0)
            throw new WebException("Existe(n) ("+count+") Capturas de Inventario ["+Constantes.ESTADO_PENDIENTE+"] Pendientes.");
        count = ds.count(new InventarioConteoDAO(), inventarioDAO.getWhere()+" AND status = 'AN'");
        if (count>0)
            throw new WebException("Existe(n) ("+count+") Conteo(s) de Inventario [AN] en Analisis de Diferencias.");

        // Paso el 3er conteo
        // ds.update("UPDATE InventarioConteo SET existencia3 = existencia2, existenciac = existencia2 WHERE "+inventarioDAO.getWhere());
        // ds.update("UPDATE InventarioConteoUbicacion SET existencia3 = existencia2 WHERE "+inventarioDAO.getWhere());
        ds.update("UPDATE InventarioConteo SET existenciac = 0 WHERE "+inventarioDAO.getWhere());        

        // Cambia el status del inventario
        inventarioDAO.fase = Constantes.ESTADO_INVENTARIO_3ERCONTEO;
        inventarioDAO.fefase = new Date();
        inventarioDAO.fetercerconteo = new Date();

        ds.update(inventarioDAO, new String[]{"fase", "fefase", "fetercerconteo"});
    }

    public void afectaInventario(DatabaseServices ds, InventarioDAO inventarioDAO, String usuario) throws Exception {
        // Valido el estado del inventario
        if (inventarioDAO.fase==null)
            throw new WebException("No se ha realizado la Carga del Inventario ["+Constantes.ESTADO_INVENTARIO_CARGAINICIAL+"].");
        if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_3ERCONTEO)==0))
            throw new WebException("Solo se puede Finalizar el Inventario con el estado ["+Constantes.ESTADO_INVENTARIO_3ERCONTEO+"].");

        ArrayList array = ds.select(new InventarioConteoDAO(), inventarioDAO.getWhere());
        log.debug("Son ["+array.size()+"] registros de Inventario");

        boolean acumula = false; //inventarioDAO.clacumula.compareTo("1")==0;

        double mntotal = 0.0;
        for (Object object : array) {
            InventarioConteoDAO inventarioConteoDAO = (InventarioConteoDAO)object;

            ProductoDAO productoDAO = new ProductoDAO(inventarioConteoDAO.compania, inventarioConteoDAO.codigo);
            if (!ds.exists(productoDAO))
                throw new WebException("No existe este Producto ["+productoDAO+"].");

            /*
            double existenciaInicial = productoDAO.existencia;
            productoDAO.existencia = acumula ?
                productoDAO.existencia + inventarioConteoDAO.existencia3 :
                inventarioConteoDAO.existencia3;
            double ajuste = productoDAO.existencia - existenciaInicial;

            productoDAO.feultmovimiento = new Date();

            ds.update(productoDAO, new String[] {"feultmovimiento", "existencia"});

            KardexProductoDAO kardexProductoDAO = new KardexProductoDAO();
            kardexProductoDAO.compania = productoDAO.compania;
            kardexProductoDAO.codigo = productoDAO.codigo;
            kardexProductoDAO.id = System.currentTimeMillis();
            kardexProductoDAO.tipomovimiento = "AJI";
            kardexProductoDAO.femovimiento = new Date();
            kardexProductoDAO.existencia = existenciaInicial;
            kardexProductoDAO.cantidad = ajuste;
            kardexProductoDAO.existenciafinal = inventarioConteoDAO.existencia3;
            kardexProductoDAO.referencia = String.valueOf(inventarioDAO.flinventario);
            kardexProductoDAO.usuario = usuario;

            ds.insert(kardexProductoDAO);

            double totalProducto = inventarioConteoDAO.existencia3 * productoDAO.costopromedio;

            if (ajuste > 0)
                mntotal += totalProducto;
            else
                mntotal -= totalProducto;
            */
        }

        mntotal = Numero.redondea(mntotal);

        // Cambia el status del inventario
        inventarioDAO.fase = Constantes.ESTADO_INVENTARIO_AFECTADO;
        inventarioDAO.fefase = new Date();

        ds.update(inventarioDAO, new String[]{"fase", "fefase", "fetermino"});
    }

    public void finalizaInventario(DatabaseServices ds, InventarioDAO inventarioDAO, String usuario) throws Exception {
        // Valido el estado del inventario
        if (inventarioDAO.fase==null)
            throw new WebException("No se ha realizado la Carga del Inventario ["+Constantes.ESTADO_INVENTARIO_CARGAINICIAL+"].");
        if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_AFECTADO)==0))
            throw new WebException("Solo se puede Finalizar el Inventario con el estado ["+Constantes.ESTADO_INVENTARIO_AFECTADO+"].");

        // Cambia el status del inventario
        inventarioDAO.status = Constantes.ESTADO_FINALIZADO;
        inventarioDAO.fetermino = new Date();

        ds.update(inventarioDAO, new String[]{"status", "fetermino"});
    }

    private String getString(Object value) {
        if (value==null)
            return "";
        return (String)value;
    }

    private double getDouble(Object value) {
        if (value==null)
            return 0.0;
        return (Double)value;
    }
}
