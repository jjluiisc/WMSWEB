package mx.reder.wms.cfdi.entity;

import java.math.BigDecimal;
import mx.gob.sat.sitioInternet.cfd.catalogos.CImpuesto;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoFactor;

/**
 *
 * @author joelbecerramiranda
 */
public interface PagoDRImpuestoCFD {
    public BigDecimal getBase();
    public BigDecimal getImporte();
    public CImpuesto.Enum getImpuesto();
    public BigDecimal getTasaOCuota();
    public CTipoFactor.Enum getTipoFactor();
}
