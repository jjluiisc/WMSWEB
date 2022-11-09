package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.CommonServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.dao.entity.ASPELOperadorDAO;
import mx.reder.wms.to.AutotransporteTO;
import org.apache.log4j.Logger;

/**
 *
 * @author joelbecerramiranda
 */
public class AutotransportesCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(AutotransportesCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String valor = request.getParameter("valor");
            if (valor==null)
                valor = "";

            DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
            Connection connectionAspel = databaseDataSourceAspel.getConnection();
            DatabaseServices dsAspel = new DatabaseServices(connectionAspel);

            ArrayList autotransportes = new ArrayList();

            try {
                ASPELOperadorDAO aspelOperadorDAO1 = new ASPELOperadorDAO();
                aspelOperadorDAO1.setEmpresa(compania);

                CommonServices cs = new CommonServices();

                ArrayList<ASPELOperadorDAO> array = ds.select(aspelOperadorDAO1, "TIPO_FIG = 'AT' AND XML_OPE LIKE '%"+valor+"%'");
                for (ASPELOperadorDAO aspelOperadorDAO : array) {

                    AutotransporteTO autotransporteTO = new AutotransporteTO();
                    autotransporteTO.fromXML(cs, aspelOperadorDAO);

                    autotransportes.add(autotransporteTO);
                }

            } catch(Exception e) {
                log.error(e.getMessage(), e);

                throw e;
            } finally {
                connectionAspel.close();
                databaseDataSourceAspel.close();
            }

            try (PrintWriter out = response.getWriter()) {
                JSON.writeArrayOfObjects(out, autotransportes);
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
