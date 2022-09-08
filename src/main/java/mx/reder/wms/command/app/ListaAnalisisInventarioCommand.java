package mx.reder.wms.command.app;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import mx.reder.wms.business.InventarioBussines;
import mx.reder.wms.collection.AnalisisInventarioCollection;
import mx.reder.wms.dao.entity.InventarioConteoUbicacionDAO;
import mx.reder.wms.dao.entity.InventarioDAO;
import mx.reder.wms.util.Constantes;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author jbecerra
 */
public class ListaAnalisisInventarioCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(ListaAnalisisInventarioCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String terminal = request.getParameter("terminal");

            //
            //
            //
            InventarioBussines inventarioBussines = new InventarioBussines();
            InventarioDAO inventarioDAO = inventarioBussines.buscaInventario(ds, compania);
            if (inventarioDAO==null)
                throw new WebException("No existe Status de Inventario de la Compania ["+compania+"].");

            if (inventarioDAO.fase==null)
                throw new WebException("No se a realizado la Carga del Inventario ["+inventarioDAO.status+"].");
            if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_1ERCONTEO)==0
                    ||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_2DOCONTEO)==0
                    ||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_3ERCONTEO)==0))
                throw new WebException("La fase del Inventario es incorrecta ["+inventarioDAO.fase+"].");

            //
            //
            //
            AnalisisInventarioCollection analisisInventarioCollection = new AnalisisInventarioCollection();
            String where = "flinventario = "+inventarioDAO.flinventario+" AND terminal = '"+terminal+"'";

            ArrayList array = ds.collection(analisisInventarioCollection, analisisInventarioCollection.getSQL(where));

            StringBuilder whereU = new StringBuilder();
            whereU.append("flinventario = ").append(inventarioDAO.flinventario).append(" ");
            whereU.append("AND fldinventario IN (");
            for(Object object : array) {
                analisisInventarioCollection = (AnalisisInventarioCollection)object;

                whereU.append(analisisInventarioCollection.fldinventario).append(",");
            }
            whereU.deleteCharAt(whereU.length() - 1);
            whereU.append(")");

            InventarioConteoUbicacionDAO inventarioConteoUbicacionDAO = new InventarioConteoUbicacionDAO();
            ArrayList arrayU = array.isEmpty() ? new ArrayList() :
                    ds.select(inventarioConteoUbicacionDAO, whereU.toString());

            try (PrintWriter out = response.getWriter()) {
                out.write("{");
                out.write("\"analisis\": ");
                JSON.writeArrayOfObjects(out, array);
                out.write(", \"ubicaciones\": ");
                JSON.writeArrayOfObjects(out, arrayU);
                out.write("}");
            }

        } catch(WebException e) {
            log.error(e.getMessage(), e);

            ErrorTO errorTO = new ErrorTO();
            errorTO.fromException(e);

            try {
                PrintWriter out = response.getWriter();
                JSON.writeObject(out, errorTO);
                out.close();

            } catch(Exception ex) {
                throw new WebException(ex.getMessage());
            }

        } catch(Exception e) {
            log.error(e.getMessage(), e);

            ErrorTO errorTO = new ErrorTO();
            errorTO.fromException(e);

            try {
                PrintWriter out = response.getWriter();
                JSON.writeObject(out, errorTO);
                out.close();

            } catch(Exception ex) {
                throw new WebException(ex.getMessage());
            }
        }
    }
}
