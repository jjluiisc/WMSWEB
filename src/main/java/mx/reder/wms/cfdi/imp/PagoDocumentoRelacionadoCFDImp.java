package mx.reder.wms.cfdi.imp;

import com.atcloud.util.Numero;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import mx.reder.wms.cfdi.entity.PagoDRImpuestoCFD;
import mx.reder.wms.cfdi.entity.PagoDocumentoRelacionadoCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitioInternet.cfd.catalogos.CObjetoImp;

/**
 *
 * @author joelbecerramiranda
 */
public class PagoDocumentoRelacionadoCFDImp implements PagoDocumentoRelacionadoCFD {
    public String idDocumento;
    public String serie;
    public String folio;
    public String moneda;
    public String equivalencia;
    public String metodoDePago;
    public String numParcialidad;
    public String impSaldoAnt;
    public String impPagado;
    public String impSaldoInsoluto;
    public String objetoImp;

    @Override
    public String getIdDocumento() {
        return idDocumento;
    }

    @Override
    public String getSerie() {
        return serie;
    }

    @Override
    public String getFolio() {
        return folio;
    }

    @Override
    public CMoneda.Enum getMoneda() {
        return CMoneda.Enum.forString(moneda);
    }

    @Override
    public BigDecimal getEquivalencia() {
        return Numero.getBigDecimal(Numero.getDoubleFromString(equivalencia), 6);
    }

    @Override
    public CMetodoPago.Enum getMetodoDePago() {
        return CMetodoPago.Enum.forString(metodoDePago);
    }

    @Override
    public BigInteger getNumParcialidad() {
        if (numParcialidad==null)
            return null;
        return Numero.getBigInteger(numParcialidad);
    }

    @Override
    public BigDecimal getImpSaldoAnt() {
        if (impSaldoAnt==null)
            return null;
        return Numero.getBigDecimal(impSaldoAnt);
    }

    @Override
    public BigDecimal getImpPagado() {
        if (impPagado==null)
            return null;
        return Numero.getBigDecimal(impPagado);
    }

    @Override
    public BigDecimal getImpSaldoInsoluto() {
        if (impSaldoInsoluto==null)
            return null;
        return Numero.getBigDecimal(impSaldoInsoluto);
    }

    @Override
    public CObjetoImp.Enum getObjetoImp() {
        return CObjetoImp.Enum.forString(objetoImp);
    }

    private List<PagoDRImpuestoCFD> impuestosTrasladados;

    public void setImpuestosTrasladados(List<PagoDRImpuestoCFD> impuestosTrasladados) {
        this.impuestosTrasladados = impuestosTrasladados;
    }

    @Override
    public List<PagoDRImpuestoCFD> getImpuestosTrasladados() {
        return impuestosTrasladados;
    }
}
