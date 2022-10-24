package mx.reder.wms.cfdi.imp;

import com.atcloud.util.Numero;
import mx.gob.sat.sitioInternet.cfd.catalogos.CImpuesto;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoFactor;

/**
 *
 * @author joelbecerramiranda
 */
public class ImpuestoAcumulado {
    public double base;
    public double importe;
    public CImpuesto.Enum impuesto;
    public double tasa;
    public CTipoFactor.Enum tipofactor;
    public double tipoCambio;

    public String getKey() {
        return impuesto.toString()+";"+tipofactor.toString()+";"+Numero.redondea2(tasa);
    }

    public void acumula(ImpuestoAcumulado impuestoAcumulado) {
        this.base = Numero.redondea(this.base + impuestoAcumulado.base);
        this.importe = Numero.redondea(this.importe + impuestoAcumulado.importe);
    }
}
