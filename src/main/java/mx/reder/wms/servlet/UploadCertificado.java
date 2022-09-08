package mx.reder.wms.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import com.atcloud.commerce.services.JSON;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.web.WebException;
import com.atcloud.to.ErrorTO;
import mx.reder.wms.cfdi.EncriptacionFacade;
import mx.reder.wms.dao.entity.CertificadoSelloDigitalDAO;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

public class UploadCertificado extends HttpServlet {
    static Logger log = Logger.getLogger(UploadCertificado.class.getName());

    public Connection getConnection() throws Exception {
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource)ic.lookup("java:comp/env/jdbc/reder");
        return ds.getConnection();
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-cache");
        
        Connection connection = getConnection();
        DatabaseServices ds = new DatabaseServices(connection);
            
        try {
            log.debug("UploadCertificado processRequest() ...");
            
            CertificadoSelloDigitalDAO daoResponse = new CertificadoSelloDigitalDAO();
            
            ds.beginTransaction();
            
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
            } else {

                // Create a factory for disk-based file items
                FileItemFactory factory = new DiskFileItemFactory();
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);

                // Parse the request
                List items = upload.parseRequest(request);

                // Process the uploaded items
                Iterator iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = (FileItem)iterator.next();

                    if (item.isFormField()) {
                        String name = item.getFieldName();
                        String value = item.getString();

                        log.debug("parameter ["+name+"] = "+value);
                        
                        if(name.equals("compania"))
                            daoResponse.compania = value;
                        else if(name.equals("password"))
                            daoResponse.password = value;

                    } else {
                        String fieldName = item.getFieldName();
                        String fileName = item.getName();
                        String contentType = item.getContentType();
                        //boolean isInMemory = item.isInMemory();
                        long sizeInBytes = item.getSize();

                        log.debug("file ["+fieldName+"] = "+fileName+" "+contentType+" len "+sizeInBytes);

                        InputStream uploadedStream = item.getInputStream();
                        byte[] data = new byte[(int)sizeInBytes];
                        uploadedStream.read(data);
                        uploadedStream.close();

                        if(fieldName.equals("archivokey"))
                            daoResponse.archivokey = data;
                        else if(fieldName.equals("archivocer"))
                            daoResponse.archivocer = data;
                        else if(fieldName.equals("archivop12"))
                            daoResponse.archivop12 = data;
                    }
                }
            }
            
            EncriptacionFacade.getInstance().validar(daoResponse);

            if (ds.update(daoResponse)==0)
                ds.insert(daoResponse);
            
            PrintWriter out = response.getWriter();
            JSON.writeObject(out, daoResponse);
            out.close();
            
            ds.commit();
            
        } catch(WebException e) {
            log.error(e.getMessage(), e);

            ErrorTO errorTO = new ErrorTO();
            errorTO.fromException(e);
            
            try {
                ds.rollback();
            } catch(SQLException SQLe) {
            }
            
            try {                
                PrintWriter out = response.getWriter();
                JSON.writeObject(out, errorTO);
                out.close();
                
            } catch(Exception ex) {
                throw new WebException(ex.getMessage());
            }
            
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            
            try {
                ds.rollback();
            } catch(SQLException SQLe) {
            }
            
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
        
        connection.close();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch(Exception e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        }        
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch(Exception e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        }        
    }
}
