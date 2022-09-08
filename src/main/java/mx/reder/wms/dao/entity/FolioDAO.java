package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;

public class FolioDAO implements DatabaseRecord {
    public String compania = "";
    public String tipo = "";
    public int folio = 0;

    public FolioDAO() {
    }

    public FolioDAO(String compania, String tipo) {
        this.compania = compania;
        this.tipo = tipo;
    }

    @Override
    public String getTable() {
        return "Folio";
    }

    @Override
    public String getOrder() {
        return "compania, tipo";
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND tipo = '"+tipo+"'";
    }

    @Override
    public String toString() {
        return compania+";"+tipo;
    }
}
