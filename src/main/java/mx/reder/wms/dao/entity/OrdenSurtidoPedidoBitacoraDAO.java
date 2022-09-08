package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.util.Date;

/**
 *
 * @author joelbecerramiranda
 */
public class OrdenSurtidoPedidoBitacoraDAO implements DatabaseRecord, java.io.Serializable {
    public Integer id = 0;
    public String compania = "";
    public int flsurtido = 0;
    public String status = "";
    public Date fechabitacora = new Date(0);
    public String usuario = "";

    public OrdenSurtidoPedidoBitacoraDAO() {
    }

    public OrdenSurtidoPedidoBitacoraDAO(int id) {
        this.id = id;
    }

    @Override
    public String getTable() {
        return "OrdenSurtidoPedidoBitacora";
    }

    @Override
    public String getOrder() {
        return "id";
    }

    @Override
    public String getWhere() {
        return "id = "+id;
    }

    @Override
    public String toString() {
        return id+";"+flsurtido+";"+status;
    }
}
