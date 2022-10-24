package mx.reder.wms.cfdi.entity;

import java.util.Date;

public interface CertificadoSelloDigitalCFD {
    public String getNocertificado();
    public String getPassword();
    public Date getFechaInicial();
    public Date getFechaFinal();
    public byte[] getArchivoKey();
    public byte[] getArchivoCer();
    public void setNocertificado(String nocertificado);
    public void setFechaInicial(Date fechainicial);
    public void setFechaFinal(Date fechafinal);
}
