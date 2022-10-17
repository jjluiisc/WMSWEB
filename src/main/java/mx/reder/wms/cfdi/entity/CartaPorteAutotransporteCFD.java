package mx.reder.wms.cfdi.entity;

import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CConfigAutotransporte;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CTipoPermiso;

public interface CartaPorteAutotransporteCFD {
    public CTipoPermiso.Enum getPermSCT();
    public String getNumPermisoSCT();
    public CConfigAutotransporte.Enum getConfigVehicular();
    public String getPlacaVM();
    public int getAnioModeloVM();
    public List<CartaPorteAutotransporteSeguroCFD> getSeguros();
    public List<CartaPorteAutotransporteRemolqueCFD> getRemolques();
}
