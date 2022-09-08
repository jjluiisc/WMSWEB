package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordASPEL;
import java.util.Date;

public class ASPELClienteDAO implements DatabaseRecord, DatabaseRecordASPEL, java.io.Serializable {
    protected String empresa = "";
    public String CLAVE = "";
    public String STATUS = "";
    public String NOMBRE = "";
    public String RFC = "";
    public String CALLE = "";
    public String NUMINT = "";
    public String NUMEXT = "";
    public String CRUZAMIENTOS = "";
    public String CRUZAMIENTOS2 = "";
    public String COLONIA = "";
    public String CODIGO = "";
    public String LOCALIDAD = "";
    public String MUNICIPIO = "";
    public String ESTADO = "";
    public String PAIS = "";
    public String NACIONALIDAD = "";
    public String REFERDIR = "";
    public String TELEFONO = "";
    public String CLASIFIC = "";
    public String FAX = "";
    public String PAG_WEB = "";
    public String CURP = "";
    public String CVE_ZONA = "";
    public String IMPRIR = "";
    public String MAIL = "";
    public Integer NIVELSEC = 0;
    public String ENVIOSILEN = "";
    public String EMAILPRED = "";
    public String DIAREV = "";
    public String DIAPAGO = "";
    public String CON_CREDITO = "";
    public Integer DIASCRED = 0;
    public Double LIMCRED = 0.0;
    public Double SALDO = 0.0;
    public Integer LISTA_PREC = 0;
    public Integer CVE_BITA = 0;
    public String ULT_PAGOD = "";
    public Double ULT_PAGOM = 0.0;
    public Date ULT_PAGOF = new Date(0);
    public Double DESCUENTO = 0.0;
    public String ULT_VENTAD = "";
    public Double ULT_COMPM = 0.0;
    public Date FCH_ULTCOM = new Date(0);
    public Double VENTAS = 0.0;
    public String CVE_VEND = "";
    public Integer CVE_OBS = 0;
    public String TIPO_EMPRESA = "";
    public String MATRIZ = "";
    public String PROSPECTO = "";
    public String CALLE_ENVIO = "";
    public String NUMINT_ENVIO = "";
    public String NUMEXT_ENVIO = "";
    public String CRUZAMIENTOS_ENVIO = "";
    public String CRUZAMIENTOS_ENVIO2 = "";
    public String COLONIA_ENVIO = "";
    public String LOCALIDAD_ENVIO = "";
    public String MUNICIPIO_ENVIO = "";
    public String ESTADO_ENVIO = "";
    public String PAIS_ENVIO = "";
    public String CODIGO_ENVIO = "";
    public String CVE_ZONA_ENVIO = "";
    public String REFERENCIA_ENVIO = "";
    public String CUENTA_CONTABLE = "";
    public String ADDENDAF = "";
    public String ADDENDAD = "";
    public String NAMESPACE = "";
    public String METODODEPAGO = "";
    public String NUMCTAPAGO = "";
    public String MODELO = "";
    public String DES_IMPU1 = "";
    public String DES_IMPU2 = "";
    public String DES_IMPU3 = "";
    public String DES_IMPU4 = "";
    public String DES_PER = "";
    public Double LAT_GENERAL = 0.0;
    public Double LON_GENERAL = 0.0;
    public Double LAT_ENVIO = 0.0;
    public Double LON_ENVIO = 0.0;
    public String UUID = "";
    public Date VERSION_SINC = new Date(0);
    public String USO_CFDI = "";
    public String CVE_PAIS_SAT = "";
    public String NUMIDREGFISCAL = "";
    public String FORMADEPAGOSAT = "";
    public String ADDENDAE = "";
    public String ADDENDAG = "";

    public ASPELClienteDAO() {
    }

    public ASPELClienteDAO(String CLAVE) {
        this.CLAVE = CLAVE;
    }

    @Override
    public String getTable() {
        return "REDER20.dbo.CLIE"+empresa;
    }

    @Override
    public String getOrder() {
        return "CLAVE";
    }

    @Override
    public String getWhere() {
        return "CLAVE = '"+CLAVE+"'";
    }

    @Override
    public String toString() {
        return CLAVE+";"+STATUS+";"+NOMBRE;
    }

    @Override
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
