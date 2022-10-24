package mx.reder.wms.cfdi.entity;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.CFormaPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;

public interface PagoCFD {
    public Calendar getFechaAplicacion();
    public CFormaPago.Enum getFormaDePagoP();
    public CMoneda.Enum getMoneda();
    public BigDecimal getTipoDeCambio();
    public BigDecimal getMonto();
    public String getNumOperacion();
    public String getRfcEmisorCtaBen();
    public String getCtaBeneficiario();
    public List<PagoDocumentoRelacionadoCFD> getDocumentosRelacionados();
}
