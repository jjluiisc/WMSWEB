package mx.reder.wms.cfdi.entity;

import mx.gob.sat.sitioInternet.cfd.catalogos.CPais;

public interface CartaPorteDomicilioCFD {
    public String getCalle();
    public String getNoExterior();
    public String getNoInterior();
    public String getColonia();
    public String getLocalidad();
    public String getReferencia();
    public String getMunicipio();
    public String getEstado();
    public CPais.Enum getPais();
    public String getCodigoPostal();
}
