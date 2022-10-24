package mx.reder.wms.cfdi.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitioInternet.cfd.catalogos.CObjetoImp;

public interface PagoDocumentoRelacionadoCFD {
    public String getIdDocumento();
    public String getSerie();
    public String getFolio();
    public CMoneda.Enum getMoneda();
    public BigDecimal getEquivalencia();
    public CMetodoPago.Enum getMetodoDePago();
    public BigInteger getNumParcialidad();
    public BigDecimal getImpSaldoAnt();
    public BigDecimal getImpPagado();
    public BigDecimal getImpSaldoInsoluto();
    public CObjetoImp.Enum getObjetoImp();
    public List<PagoDRImpuestoCFD> getImpuestosTrasladados();
}
