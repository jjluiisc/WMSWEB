package mx.reder.wms.command;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import java.sql.Connection;
import java.util.ArrayList;
import mx.reder.wms.collection.ASPELProductosCollection;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author jbecerra
 */
public class BusquedaProductosCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(BusquedaProductosCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        DatabaseDataSource databaseDataSourceAspel = null;
        Connection connectionAspel = null; 
        DatabaseServices dsAspel = null;
        try {            
            String compania = request.getParameter("compania");
            if (compania==null)
                throw new WebException("Debe de especificar la Compania");
            String usuario = request.getParameter("usuario");
            if (usuario==null)
                throw new WebException("Debe de especificar el Usuario");
            String codigo = request.getParameter("codigo");
            if (codigo==null)
                throw new WebException("Debe de especificar el Codigo");

            ASPELProductosCollection aspelProductosCollection = new ASPELProductosCollection();
            aspelProductosCollection.setEmpresa(compania);
            
            String where = "i.CVE_ART = '"+codigo+"'";
            databaseDataSourceAspel = new DatabaseDataSource("REDER");
            connectionAspel = databaseDataSourceAspel.getConnection();
            dsAspel = new DatabaseServices(connectionAspel);
            
            ArrayList array = dsAspel.collection(aspelProductosCollection, aspelProductosCollection.getSQL(where));
            
            connectionAspel.close();
            databaseDataSourceAspel.close();
            
            try (PrintWriter out = response.getWriter()) {
                JSON.writeArrayOfObjects(out, array);
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