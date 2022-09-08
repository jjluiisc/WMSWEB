package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import java.util.Date;

/**
 *
 * @author joelbecerramiranda
 */
public class PedidoBitacoraDAO implements DatabaseRecord, java.io.Serializable {
    public Integer id = 0;
    public String compania = "";
    public String pedido = "";
    public String status = "";
    public Date fechabitacora = new Date(0);
    public String usuario = "";

    public PedidoBitacoraDAO() {
    }

    public PedidoBitacoraDAO(int id) {
        this.id = id;
    }

    @Override
    public String getTable() {
        return "PedidoBitacora";
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
        return id+";"+pedido+";"+status;
    }
}
