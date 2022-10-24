package mx.reder.wms.cfdi.entity;

import mx.gob.sat.sitioInternet.cfd.catalogos.CClaveUnidad;
import mx.gob.sat.sitioInternet.cfd.catalogos.CObjetoImp;

public interface ConceptoCFD {
    public double getCantidad();
    public String getUnidad();
    public String getClaveProductoServicio();
    public CClaveUnidad.Enum getClaveUnidad();
    public String getDescripcion();
    public String getNoIdentificacion();
    public String getEAN();
    public String getSKU();
    public double getValorUnitario();
    public double getPrIesps();
    public double getIesps();
    public double getImporte();
    public double getPrDescuento();
    public double getDescuento();
    public CObjetoImp.Enum getObjetoImp();
    public double getPrIva();
    public double getIva();
    public double getTotal();
    public InformacionAduaneraCFD getInformacionAduanera();
}
