package mx.reder.wms.cfdi.imp;

import java.util.ArrayList;
import java.util.List;
import mx.reder.wms.cfdi.entity.CartaPorteFiguraTransporteCFD;
import mx.reder.wms.cfdi.entity.CartaPorteTipoFiguraTransporteCFD;

public class CartaPorteFiguraTransporteCFDImp implements CartaPorteFiguraTransporteCFD {
    public ArrayList<CartaPorteTipoFiguraTransporteCFD> tiposFigura;

    @Override
    public List getTiposFigura() {
        return tiposFigura;
    }
}
