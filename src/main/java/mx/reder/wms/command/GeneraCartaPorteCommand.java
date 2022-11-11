package mx.reder.wms.command;

import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Fecha;
import com.atcloud.util.Numero;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.business.CartaPorteBusiness;
import mx.reder.wms.dao.engine.DatabaseDataSource;
import mx.reder.wms.dao.entity.CartaPorteCfdiDAO;
import mx.reder.wms.to.MensajeTO;
import mx.reder.wms.to.RutaFacturaCartaPorteTO;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author joelbecerramiranda
 */
public class GeneraCartaPorteCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(GeneraCartaPorteCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");
            String transporte = request.getParameter("transporte");
            String rows = request.getParameter("rows");

            log.debug(transporte);
            JSONObject jsonTransporte = (JSONObject)JSONValue.parse(transporte);

            log.debug(rows);
            JSONArray jsonRows = (JSONArray)JSONValue.parse(rows);

            DatabaseDataSource databaseDataSourceAspel = new DatabaseDataSource("REDER");
            Connection connectionAspel = databaseDataSourceAspel.getConnection();
            DatabaseServices dsAspel = new DatabaseServices(connectionAspel);

            //
            // Detalles Confirmacion
            //
            MensajeTO mensajeTO = new MensajeTO();

            try {
                CartaPorteBusiness bussines = new CartaPorteBusiness();
                bussines.setDatabaseServices(ds);
                bussines.setDatabaseAspelServices(dsAspel);

                ArrayList<RutaFacturaCartaPorteTO> facturas = new ArrayList<>();
                for (int i=0; i<jsonRows.size(); i++) {
                    JSONObject jsonFactura = (JSONObject)jsonRows.get(i);

                    RutaFacturaCartaPorteTO factura = new RutaFacturaCartaPorteTO();
                    factura.compania = (String)jsonFactura.get("compania");
                    factura.flsurtido = Numero.getIntFromString(jsonFactura.get("flsurtido").toString());
                    factura.fechahorallegada = Fecha.getFechaHora((String)jsonFactura.get("fechallegada"));
                    factura.distanciarecorrida = Numero.getIntFromString(jsonFactura.get("distancia").toString());
                    facturas.add(factura);
                }

                CartaPorteCfdiDAO cartaPorteCfdiDAO =
                        bussines.cartaPorte(compania, usuario,
                                (String)jsonTransporte.get("figuratransporte_clave"), (String)jsonTransporte.get("autotransporte_clave"), facturas);
                File fileCP = bussines.generaPDF(cartaPorteCfdiDAO);

                mensajeTO.msg = "OK";
                mensajeTO.wrn = fileCP.getAbsolutePath().replaceAll("\\\\", "/");

            } catch(Exception e) {
                log.error(e.getMessage(), e);

                throw e;
            } finally {
                connectionAspel.close();
                databaseDataSourceAspel.close();
            }

            try (PrintWriter out = response.getWriter()) {
                JSON.writeObject(out, mensajeTO);
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
