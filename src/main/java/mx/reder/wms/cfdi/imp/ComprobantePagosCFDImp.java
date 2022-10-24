package mx.reder.wms.cfdi.imp;

import java.util.ArrayList;
import mx.reder.wms.cfdi.entity.ComprobantePagosCFD;
import mx.reder.wms.cfdi.entity.PagoCFD;

/**
 *
 * @author joelbecerramiranda
 */
public class ComprobantePagosCFDImp implements ComprobantePagosCFD {
    private ArrayList<PagoCFD> pagos;

    public ComprobantePagosCFDImp() {
        pagos = new ArrayList<>();
    }

    public void setPagos(ArrayList<PagoCFD> pagos) {
        this.pagos = pagos;
    }

    @Override
    public ArrayList<PagoCFD> getPagos() {
        return pagos;
    }
}
