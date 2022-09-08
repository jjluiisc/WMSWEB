package mx.reder.wms.command.app;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Numero;
import mx.reder.wms.business.InventarioBussines;
import mx.reder.wms.dao.entity.InventarioConteoDAO;
import mx.reder.wms.dao.entity.InventarioDAO;
import mx.reder.wms.util.Constantes;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author jbecerra
 */
public class AnalizaProductosInventarioCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(AnalizaProductosInventarioCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String folio = request.getParameter("folio");
            String terminal = request.getParameter("terminal");
            String detalles = request.getParameter("detalles");
            String codigos = request.getParameter("codigos");

            //
            //
            //
            InventarioBussines inventarioBussines = new InventarioBussines();
            InventarioDAO inventarioDAO = inventarioBussines.buscaInventario(ds, compania);
            if (inventarioDAO==null)
                throw new WebException("No existe Status de Inventario de la Compania ["+compania+"].");
            if (inventarioDAO.flinventario!=Numero.getIntFromString(folio))
                throw new WebException("El folio del Inventario ["+folio+"] no corresponde al Inventario de la Compania ["+inventarioDAO+"].");

            if (inventarioDAO.fase==null)
                throw new WebException("No se a realizado la Carga del Inventario ["+inventarioDAO.status+"].");
            if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_1ERCONTEO)==0
                    ||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_2DOCONTEO)==0
                    ||inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_3ERCONTEO)==0))
                throw new WebException("La fase del Inventario es incorrecta ["+inventarioDAO.fase+"].");

            //
            //
            //

            String tokensD[] = detalles.split(";");
            String tokensC[] = codigos.split(";");
            if (tokensD.length!=tokensC.length)
                throw new WebException("El numero de detalles ["+tokensD.length+"] no es igual al numero de productos ["+tokensC.length+"].");

            ArrayList array = new ArrayList();

            for (int indx=0; indx<tokensD.length; indx++) {
                String detalle = tokensD[indx];

                InventarioConteoDAO inventarioConteoDAO = new InventarioConteoDAO();
                inventarioConteoDAO.fldinventario = Numero.getIntFromString(detalle);
                if (!ds.exists(inventarioConteoDAO))
                    throw new WebException("No existe este Conteo de Inventario ["+inventarioConteoDAO+"].");

                String codigo = tokensC[indx];
                if (inventarioConteoDAO.codigo.compareTo(codigo)!=0)
                    throw new WebException("El Codigo del Conteo de Inventario ["+codigo+"] no es el mismo ["+inventarioConteoDAO.codigo+"].");

                inventarioConteoDAO.status = "AN";
                inventarioConteoDAO.terminal = terminal;

                ds.update(inventarioConteoDAO, new String[] {"status", "terminal"});
                array.add(inventarioConteoDAO);
            }

            try (PrintWriter out = response.getWriter()) {
                JSON.writeArrayOfObjects(out, array, new InventarioConteoDAO());
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
