package mx.reder.wms.command;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebCommandInterface;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import mx.reder.wms.to.MensajeTO;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.DailyRollingFileAppender;

/**
 *
 * @author jbecerra
 */
public class VerLogCommand implements WebCommandInterface {
    static Logger log = Logger.getLogger(VerLogCommand.class.getName());

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, DatabaseServices ds) throws WebException {
        try {
            String compania = request.getParameter("compania");
            String usuario = request.getParameter("usuario");

            MensajeTO mensajeTO = new MensajeTO();

            String fileLog = null;
            Enumeration appenders = Logger.getRootLogger().getAllAppenders();

            while (appenders.hasMoreElements()) {
                Appender appender = (Appender)appenders.nextElement();
                log.debug("appender name ["+appender.getName()+"] "+appender.getClass());
                if (appender instanceof DailyRollingFileAppender) {
                    fileLog = ((DailyRollingFileAppender)appender).getFile();
                    log.debug("fileLog ["+fileLog+"]");
                    fileLog = fileLog.replaceAll("\\\\", "/");
                    log.debug("fileLog ["+fileLog+"]");
                }
            }

            try {
                if (fileLog!=null) {
                    File file = new File(fileLog);
                    long read = 1024 * 16 * 2;
                    long pos = file.length() - read;
                    if (pos < 0l)
                        pos = 0l;

                    RandomAccessFile raf = new RandomAccessFile(file, "r");
                    raf.seek(pos);

                    String line = null;
                    StringBuilder content = new StringBuilder();
                    while((line=raf.readLine())!=null)
                        content.append(line).append("\n");

                    raf.close();

                    mensajeTO.msg = content.toString().replaceAll("\\\\", "/");
                }

            } catch(Exception e) {
                throw new WebException(e.getMessage());
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