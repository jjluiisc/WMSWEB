package mx.reder.wms.cfdi.imp;

import java.util.Date;
import mx.reder.wms.cfdi.entity.DocumentoCFD;
import java.math.BigDecimal;

/**
 *
 * @author joelbecerramiranda
 */
public class DocumentoImp implements DocumentoCFD {
    public String serie;
    public String folio;
    public Date fecha;
    public String tipoComprobante;
    public String moneda;
    public BigDecimal tipoCambio;
    public String exportacion;
    public String informacionGlobal;
    public String cfdiRelacionados;

    @Override
    public String getSerie() {
        return serie;
    }

    @Override
    public String getFolio() {
        return folio;
    }

    @Override
    public Date getFecha() {
        return fecha;
    }

    @Override
    public String getTipoDeComprobante() {
        return tipoComprobante;
    }

    @Override
    public String getMoneda() {
        return moneda;
    }

    @Override
    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    @Override
    public String getExportacion() {
        return exportacion;
    }

    @Override
    public String getCfdiRelacionados() {
        return cfdiRelacionados;
    }

    @Override
    public String getInformacionGlobal() {
        return informacionGlobal;
    }
}
