package mx.reder.wms.dao.engine;

import java.sql.Connection;
import java.sql.SQLException;
import mx.reder.wms.util.Configuracion;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

public class DatabaseDataSource {
    static Logger log = Logger.getLogger(DatabaseDataSource.class.getName());
    private BasicDataSource bds = null;

    public DatabaseDataSource(String compania) throws Exception {
        setup(compania);
    }

    private void setup(String compania) throws Exception {
        log.info("Configurando el Data Source ["+compania+"] ...");
        Configuracion configuracion = Configuracion.getInstance();
        bds = new BasicDataSource();
        String driver = configuracion.getProperty(compania+".driver");
        log.debug("Driver = "+driver);
        bds.setDriverClassName(driver);
        String url = configuracion.getProperty(compania+".url");
        log.debug("URL = "+url);
        bds.setUrl(url);
        String usuario = configuracion.getProperty(compania+".usuario");
        bds.setUsername(usuario);
        String password = configuracion.getProperty(compania+".password");
        bds.setPassword(password);
    }

    public Connection getConnection() throws SQLException {
        return bds.getConnection();
    }

    public void close() throws SQLException {
        log.info("Finalizando el Data Source ...");
        bds.close();
    }
}