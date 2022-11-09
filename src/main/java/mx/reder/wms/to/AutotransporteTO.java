package mx.reder.wms.to;

import com.atcloud.util.CommonServices;
import com.atcloud.util.Numero;
import java.io.Serializable;
import mx.reder.wms.dao.entity.ASPELOperadorDAO;

public class AutotransporteTO implements Serializable {
    public String compania = "";
    public String clave = "";
    public String tipopermiso = "";
    public String numeropermiso = "";
    public String configuracion = "";
    public String placa = "";
    public String aniomodelo = "";
    public String asegurarespcivil = "";
    public String polizarespcivil = "";
    public String aseguramedambiente = "";
    public String polizamedambiente = "";
    public String aseguracarga = "";
    public String polizacarga = "";
    public double primaseguro = 0.0d;


    public void fromXML(CommonServices cs, ASPELOperadorDAO aspelOperadorDAO) {
        this.compania = aspelOperadorDAO.getEmpresa();
        this.clave = aspelOperadorDAO.CVE_OPE;
        this.tipopermiso = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "PermSCT=\"", "\"");
        this.numeropermiso = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "NumPermisoSCT=\"", "\"");
        this.configuracion = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "ConfigVehicular=\"", "\"");
        this.placa = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "PlacaVM=\"", "\"");
        this.aniomodelo = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "AnioModeloVM=\"", "\"");
        this.asegurarespcivil = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "AseguraRespCivil=\"", "\"");
        this.polizarespcivil = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "PolizaRespCivil=\"", "\"");
        this.aseguramedambiente = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "AseguraMedAmbiente=\"", "\"");
        this.polizamedambiente = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "PolizaMedAmbiente=\"", "\"");
        this.aseguracarga = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "AseguraCarga=\"", "\"");
        this.polizacarga = cs.getStringBetween(aspelOperadorDAO.XML_OPE, "PolizaCarga=\"", "\"");
        this.primaseguro = Numero.getDoubleFromString(cs.getStringBetween(aspelOperadorDAO.XML_OPE, "PrimaSeguro=\"", "\""));
    }
}
