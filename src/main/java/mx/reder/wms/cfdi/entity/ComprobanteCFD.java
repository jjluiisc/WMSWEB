package mx.reder.wms.cfdi.entity;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.CExportacion;
import mx.gob.sat.sitioInternet.cfd.catalogos.CFormaPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitioInternet.cfd.catalogos.CRegimenFiscal;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoDeComprobante;
import mx.gob.sat.sitioInternet.cfd.catalogos.CUsoCFDI;

public interface ComprobanteCFD {
    public String getSerie();
    public String getFolio();
    public Calendar getFecha();
    public CFormaPago.Enum getFormaDePago();
    public CMetodoPago.Enum getMetodoDePago();
    public String getNumCtaPago();
    public CUsoCFDI.Enum getUsoCFDI();
    public CTipoDeComprobante.Enum getTipoDeComprobante();
    public CMoneda.Enum getMoneda();
    public BigDecimal getTipoCambio();
    public String getLugarExpedicion();
    public CExportacion.Enum getExportacion();
    public String getInformacionGlobal();
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

    public CartaPorteCFD getCartaPorte();
    public ComprobantePagosCFD getComprobantePagos();
}
