package mx.reder.wms.to;

import com.atcloud.util.CommonServices;
import java.io.Serializable;
import mx.reder.wms.dao.entity.ASPELOperadorDAO;

public class TipoFiguraTransporteTO implements Serializable {
    public String compania = "";
    public String clave = "";
    public String tipofigura = "";
    public String rfc = "";
    public String licencia = "";
    public String nombre = "";
    public String registrotributario = "";
    public String residenciafiscal = "";
    public String direccion = "";

    public void fromXML(CommonServices cs, ASPELOperadorDAO aspelOperadorDAO) {
        this.compania = aspelOperadorDAO.getEmpresa();
        this.clave = aspelOperadorDAO.CVE_OPE;
        this.tipofigura = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "TipoFigura=\"", "\"");
        this.rfc = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "RFCFigura=\"", "\"");
        this.licencia = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "NumLicencia=\"", "\"");
        this.nombre = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "NombreFigura=\"", "\"");
        this.registrotributario = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "NumRegIdTribFigura=\"", "\"");
        this.residenciafiscal = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "ResidenciaFiscalFigura=\"", "\"");
        this.direccion = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "<Domicilio ", "/>");
    }
}
