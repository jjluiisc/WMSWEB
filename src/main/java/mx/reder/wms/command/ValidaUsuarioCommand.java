package mx.reder.wms.command;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import com.atcloud.util.Reflector;
import java.util.ArrayList;
import mx.reder.wms.collection.PermisoPerfilCollection;
import mx.reder.wms.dao.entity.UsuarioDAO;
import mx.reder.wms.to.MensajeTO;
import mx.reder.wms.to.UsuarioTO;
import org.apache.log4j.Logger;

/**
 *
 * @author jbecerra
 */
public class ValidaUsuarioCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(ValidaUsuarioCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String usuario = request.getParameter("usuario");
            String password = request.getParameter("password");

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.usuario = usuario;
            if (!ds.exists(usuarioDAO))
                throw new WebException("No existe este Usuario ["+usuarioDAO+"].");

            //if (usuarioDAO.compania.compareTo(compania)!=0)
            //    throw new WebException("Este Usuario ["+usuarioDAO+"] no es de esta Compania ["+compania+"].");

            if (usuarioDAO.password.compareTo(password)!=0)
                throw new WebException("Este Usuario ["+usuarioDAO+"] tiene otra contraseña.");

            UsuarioTO usuarioTO = new UsuarioTO();

            Reflector.copyAllFields(usuarioDAO, usuarioTO);

            PermisoPerfilCollection permisoPerfilCollection = new PermisoPerfilCollection();
            permisoPerfilCollection.perfil = usuarioTO.perfil;

            ArrayList<PermisoPerfilCollection> arrayPermisos = ds.collection(permisoPerfilCollection,
                    permisoPerfilCollection.getSQL("pp.perfil = '"+usuarioTO.perfil+"'"));

            MensajeTO mensajeTO = new MensajeTO();
            mensajeTO.msg = "OK";

            try (PrintWriter out = response.getWriter()) {
                out.write("{");
                out.write("\"mensaje\": ");
                JSON.writeObject(out, mensajeTO);
                out.write(", \"usuario\": ");
                JSON.writeObject(out, usuarioTO);
                out.write(", \"permisos\": ");
                JSON.writeArrayOfObjects(out, arrayPermisos);
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