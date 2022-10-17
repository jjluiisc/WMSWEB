package mx.reder.wms.cfdi.entity;

import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.CClaveUnidad;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CTipoEmbalaje;

public interface CartaPorteMercanciaCFD {
    public String getBienesTransp();
    public String getClaveSTCC();
    public String getDescripcion();
    public double getCantidad();
    public CClaveUnidad.Enum getClaveUnidad();
    public String getUnidad();
    public String getDimensiones();
    public String getMaterialPeligroso();
    public String getCveMaterialPeligroso();
    public CTipoEmbalaje.Enum getEmbalaje();
    public String getDescripEmbalaje();
    public double getPesoEnKg();
    public Double getValorMercancia();
    public CMoneda.Enum getMoneda();
    public String getFraccionArancelaria();
    public String getUUIDComercioExt();
    public List getCantidadTransporta();
}
