package mx.reder.wms.reports;

import java.lang.reflect.Field;
import java.util.ArrayList;
import mx.reder.wms.dao.entity.CompaniaDAO;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class ReporteadorDS implements JRDataSource {
    public ArrayList datos = null;
    public Object detalleTO = null;
    public CompaniaDAO companiaDAO = null;
    
    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        String name = jrField.getName();

        try {
            
            Field field = detalleTO.getClass().getField(name);
            Object value = field.get(detalleTO);
            return value;
            
        } catch(NoSuchFieldException e) {
            
            try {

                Field field = CompaniaDAO.class.getField(name);
                Object value = field.get(companiaDAO);
                return value;

            } catch(NoSuchFieldException eex) {
            } catch(Exception eex) {                        
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public boolean next() throws JRException {
        if (datos.size()>0) {
            detalleTO = datos.remove(0);
            return true;
        }
        return false;
    }
}
