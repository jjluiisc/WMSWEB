package mx.reder.wms.cfdi.imp;

import com.atcloud.util.Numero;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import mx.reder.wms.cfdi.entity.PagoCFD;
import mx.reder.wms.cfdi.entity.PagoDocumentoRelacionadoCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.CFormaPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;

/**
 *
 * @author joelbecerramiranda
 */
public class PagoCFDImp implements PagoCFD {
    public Calendar fechaAplicacion;
    public String formaDePagoP;
    public String moneda;
    public String tipoDeCambio;
    public String monto;
    public String numOperacion;
    public String rfcEmisorCtaBen;
    public String ctaBeneficiario;

    @Override
    public Calendar getFechaAplicacion() {
        return fechaAplicacion;
    }

    @Override
    public CFormaPago.Enum getFormaDePagoP() {
        return CFormaPago.Enum.forString(formaDePagoP);
    }

    @Override
    public CMoneda.Enum getMoneda() {
        return CMoneda.Enum.forString(moneda);
    }

    @Override
    public BigDecimal getTipoDeCambio() {
        return Numero.getBigDecimal(Numero.getDoubleFromString(tipoDeCambio), 6);
    }

    @Override
    public BigDecimal getMonto() {
        return Numero.getBigDecimal(monto);
    }

    @Override
    public String getNumOperacion() {
        return numOperacion;
    }

    @Override
    public String getRfcEmisorCtaBen() {
        return rfcEmisorCtaBen;
    }

    @Override
    public String getCtaBeneficiario() {
        return ctaBeneficiario;
    }

    private List<PagoDocumentoRelacionadoCFD> documentosRelacionados;

    public void setDocumentosRelacionados(List documentosRelacionados) {
        this.documentosRelacionados = documentosRelacionados;
    }

    @Override
    public List<PagoDocumentoRelacionadoCFD> getDocumentosRelacionados() {
        return documentosRelacionados;
    }
}
