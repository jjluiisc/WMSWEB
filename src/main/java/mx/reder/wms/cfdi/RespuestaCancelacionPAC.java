package mx.reder.wms.cfdi;

public class RespuestaCancelacionPAC {
    private String mensaje = null;
    private String xmlAcuse = null;

    /**
     * @return the mensaje
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * @param mensaje the mensaje to set
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /**
     * @return the xmlAcuse
     */
    public String getXmlAcuse() {
        return xmlAcuse;
    }

    /**
     * @param xmlAcuse the xmlAcuse to set
     */
    public void setXmlAcuse(String xmlAcuse) {
        this.xmlAcuse = xmlAcuse;
    }
}
