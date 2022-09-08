package mx.reder.wms.business;

import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Reflector;
import com.atcloud.web.WebException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import mx.reder.wms.dao.GenericDAO;
import mx.reder.wms.dao.entity.InventarioCapturaDAO;
import mx.reder.wms.dao.entity.InventarioCapturaDetalleDAO;
import mx.reder.wms.dao.entity.InventarioConteoDAO;
import mx.reder.wms.dao.entity.InventarioConteoUbicacionDAO;
import mx.reder.wms.dao.entity.InventarioDAO;
import mx.reder.wms.util.Constantes;
import org.apache.log4j.Logger;

public class InventarioCapturaBussines {
    static Logger log = Logger.getLogger(InventarioCapturaBussines.class.getName());

    public InventarioCapturaDAO buscaInventarioCaptura(DatabaseServices ds, String compania, int flinventario) throws Exception {
        ArrayList<InventarioCapturaDAO> array = ds.select(new InventarioCapturaDAO(),
                "compania = '"+compania+"' AND flinventario = "+flinventario+" AND status = '"+Constantes.ESTADO_PENDIENTE+"'", "flinventario DESC");
        if (array.isEmpty())
            return null;
        if (array.size()>1)
            throw new WebException("Existe mas de una Captura de Inventario PE de la Compania ["+compania+"].");

        return (InventarioCapturaDAO)array.get(0);
    }

    public InventarioCapturaDAO creaInventarioCaptura(DatabaseServices ds, String compania, int flinventario, String usuario, String terminal) throws Exception {
        ArrayList<InventarioCapturaDAO> array = ds.select(new InventarioCapturaDAO(),
                "compania = '"+compania+"' AND flinventario = "+flinventario+" AND status = '"+Constantes.ESTADO_PENDIENTE+"'", "flinventario DESC");
        if (!array.isEmpty())
            throw new WebException("Ya existe una Captura de Inventario [PE] en esta Compania.");

        InventarioDAO inventarioDAO = new InventarioDAO(compania, flinventario);
        if (!ds.exists(inventarioDAO))
            throw new WebException("No existe este Inventario "+inventarioDAO+".");

        int folio = GenericDAO.obtenerSiguienteFolio(ds, compania, Constantes.FOLIO_CAPTURA_INVENTARIO);

        InventarioCapturaDAO inventarioCapturaDAO = new InventarioCapturaDAO();
        inventarioCapturaDAO.compania = compania;
        inventarioCapturaDAO.flcapturainventario = folio;
        inventarioCapturaDAO.flinventario = flinventario;
        inventarioCapturaDAO.fecreacion = new Date();
        inventarioCapturaDAO.status = Constantes.ESTADO_PENDIENTE;
        inventarioCapturaDAO.femodificacion = null;
        inventarioCapturaDAO.fetermino = null;
        inventarioCapturaDAO.usuario = usuario;
        inventarioCapturaDAO.terminal = terminal==null ? "" : terminal;
        inventarioCapturaDAO.fase = inventarioDAO.fase;

        ds.insert(inventarioCapturaDAO);

        return inventarioCapturaDAO;
    }

    public void borraInventarioCaptura(DatabaseServices ds, InventarioCapturaDAO inventarioCapturaDAO) throws Exception {
        inventarioCapturaDAO.status = Constantes.ESTADO_BORRADO;
        inventarioCapturaDAO.femodificacion = new Date();

        ds.update(inventarioCapturaDAO, new String[] {"status", "femodificacion", "usuario"});
    }

    public void terminaInventarioCaptura(DatabaseServices ds, InventarioCapturaDAO inventarioCapturaDAO) throws Exception {
        InventarioDAO inventarioDAO = new InventarioDAO(inventarioCapturaDAO.compania, inventarioCapturaDAO.flinventario);
        if (!ds.exists(inventarioDAO))
            throw new WebException("No existe este Inventario "+inventarioDAO+".");

        if (inventarioDAO.fase==null)
            throw new WebException("No se a realizado la Carga del Inventario ["+inventarioDAO.status+"].");
        if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_1ERCONTEO)==0
                ||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_2DOCONTEO)==0
                ||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_3ERCONTEO)==0))
            throw new WebException("La fase del Inventario es incorrecta ["+inventarioDAO.fase+"].");

        ArrayList array = ds.select(new InventarioCapturaDetalleDAO(),
                inventarioCapturaDAO.getWhere()+" AND status = '"+Constantes.ESTADO_PENDIENTE+"'");

        for(Object object : array) {
            InventarioCapturaDetalleDAO inventarioCapturaDetalleDAO = (InventarioCapturaDetalleDAO)object;

            InventarioConteoDAO inventarioConteoDAO = (InventarioConteoDAO)ds.first(new InventarioConteoDAO(),
                    "compania = '"+inventarioCapturaDetalleDAO.compania+"' AND flinventario = "
                            +inventarioCapturaDetalleDAO.flinventario+" AND codigo = '"+inventarioCapturaDetalleDAO.codigo+"'");
            if (inventarioConteoDAO==null)
                throw new WebException("No existe InventarioConteo ["+inventarioCapturaDetalleDAO.compania+";"+
                        +inventarioCapturaDetalleDAO.flinventario+";"+inventarioCapturaDetalleDAO.codigo+"]");

            InventarioConteoUbicacionDAO inventarioConteoUbicacionDAO = (InventarioConteoUbicacionDAO)ds.first(new InventarioConteoUbicacionDAO(),
                    "compania = '"+inventarioCapturaDetalleDAO.compania+"' AND flinventario = "
                            +inventarioCapturaDetalleDAO.flinventario+" AND codigo = '"+inventarioCapturaDetalleDAO.codigo+"' "
                            +"AND ubicacion = '"+inventarioCapturaDetalleDAO.ubicacion+"' AND lote = '"+inventarioCapturaDetalleDAO.lote+"'");

            if (inventarioConteoUbicacionDAO==null) {
                inventarioConteoUbicacionDAO = new InventarioConteoUbicacionDAO();
                inventarioConteoUbicacionDAO.flduinventario = null;

                Reflector.copyAllFields(inventarioConteoDAO, inventarioConteoUbicacionDAO);
                inventarioConteoUbicacionDAO.ubicacion = inventarioCapturaDetalleDAO.ubicacion;
                inventarioConteoUbicacionDAO.lote = inventarioCapturaDetalleDAO.lote;
                inventarioConteoUbicacionDAO.fecaducidad = inventarioCapturaDetalleDAO.fecaducidad;

                inventarioConteoUbicacionDAO.existencia1 = BigDecimal.ZERO;
                inventarioConteoUbicacionDAO.existencia2 = BigDecimal.ZERO;
                inventarioConteoUbicacionDAO.existencia3 = BigDecimal.ZERO;
                inventarioConteoUbicacionDAO.existenciac = BigDecimal.ZERO;

                ds.insert(inventarioConteoUbicacionDAO);

                inventarioConteoUbicacionDAO.flduinventario = (Integer)ds.aggregate(inventarioConteoUbicacionDAO, "MAX", "flduinventario");
            }


            //
            // El 1er Conteo Acumula
            //
            if (inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_1ERCONTEO)==0) {
                inventarioConteoDAO.existencia1 = inventarioConteoDAO.existencia1.add(inventarioCapturaDetalleDAO.cantidad);
                inventarioConteoDAO.existenciac = inventarioConteoDAO.existencia1;
                ds.update(inventarioConteoDAO, new String[] {"existencia1", "existenciac"});

                inventarioConteoUbicacionDAO.existencia1 = inventarioConteoUbicacionDAO.existencia1.add(inventarioCapturaDetalleDAO.cantidad);
                inventarioConteoUbicacionDAO.existenciac = inventarioConteoUbicacionDAO.existencia1;
                ds.update(inventarioConteoUbicacionDAO, new String[] {"existencia1", "existenciac"});
            }
            //
            // El 2do Conteo Acumula
            //
            else if (inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_2DOCONTEO)==0) {
                inventarioConteoDAO.existencia2 = inventarioConteoDAO.existencia2.add(inventarioCapturaDetalleDAO.cantidad);
                inventarioConteoDAO.existenciac = inventarioConteoDAO.existencia2;
                ds.update(inventarioConteoDAO, new String[] {"existencia2", "existenciac"});

                inventarioConteoUbicacionDAO.existencia2 = inventarioConteoUbicacionDAO.existencia2.add(inventarioCapturaDetalleDAO.cantidad);
                inventarioConteoUbicacionDAO.existenciac = inventarioConteoUbicacionDAO.existencia2;
                ds.update(inventarioConteoUbicacionDAO, new String[] {"existencia2", "existenciac"});
            }
            //
            // El 3er Conteo Acumula
            //
            else if (inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_3ERCONTEO)==0) {
                inventarioConteoDAO.existencia3 = inventarioConteoDAO.existencia3.add(inventarioCapturaDetalleDAO.cantidad);
                inventarioConteoDAO.existenciac = inventarioConteoDAO.existencia3;
                ds.update(inventarioConteoDAO, new String[] {"existencia3", "existenciac"});

                inventarioConteoUbicacionDAO.existencia3 = inventarioConteoUbicacionDAO.existencia3.add(inventarioCapturaDetalleDAO.cantidad);
                inventarioConteoUbicacionDAO.existenciac = inventarioConteoUbicacionDAO.existencia3;
                ds.update(inventarioConteoUbicacionDAO, new String[] {"existencia3", "existenciac"});
            }

            //
            //
            //

            inventarioCapturaDetalleDAO.status = Constantes.ESTADO_TERMINADO;
            ds.update(inventarioCapturaDetalleDAO, new String[] {"status"});
        }

        inventarioCapturaDAO.status = Constantes.ESTADO_TERMINADO;
        inventarioCapturaDAO.femodificacion = new Date();

        ds.update(inventarioCapturaDAO, new String[] {"status", "femodificacion", "usuario"});
    }

    public InventarioCapturaDetalleDAO agregaInventarioCapturaDetalle(DatabaseServices ds, InventarioCapturaDetalleDAO inventarioCapturaDetalleDAO) throws Exception {
        InventarioCapturaDAO inventarioCapturaDAO = new InventarioCapturaDAO(inventarioCapturaDetalleDAO.compania, inventarioCapturaDetalleDAO.flcapturainventario);
        if (!ds.exists(inventarioCapturaDAO))
            throw new WebException("No existe esta Captura de Inventario "+inventarioCapturaDAO+".");

        inventarioCapturaDetalleDAO.fldcapturainventario = null;

        ds.insert(inventarioCapturaDetalleDAO);

        inventarioCapturaDetalleDAO.fldcapturainventario = (Integer)ds.aggregate(inventarioCapturaDetalleDAO, "max", "fldcapturainventario");
        return inventarioCapturaDetalleDAO;
    }

    public void borraInventarioCapturaDetalle(DatabaseServices ds, InventarioCapturaDetalleDAO inventarioCapturaDetalleDAO) throws Exception {
        InventarioCapturaDAO inventarioCapturaDAO = new InventarioCapturaDAO(inventarioCapturaDetalleDAO.compania, inventarioCapturaDetalleDAO.flcapturainventario);
        if (!ds.exists(inventarioCapturaDAO))
            throw new WebException("No existe esta Captura de Inventario "+inventarioCapturaDAO+".");

        inventarioCapturaDetalleDAO.status = Constantes.ESTADO_BORRADO;

        ds.update(inventarioCapturaDetalleDAO, new String[] {"status"});
    }
}
