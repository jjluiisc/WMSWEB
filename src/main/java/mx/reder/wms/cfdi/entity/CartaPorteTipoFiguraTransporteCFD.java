package mx.reder.wms.cfdi.entity;

import mx.gob.sat.sitioInternet.cfd.catalogos.CPais;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CFiguraTransporte;

public interface CartaPorteTipoFiguraTransporteCFD {
    public CFiguraTransporte.Enum getTipoFigura();
    public String getRFCFigura();
    public String getNumLicencia();
    public String getNombreFigura();
    public String getNumRegIdTribFigura();
    public CPais.Enum getResidenciaFiscalFigura();
    public CartaPorteDomicilioCFD getDomicilio();
}
