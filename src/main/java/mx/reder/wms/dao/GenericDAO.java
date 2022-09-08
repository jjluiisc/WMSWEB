package mx.reder.wms.dao;

import com.atcloud.dao.engine.DatabaseServices;
import mx.reder.wms.dao.entity.FolioDAO;

public class GenericDAO {

    public static int dameFolioActual(DatabaseServices ds, String compania, String tipo) throws Exception {
        FolioDAO foliosDAO = (FolioDAO)ds.first(new FolioDAO(), "compania = '"+compania+"' AND tipo = '"+tipo+"'");
        if (foliosDAO == null) {
            foliosDAO = new FolioDAO();
            foliosDAO.compania = compania;
            foliosDAO.tipo = tipo;
            foliosDAO.folio = 0;
        }
        return foliosDAO.folio;
    }

    public static int obtenerSiguienteFolio(DatabaseServices ds, String compania, String tipo) throws Exception {
        FolioDAO foliosDAO = (FolioDAO)ds.first(new FolioDAO(), "compania = '"+compania+"' AND tipo = '"+tipo+"'");
        if (foliosDAO == null) {
            foliosDAO = new FolioDAO();
            foliosDAO.compania = compania;
            foliosDAO.tipo = tipo;
            foliosDAO.folio = 1;

            ds.insert(foliosDAO);
        } else {
            foliosDAO.folio ++;

            ds.update(foliosDAO);
        }
        return foliosDAO.folio;
    }
}