package mx.reder.wms.command;

import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.collection.engine.CollectionRecord;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import java.sql.Connection;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author jbecerra
 */
public class ASPELColeccionCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(ASPELColeccionCommand.class);

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        DatabaseDataSource databaseDataSourceAspel = null;
        Connection connectionAspel = null; 
        DatabaseServices dsAspel = null;
        try {
            String registro = request.getParameter("registro");
            String compania = request.getParameter("compania");
            String where = request.getParameter("where");

            log.debug("ColeccionCommand registro = ["+registro+"] where = ["+where+"]");

            CollectionRecord coll = (CollectionRecord)Class.forName(registro).newInstance();
            ((DatabaseRecordASPEL)coll).setEmpresa(compania);

            databaseDataSourceAspel = new DatabaseDataSource("REDER");
            connectionAspel = databaseDataSourceAspel.getConnection();
            dsAspel = new DatabaseServices(connectionAspel);
            
            ArrayList array = dsAspel.collection(coll, coll.getSQL(where));
            //if(array==null||array.isEmpty())
            //    throw new WebException("No se encontro ning&uacute;n registro con este criterio de b&uacute;squeda.");
            connectionAspel.close();
            databaseDataSourceAspel.close();
            
            PrintWriter out = response.getWriter();
            JSON.writeArrayOfObjects(out, array, coll);
            out.close();

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
