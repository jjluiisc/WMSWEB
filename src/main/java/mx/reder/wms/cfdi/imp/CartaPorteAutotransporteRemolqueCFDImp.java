package mx.reder.wms.cfdi.imp;

import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteRemolqueCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CSubTipoRem;

public class CartaPorteAutotransporteRemolqueCFDImp implements CartaPorteAutotransporteRemolqueCFD {
    public String placa;
    public String subTipoRem;

    @Override
    public String getPlaca() {
        return placa;
    }

    @Override
    public CSubTipoRem.Enum getSubTipoRem() {
        if (subTipoRem==null)
            return null;
        return CSubTipoRem.Enum.forString(subTipoRem);
    }
}
