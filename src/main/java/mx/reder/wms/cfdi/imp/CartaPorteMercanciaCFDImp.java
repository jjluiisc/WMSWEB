package mx.reder.wms.cfdi.imp;

import mx.reder.wms.cfdi.entity.CartaPorteMercanciaCFD;
import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.CClaveUnidad;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CTipoEmbalaje;

public class CartaPorteMercanciaCFDImp implements CartaPorteMercanciaCFD {
    public String bienesTransp;
    public String claveSTCC;
    public String descripcion;
    public double cantidad;
    public String claveUnidad;
    public String unidad;
    public String dimensiones;
    public String materialPeligroso;
    public String cveMaterialPeligroso;
    public String embalaje;
    public String descripEmbalaje;
    public double pesoEnKg;
    public Double valorMercancia;
    public String moneda;
    public String fraccionArancelaria;
    public String uuidComercioExt;
    public List cantidadTransporta;

    @Override
    public String getBienesTransp() {
        return bienesTransp;
    }

    @Override
    public String getClaveSTCC() {
        return claveSTCC;
    }

    @Override
    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public double getCantidad() {
        return cantidad;
    }

    @Override
    public CClaveUnidad.Enum getClaveUnidad() {
        if (claveUnidad==null)
            return null;
        return CClaveUnidad.Enum.forString(claveUnidad);
    }

    @Override
    public String getUnidad() {
        return unidad;
    }

    @Override
    public String getDimensiones() {
        return dimensiones;
    }

    @Override
    public String getMaterialPeligroso() {
        return materialPeligroso;
    }

    @Override
    public String getCveMaterialPeligroso() {
        return cveMaterialPeligroso;
    }

    @Override
    public CTipoEmbalaje.Enum getEmbalaje() {
        if (embalaje==null)
            return null;
        return CTipoEmbalaje.Enum.forString(embalaje);
    }

    @Override
    public String getDescripEmbalaje() {
        return descripEmbalaje;
    }

    @Override
    public double getPesoEnKg() {
        return pesoEnKg;
    }

    @Override
    public Double getValorMercancia() {
        return valorMercancia;
    }

    @Override
    public CMoneda.Enum getMoneda() {
        return CMoneda.Enum.forString(moneda);
    }

    @Override
    public String getFraccionArancelaria() {
        return fraccionArancelaria;
    }

    @Override
    public String getUUIDComercioExt() {
        return uuidComercioExt;
    }

    @Override
    public List getCantidadTransporta() {
        return cantidadTransporta;
    }
}
