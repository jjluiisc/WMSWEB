package mx.reder.wms.cfdi.imp;

import mx.reder.wms.cfdi.entity.DireccionCFD;
import mx.reder.wms.cfdi.entity.EntregarEnCFD;
import mx.reder.wms.cfdi.entity.ReceptorCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.CRegimenFiscal;

/**
 *
 * @author joelbecerramiranda
 */
public class ReceptorImp implements ReceptorCFD, DireccionCFD, EntregarEnCFD {
    public String nombre;
    public String rfc;
    public String regimenFiscal;
    public String usoCFDI;
    public String formaDePago;
    public String metodoDePago;
    public String numCtaPago;
    public String calle;
    public String noExterior;
    public String noInterior;
    public String colonia;
    public String municipio;
    public String estado;
    public String pais;
    public String codigoPostal;
    public String localidad;
    public String referencia;

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public String getRfc() {
        return rfc;
    }

    @Override
    public CRegimenFiscal.Enum getRegimenFiscalReceptor() {
        return CRegimenFiscal.Enum.forString(regimenFiscal);
    }

    @Override
    public String getUsoCFDI() {
        return usoCFDI;
    }

    @Override
    public String getFormaDePago() {
        return formaDePago;
    }

    @Override
    public String getMetodoDePago() {
        return metodoDePago;
    }

    @Override
    public String getNumCtaPago() {
        return numCtaPago;
    }

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
    public String getMunicipio() {
        return municipio;
    }

    @Override
    public String getEstado() {
        return estado;
    }

    @Override
    public String getPais() {
        return pais;
    }

    @Override
    public String getCodigoPostal() {
        return codigoPostal;
    }
}
