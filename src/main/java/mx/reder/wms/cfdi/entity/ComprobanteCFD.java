package mx.reder.wms.cfdi.entity;

import java.util.Calendar;
import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.CFormaPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitioInternet.cfd.catalogos.CRegimenFiscal;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoDeComprobante;

public interface ComprobanteCFD {
    public String getSerie();
    public String getFolio();
    public Calendar getFecha();
    public CFormaPago.Enum getFormaDePago();
    public CMetodoPago.Enum getMetodoDePago();
    public String getNumCtaPago();
    public CTipoDeComprobante.Enum getTipoDeComprobante();
    public CMoneda.Enum getMoneda();
    public String getLugarExpedicion();
    public String getCfdiRelacionados();

    public EmisorCFD getEmisor();
    public DireccionCFD getDireccionFiscal();
    public DireccionCFD getExpedidoEn();
    public CRegimenFiscal.Enum getRegimenFiscal();
    public ReceptorCFD getReceptor();
    public DireccionCFD getDomicilio();
    public EntregarEnCFD getEntregarEn();
    public List getConceptos();
    public List getImpuestosTrasladados();
}
