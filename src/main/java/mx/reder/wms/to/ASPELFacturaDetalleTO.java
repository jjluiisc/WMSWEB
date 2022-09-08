package mx.reder.wms.to;

import com.atcloud.util.Numero;
import java.util.Date;
import mx.reder.wms.cfdi.entity.ConceptoCFD;
import mx.reder.wms.cfdi.entity.InformacionAduaneraCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.CClaveUnidad;

/**
 *
 * @author joelbecerramiranda
 */
public class ASPELFacturaDetalleTO implements ConceptoCFD {
    public String CVE_DOC = null;
    public Integer NUM_PAR = null;
    public String CVE_ART = null;
    public Double CANT = null;
    public Double PXS = null;
    public Double PREC = null;
    public Double COST = null;
    public Double IMPU1 = null;
    public Double IMPU2 = null;
    public Double IMPU3 = null;
    public Double IMPU4 = null;
    public Short IMP1APLA = null;
    public Short IMP2APLA = null;
    public Short IMP3APLA = null;
    public Short IMP4APLA = null;
    public Double TOTIMP1 = null;
    public Double TOTIMP2 = null;
    public Double TOTIMP3 = null;
    public Double TOTIMP4 = null;
    public Double DESC1 = null;
    public Double DESC2 = null;
    public Double DESC3 = null;
    public Double COMI = null;
    public Double APAR = null;
    public String ACT_INV = null;
    public Integer NUM_ALM = null;
    public String POLIT_APLI = null;
    public Double TIP_CAM = null;
    public String UNI_VENTA = null;
    public String TIPO_PROD = null;
    public Integer CVE_OBS = null;
    public Integer REG_SERIE = null;
    public Integer E_LTPD = null;
    public String TIPO_ELEM = null;
    public Integer NUM_MOV = null;
    public Double TOT_PARTIDA = null;
    public String IMPRIMIR = null;
    public String MAN_IEPS = null;
    public Integer APL_MAN_IMP = null;
    public Double CUOTA_IEPS = null;
    public String APL_MAN_IEPS = null;
    public Double MTO_PORC = null;
    public Double MTO_CUOTA = null;
    public Integer CVE_ESQ = null;
    public String DESCR_ART = null;
    public String UUID = null;
    public Date VERSION_SINC = null;

    public String CVE_PRODSERV = null;
    public String CVE_UNIDAD = null;
    public String DESCR = null;
    public String UNI_MED = null;

    @Override
    public double getCantidad() {
        return CANT;
    }

    @Override
    public String getUnidad() {
        return CVE_UNIDAD;
    }

    @Override
    public String getClaveProductoServicio() {
        return CVE_PRODSERV;
    }

    @Override
    public CClaveUnidad.Enum getClaveUnidad() {
        if (UNI_MED==null)
            return CClaveUnidad.PZ;
        CClaveUnidad.Enum claveunidad = CClaveUnidad.Enum.forString(UNI_MED.toUpperCase());
        return claveunidad!=null ? claveunidad: CClaveUnidad.PZ;
    }

    @Override
    public String getDescripcion() {
        return DESCR;
    }

    @Override
    public String getNoIdentificacion() {
        return CVE_ART;
    }

    @Override
    public String getEAN() {
        return null;
    }

    @Override
    public String getSKU() {
        return CVE_ART;
    }

    @Override
    public double getValorUnitario() {
        return PREC;
    }

    @Override
    public double getPrIesps() {
        return Numero.redondea2(IMPU1 / 100.0);
    }

    @Override
    public double getIesps() {
        return TOTIMP1;
    }

    @Override
    public double getImporte() {
        return TOT_PARTIDA;
    }

    @Override
    public double getPrDescuento() {
        return Numero.redondea2(DESC1 / 100.0);
    }

    @Override
    public double getDescuento() {
        return Numero.redondea(TOT_PARTIDA * (DESC1 / 100.0));
    }

    @Override
    public double getPrIva() {
        return Numero.redondea2(IMPU4 / 100.0);
    }

    @Override
    public double getIva() {
        return TOTIMP4;
    }

    @Override
    public double getTotal() {
        return TOT_PARTIDA - getDescuento() + TOTIMP4;
    }

    @Override
    public InformacionAduaneraCFD getInformacionAduanera() {
        return null;
    }
}
