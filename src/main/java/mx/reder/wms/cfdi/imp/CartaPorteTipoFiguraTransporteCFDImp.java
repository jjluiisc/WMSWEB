package mx.reder.wms.cfdi.imp;

import mx.reder.wms.cfdi.entity.CartaPorteDomicilioCFD;
import mx.reder.wms.cfdi.entity.CartaPorteTipoFiguraTransporteCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.CPais;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CFiguraTransporte;

public class CartaPorteTipoFiguraTransporteCFDImp implements CartaPorteTipoFiguraTransporteCFD {
    public String tipoFigura;
    public String rfcFigura;
    public String numLicencia;
    public String nombreFigura;
    public String numRegIdTribFigura;
    public String pais;
    public CartaPorteDomicilioCFDImp domicilio;

    @Override
    public CFiguraTransporte.Enum getTipoFigura() {
       if (tipoFigura==null)
           return null;
        return CFiguraTransporte.Enum.forString(tipoFigura);
    }

    @Override
    public String getRFCFigura() {
        return rfcFigura;
    }

    @Override
    public String getNumLicencia() {
        return numLicencia;
    }

    @Override
    public String getNombreFigura() {
        return nombreFigura;
    }

    @Override
    public String getNumRegIdTribFigura() {
        return numRegIdTribFigura;
    }

    @Override
    public CPais.Enum getResidenciaFiscalFigura() {
        if (pais==null)
            return null;
        return CPais.Enum.forString(pais);
    }

    @Override
    public CartaPorteDomicilioCFD getDomicilio() {
        return domicilio;
    }
}
