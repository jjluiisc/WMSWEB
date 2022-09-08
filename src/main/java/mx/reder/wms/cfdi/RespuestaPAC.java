package mx.reder.wms.cfdi;

import mx.gob.sat.cfd.x3.ComprobanteDocument;

public class RespuestaPAC {
    private ComprobanteDocument cd = null;
    private String request = null;
    private String response = null;
    private String timbreFiscal = null;

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
     * @return the request
     */
    public String getRequest() {
        return request;
    }

    /**
     * @param request the request to set
     */
    public void setRequest(String request) {
        this.request = request;
    }

    /**
     * @return the response
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * @return the timbreFiscal
     */
    public String getTimbreFiscal() {
        return timbreFiscal;
    }

    /**
     * @param timbreFiscal the timbreFiscal to set
     */
    public void setTimbreFiscal(String timbreFiscal) {
        this.timbreFiscal = timbreFiscal;
    }
}
