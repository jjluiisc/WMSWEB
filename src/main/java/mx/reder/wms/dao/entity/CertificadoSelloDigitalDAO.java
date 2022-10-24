package mx.reder.wms.dao.entity;

import java.util.Date;
import com.atcloud.dao.engine.DatabaseRecord;
import com.atcloud.dao.engine.DatabaseRecordABC;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.util.Fecha;
import mx.reder.wms.cfdi.entity.CertificadoSelloDigitalCFD;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CertificadoSelloDigitalDAO implements DatabaseRecord, DatabaseRecordABC, CertificadoSelloDigitalCFD, java.io.Serializable {
    public String compania = "";
    public String nocertificado = "";
    public String password = "";
    public Date fechainicial = new Date();
    public Date fechafinal = new Date();
    public byte[] archivokey = null;
    public byte[] archivocer = null;
    public byte[] archivop12 = null;

    public CertificadoSelloDigitalDAO() {
    }

    public CertificadoSelloDigitalDAO(String compania, String nocertificado) {
        this.compania = compania;
        this.nocertificado = nocertificado;
    }

    @Override
    public String getTable() {
        return "CertificadoSelloDigital";
    }

    @Override
    public String getOrder() {
        return "compania, nocertificado";
    }

    @Override
    public String getOrderFirst() {
        return "compania, nocertificado";
    }

    @Override
    public String getOrderLast() {
        return "compania, nocertificado DESC";
    }

    @Override
    public String getWhereFirst() {
        return "compania = '"+compania+"'";
    }

    @Override
    public String getWhereNext() {
        return "compania = '"+compania+"' AND nocertificado > '"+nocertificado+"'";
    }

    @Override
    public String getWherePrev() {
        return "compania = '"+compania+"' AND nocertificado < '"+nocertificado+"'";
    }

    @Override
    public String getWhereLast() {
        return "compania = '"+compania+"'";
    }

    @Override
    public void setKey(String[] values) {
        compania = values[0];
        nocertificado = values[1];
    }

    @Override
    public void setValues(String[] values) {
        compania = values[0];
        nocertificado = values[1];
        password = values[2];
        fechainicial = Fecha.getDate(values[3]);
        fechafinal = Fecha.getDate(values[4]);
    }

    @Override
    public String getWhere() {
        return "compania = '"+compania+"' AND nocertificado = '"+nocertificado+"'";
    }

    @Override
    public String toString() {
        return compania+";"+nocertificado;
    }

    public void delete(DatabaseServices ds, String[] valores) throws Exception {
        JSONObject json = (JSONObject)JSONValue.parse(valores[0]);

        this.compania = (String)json.get("compania");
        this.nocertificado = (String)json.get("nocertificado");
        boolean existe = ds.exists(this);

        if (existe) {
            ds.delete(this);
        }
    }

    @Override
    public String getNocertificado() {
        return nocertificado;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Date getFechaInicial() {
        return fechainicial;
    }

    @Override
    public Date getFechaFinal() {
        return fechafinal;
    }

    @Override
    public byte[] getArchivoKey() {
        return archivokey;
    }

    @Override
    public byte[] getArchivoCer() {
        return archivocer;
    }

    @Override
    public void setNocertificado(String nocertificado) {
        this.nocertificado = nocertificado;
    }

    @Override
    public void setFechaInicial(Date fechainicial) {
        this.fechainicial = fechainicial;
    }

    @Override
    public void setFechaFinal(Date fechafinal) {
        this.fechafinal = fechafinal;
    }
}
