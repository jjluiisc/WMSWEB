package mx.reder.wms.cfdi.imp;

import java.util.ArrayList;
import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CConfigAutotransporte;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CTipoPermiso;
import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteCFD;
import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteRemolqueCFD;
import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteSeguroCFD;

public class CartaPorteAutotransporteCFDImp implements CartaPorteAutotransporteCFD {
    public String permisoSCT;
    public String numPermisoSCT;
    public String configVehicular;
    public String placaVM;
    public int anioModeloVM;
    public ArrayList<CartaPorteAutotransporteSeguroCFD> seguros;
    public ArrayList<CartaPorteAutotransporteRemolqueCFD> remolques;

    @Override
    public CTipoPermiso.Enum getPermSCT() {
        return CTipoPermiso.Enum.forString(permisoSCT);
    }

    @Override
    public String getNumPermisoSCT() {
        return numPermisoSCT;
    }

    @Override
    public CConfigAutotransporte.Enum getConfigVehicular() {
        return CConfigAutotransporte.Enum.forString(configVehicular);
    }

    @Override
    public String getPlacaVM() {
        return placaVM;
    }

    @Override
    public int getAnioModeloVM() {
        return anioModeloVM;
    }

    @Override
    public List<CartaPorteAutotransporteSeguroCFD> getSeguros() {
        return seguros;
    }

    @Override
    public List<CartaPorteAutotransporteRemolqueCFD> getRemolques() {
        return remolques;
    }
}
