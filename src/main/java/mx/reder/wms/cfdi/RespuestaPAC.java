package mx.reder.wms.cfdi;

public class RespuestaPAC {
    private String cfdi = null;
    private String request = null;
    private String response = null;
    private String timbreFiscal = null;

    /**
     * @return the cfdi
     */
    public String getCfdi() {
        return cfdi;
    }

    /**
     * @param cfdi the cfdi to set
     */
    public void setCfdi(String cfdi) {
        this.cfdi = cfdi;
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
