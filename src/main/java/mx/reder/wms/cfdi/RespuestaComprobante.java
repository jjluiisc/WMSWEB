package mx.reder.wms.cfdi;

import mx.gob.sat.cfd.x4.ComprobanteDocument;
import mx.gob.sat.timbreFiscalDigital.TimbreFiscalDigitalDocument;

public class RespuestaComprobante {
    private ComprobanteDocument cd = null;
    private TimbreFiscalDigitalDocument tfd = null;
    private byte[] xml = null;
    private String qr = null;
    private String cadenaoriginal = null;

    /**
     * @return the cd
     */
    public ComprobanteDocument getCd() {
        return cd;
    }

    /**
     * @param cd the cd to set
     */
    public void setCd(ComprobanteDocument cd) {
        this.cd = cd;
    }

    /**
     * @return the tfd
     */
    public TimbreFiscalDigitalDocument getTfd() {
        return tfd;
    }

    /**
     * @param tfd the tfd to set
     */
    public void setTfd(TimbreFiscalDigitalDocument tfd) {
        this.tfd = tfd;
    }

    /**
     * @return the xml
     */
    public byte[] getXml() {
        return xml;
    }

    /**
     * @param xml the xml to set
     */
    public void setXml(byte[] xml) {
        this.xml = xml;
    }

    /**
     * @return the qr
     */
    public String getQr() {
        return qr;
    }

    /**
     * @param qr the qr to set
     */
    public void setQr(String qr) {
        this.qr = qr;
    }

    /**
     * @return the cadenaoriginal
     */
    public String getCadenaoriginal() {
        return cadenaoriginal;
    }

    /**
     * @param cadenaoriginal the cadenaoriginal to set
     */
    public void setCadenaoriginal(String cadenaoriginal) {
        this.cadenaoriginal = cadenaoriginal;
    }
}
