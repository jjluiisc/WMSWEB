package mx.reder.wms.cfdi.imp;

import com.atcloud.util.Numero;
import mx.reder.wms.cfdi.entity.ConceptoCFD;
import mx.reder.wms.cfdi.entity.InformacionAduaneraCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.CClaveUnidad;
import mx.gob.sat.sitioInternet.cfd.catalogos.CObjetoImp;

/**
 *
 * @author joelbecerramiranda
 */
public class ConceptoImp implements ConceptoCFD {
    public String sku = null;
    public String ean = null;
    public String claveProductoServicio = null;
    public String claveUnidad = null;
    public String descripcion = null;
    public String unidadMedida = null;
    public Double cantidad = null;
    public Double precio = null;
    public Double importe = null;
    public Double prIesps = null;
    public Double prIva = null;
    public Double totIesps = null;
    public Double totIva = null;
    public Double prDescuento = null;
    public Double descuento = null;

    @Override
    public double getCantidad() {
        return cantidad;
    }

    @Override
    public String getUnidad() {
        return claveUnidad;
    }

    @Override
    public String getClaveProductoServicio() {
        return claveProductoServicio;
    }

    @Override
    public CClaveUnidad.Enum getClaveUnidad() {
        if (unidadMedida==null)
            return CClaveUnidad.PZ;
        CClaveUnidad.Enum claveunidad = CClaveUnidad.Enum.forString(unidadMedida.toUpperCase());
        return claveunidad!=null ? claveunidad: CClaveUnidad.PZ;
    }

    @Override
    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String getNoIdentificacion() {
        return sku;
    }

    @Override
    public String getEAN() {
        return ean;
    }

    @Override
    public String getSKU() {
        return sku;
    }

    @Override
    public double getValorUnitario() {
        return precio;
    }

    @Override
    public double getPrIesps() {
        return Numero.redondea2(prIesps / 100.0);
    }

    @Override
    public double getIesps() {
        return totIesps;
    }

    @Override
    public double getImporte() {
        return importe;
    }

    @Override
    public double getPrDescuento() {
        return Numero.redondea2(prDescuento / 100.0);
    }

    @Override
    public double getDescuento() {
        return descuento;
    }

    @Override
    public CObjetoImp.Enum getObjetoImp() {
        double _prIva = getPrIva();
        if (_prIva==0.0) {
            return CObjetoImp.X_01;
        }
        // Cualquier valor distinto de cero, se asume que el objeto de impuesto es X_02
        return CObjetoImp.X_02;
    }

    @Override
    public double getPrIva() {
        return Numero.redondea2(prIva / 100.0);
    }

    @Override
    public double getIva() {
        return totIva;
    }

    @Override
    public double getTotal() {
        return importe - descuento + totIva;
    }

    @Override
    public InformacionAduaneraCFD getInformacionAduanera() {
        return null;
    }
}
