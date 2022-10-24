package mx.reder.wms.cfdi.imp;

import mx.reder.wms.cfdi.entity.CartaPorteDomicilioCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.CPais;

public class CartaPorteDomicilioCFDImp implements CartaPorteDomicilioCFD {
    public String calle;
    public String noExterior;
    public String noInterior;
    public String colonia;
    public String localidad;
    public String referencia;
    public String municipio;
    public String estado;
    public String pais;
    public String codigoPostal;

    @Override
    public String getCalle() {
        return calle;
    }

    @Override
    public String getNoExterior() {
        return noExterior;
    }

    @Override
    public String getNoInterior() {
        return noInterior;
    }

    @Override
    public String getColonia() {
        return colonia;
    }

    @Override
    public String getLocalidad() {
        return localidad;
    }

    @Override
    public String getReferencia() {
        return referencia;
    }

    @Override
    public String getMunicipio() {
        return municipio;
    }

    @Override
    public String getEstado() {
        return estado;
    }

    @Override
    public CPais.Enum getPais() {
        return CPais.Enum.forString(pais);
    }

    @Override
    public String getCodigoPostal() {
        return codigoPostal;
    }
}
