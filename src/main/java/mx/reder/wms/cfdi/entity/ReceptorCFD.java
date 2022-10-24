package mx.reder.wms.cfdi.entity;

import mx.gob.sat.sitioInternet.cfd.catalogos.CRegimenFiscal;

public interface ReceptorCFD {
    public String getNombre();
    public String getRfc();
    public CRegimenFiscal.Enum getRegimenFiscalReceptor();
    public String getUsoCFDI();
    public String getFormaDePago();
    public String getMetodoDePago();
    public String getNumCtaPago();
}
