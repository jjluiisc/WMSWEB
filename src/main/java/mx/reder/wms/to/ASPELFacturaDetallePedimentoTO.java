package mx.reder.wms.to;

import com.atcloud.util.Fecha;
import java.util.Calendar;
import java.util.Date;
import mx.reder.wms.cfdi.entity.InformacionAduaneraCFD;

/**
 *
 * @author joelbecerramiranda
 */
public class ASPELFacturaDetallePedimentoTO implements InformacionAduaneraCFD {
    public String CVE_DOC = null;
    public Integer NUM_PAR = null;
    public String CVE_ART = null;
    public Double CANT = null;
    public Integer E_LTPD = null;
    public Integer REG_LTPD = null;
    public Double CANTIDAD = null;
    public Double PXRS = null;
    public String LOTE;
    public String PEDIMENTO;
    public Integer CVE_ALM = null;
    public Date FCHCADUC = null;
    public Date FCHADUANA = null;

    @Override
    public String getGln() {
        return null;
    }

    @Override
    public String getNumero() {
        if (PEDIMENTO!=null) {
            PEDIMENTO = PEDIMENTO.trim();
            if (PEDIMENTO.matches("\\d*")) {
                if (PEDIMENTO.length()==15)
                    return PEDIMENTO.substring(0,2)+"  "+PEDIMENTO.substring(2,4)+"  "+PEDIMENTO.substring(4,8)+"  "+PEDIMENTO.substring(8,15);
            } else {
                String[] tokens = PEDIMENTO.split(" ");
                if (tokens.length==4)
                    return tokens[0]+"  "+tokens[1]+"  "+tokens[2]+"  "+tokens[3];
            }
        }
        return PEDIMENTO;
    }

    @Override
    public Calendar getFecha() {
        return null;
    }

    @Override
    public String getAduana() {
        return null;
    }

    @Override
    public String getLote() {
        return LOTE;
    }

    @Override
    public Calendar getFechaCaducidad() {
        if (FCHCADUC==null)
            return null;
        return Fecha.getCalendar(FCHCADUC);
    }
}
