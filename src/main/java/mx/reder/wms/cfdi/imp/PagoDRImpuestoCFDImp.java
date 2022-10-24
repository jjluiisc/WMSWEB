package mx.reder.wms.cfdi.imp;

import com.atcloud.util.Numero;
import java.math.BigDecimal;
import mx.reder.wms.cfdi.entity.PagoDRImpuestoCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.CImpuesto;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoFactor;

/**
 *
 * @author joelbecerramiranda
 */
public class PagoDRImpuestoCFDImp implements PagoDRImpuestoCFD {
    public double base;
    public double importe;
    public String impuesto;
    public BigDecimal tasa;
    public String tipofactor;

    @Override
    public BigDecimal getBase() {
        return Numero.getBigDecimal(base);
    }

    @Override
    public BigDecimal getImporte() {
        return Numero.getBigDecimal(importe);
    }

    @Override
    public CImpuesto.Enum getImpuesto() {
        return CImpuesto.Enum.forString(impuesto);
    }

    @Override
    public BigDecimal getTasaOCuota() {
        return tasa;
    }

    @Override
    public CTipoFactor.Enum getTipoFactor() {
        return CTipoFactor.Enum.forString(tipofactor);
    }
}
