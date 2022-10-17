package mx.reder.wms.cfdi.imp;

import java.util.ArrayList;
import java.util.List;
import mx.reder.wms.cfdi.entity.CartaPorteCFD;
import mx.reder.wms.cfdi.entity.CartaPorteFiguraTransporteCFD;
import mx.reder.wms.cfdi.entity.CartaPorteMercanciaCFD;
import mx.reder.wms.cfdi.entity.CartaPorteUbicacionCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CClaveUnidadPeso;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CCveTransporte;
import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteCFD;
import mx.gob.sat.cartaPorte20.CartaPorteDocument;

/**
 *
 * @author joelbecerramiranda
 */
public class CartaPorteCFDImp implements CartaPorteCFD {
    public String transpInternac;
    public String entradaSalidaMerc;
    public String viaEntradaSalida;
    public ArrayList<CartaPorteUbicacionCFD> ubicaciones;
    public ArrayList<CartaPorteMercanciaCFD> mercancias;
    public String unidadPeso;
    public Double pesoBrutoTotal;
    public Double pesoNetoTotal;
    public CartaPorteAutotransporteCFD autotransporte;
    public CartaPorteFiguraTransporteCFD figuraTransporte;

    @Override
    public CartaPorteDocument.CartaPorte.TranspInternac.Enum getTranspInternac() {
        return CartaPorteDocument.CartaPorte.TranspInternac.Enum.forString(transpInternac);
    }

    @Override
    public CartaPorteDocument.CartaPorte.EntradaSalidaMerc.Enum getEntradaSalida() {
        if (entradaSalidaMerc==null)
            return null;
        return CartaPorteDocument.CartaPorte.EntradaSalidaMerc.Enum.forString(entradaSalidaMerc);
    }

    @Override
    public CCveTransporte.Enum getViaEntradaSalida() {
        if (viaEntradaSalida==null)
            return null;
        return CCveTransporte.Enum.forString(viaEntradaSalida);
    }

    @Override
    public List getUbicaciones() {
        return ubicaciones;
    }

    @Override
    public List getMercancias() {
        return mercancias;
    }

    @Override
    public CClaveUnidadPeso.Enum getUnidadPeso() {
        if (unidadPeso==null)
            return null;
        return CClaveUnidadPeso.Enum.forString(unidadPeso);
    }

    @Override
    public Double getPesoBrutoTotal() {
        return pesoBrutoTotal;
    }

    @Override
    public Double getPesoNetoTotal() {
        return pesoNetoTotal;
    }

    @Override
    public CartaPorteAutotransporteCFD getAutotransporte() {
        return autotransporte;
    }

    @Override
    public CartaPorteFiguraTransporteCFD getFiguraTransporte() {
        return figuraTransporte;
    }
}
