package mx.reder.wms.cfdi.entity;

import mx.gob.sat.sitioInternet.cfd.catalogos.CUsoCFDI;

public interface ReceptorCFD {
    public String getNombre();
    public String getRfc();
    public CUsoCFDI.Enum getUsoCFDI();
}
