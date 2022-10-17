package mx.reder.wms.cfdi;

/**
 *
 * @author joelbecerramiranda
 */
public class TimbradoException extends Exception {
    private String xml;

    public TimbradoException() {
    }

    public TimbradoException(String xml, String msg) {
        super(msg);
        this.xml = xml;
    }

    public String getXml() {
        return xml;
    }
}
