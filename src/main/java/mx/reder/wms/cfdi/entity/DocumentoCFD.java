package mx.reder.wms.cfdi.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author joelbecerramiranda
 */
public interface DocumentoCFD {
    public String getSerie();
    public String getFolio();
    public Date getFecha();
    public String getTipoDeComprobante();
    public String getMoneda();
    public BigDecimal getTipoCambio();
    public String getExportacion();
    public String getCfdiRelacionados();
    public String getInformacionGlobal();
}
