package mx.reder.wms.cfdi.entity;

import java.util.List;
import mx.gob.sat.cartaPorte20.CartaPorteDocument.CartaPorte;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CClaveUnidadPeso;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CCveTransporte;

public interface CartaPorteCFD {
    public CartaPorte.TranspInternac.Enum getTranspInternac();
    public CartaPorte.EntradaSalidaMerc.Enum getEntradaSalida();
    public CCveTransporte.Enum getViaEntradaSalida();
    public List getUbicaciones();
    public List getMercancias();
    public CClaveUnidadPeso.Enum getUnidadPeso();
    public Double getPesoBrutoTotal();
    public Double getPesoNetoTotal();
    public CartaPorteAutotransporteCFD getAutotransporte();
    public CartaPorteFiguraTransporteCFD getFiguraTransporte();
}
