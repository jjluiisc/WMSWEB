package mx.reder.wms.cfdi.imp;

import com.atcloud.util.Fecha;
import java.util.Calendar;
import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.CFormaPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitioInternet.cfd.catalogos.CRegimenFiscal;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoDeComprobante;
import mx.gob.sat.sitioInternet.cfd.catalogos.CUsoCFDI;
import mx.reder.wms.cfdi.entity.CartaPorteCFD;
import mx.reder.wms.cfdi.entity.ComprobanteCFD;
import mx.reder.wms.cfdi.entity.ComprobantePagosCFD;
import mx.reder.wms.cfdi.entity.DireccionCFD;
import mx.reder.wms.cfdi.entity.DocumentoCFD;
import mx.reder.wms.cfdi.entity.EmisorCFD;
import mx.reder.wms.cfdi.entity.EntregarEnCFD;
import mx.reder.wms.cfdi.entity.ReceptorCFD;
import java.math.BigDecimal;
import mx.gob.sat.sitioInternet.cfd.catalogos.CExportacion;

/**
 *
 * @author joelbecerramiranda
 */
public class ComprobanteCFDImp implements ComprobanteCFD {
    private EmisorCFD emisorCFD;
    private DireccionCFD direccionCFD;
    private DocumentoCFD documentoCFD;
    private ReceptorCFD receptorCFD;
    private DireccionCFD domicilioCFD;
    private EntregarEnCFD entregarEnCFD;
    private List detalles;
    private CartaPorteCFDImp cartaPorteImp;
    private ComprobantePagosCFDImp comprobantePagosImp;

    public ComprobanteCFDImp(EmisorCFD emisorCFD, DireccionCFD direccionCFD, DocumentoCFD documentoCFD,
            ReceptorCFD receptorCFD, DireccionCFD domicilioCFD, EntregarEnCFD entregarEnCFD,
            List detalles, CartaPorteCFDImp cartaPorteImp, ComprobantePagosCFDImp comprobantePagosImp) {
        this.emisorCFD = emisorCFD;
        this.direccionCFD = direccionCFD;
        this.documentoCFD = documentoCFD;
        this.receptorCFD = receptorCFD;
        this.domicilioCFD = domicilioCFD;
        this.entregarEnCFD = entregarEnCFD;
        this.detalles = detalles;
        this.cartaPorteImp = cartaPorteImp;
        this.comprobantePagosImp = comprobantePagosImp;
    }

    @Override
    public String getSerie() {
        return documentoCFD.getSerie();
    }

    @Override
    public String getFolio() {
        return documentoCFD.getFolio();
    }

    @Override
    public Calendar getFecha() {
        return Fecha.getCalendar(documentoCFD.getFecha());
    }

    @Override
    public CFormaPago.Enum getFormaDePago() {
        return CFormaPago.Enum.forString(receptorCFD.getFormaDePago());
    }

    @Override
    public CMetodoPago.Enum getMetodoDePago() {
        return CMetodoPago.Enum.forString(receptorCFD.getMetodoDePago());
    }

    @Override
    public String getNumCtaPago() {
        return receptorCFD.getNumCtaPago();
    }

    @Override
    public CUsoCFDI.Enum getUsoCFDI() {
        return CUsoCFDI.Enum.forString(receptorCFD.getUsoCFDI());
    }

    @Override
    public CTipoDeComprobante.Enum getTipoDeComprobante() {
        return CTipoDeComprobante.Enum.forString(documentoCFD.getTipoDeComprobante());
    }

    @Override
    public CMoneda.Enum getMoneda() {
        return CMoneda.Enum.forString(documentoCFD.getMoneda());
    }

    @Override
    public BigDecimal getTipoCambio() {
        return documentoCFD.getTipoCambio();
    }

    @Override
    public String getLugarExpedicion() {
        return direccionCFD.getCodigoPostal();
    }

    @Override
    public CExportacion.Enum getExportacion() {
        return CExportacion.Enum.forString(documentoCFD.getExportacion());
    }

    @Override
    public String getCfdiRelacionados() {
        return documentoCFD.getCfdiRelacionados();
    }

    @Override
    public String getInformacionGlobal() {
        return documentoCFD.getInformacionGlobal();
    }

    @Override
    public CartaPorteCFD getCartaPorte() {
        return cartaPorteImp;
    }

    @Override
    public ComprobantePagosCFD getComprobantePagos() {
        return comprobantePagosImp;
    }

    @Override
    public EmisorCFD getEmisor() {
        return emisorCFD;
    }

    @Override
    public DireccionCFD getDireccionFiscal() {
        return direccionCFD;
    }

    @Override
    public DireccionCFD getExpedidoEn() {
        return direccionCFD;
    }

    @Override
    public CRegimenFiscal.Enum getRegimenFiscal() {
        return CRegimenFiscal.Enum.forString(emisorCFD.getRegimenFiscal());
    }

    @Override
    public ReceptorCFD getReceptor() {
        return receptorCFD;
    }

    @Override
    public DireccionCFD getDomicilio() {
        return domicilioCFD;
    }

    @Override
    public EntregarEnCFD getEntregarEn() {
        return entregarEnCFD;
    }

    @Override
    public List getConceptos() {
        return detalles;
    }

    @Override
    public List getImpuestosTrasladados() {
        return null;
    }
}
