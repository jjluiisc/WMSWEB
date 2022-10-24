package mx.reder.wms.cfdi;

/**
 *
 * @author joelbecerramiranda
 */
public class TimbradoExceptionSAT extends TimbradoException {
    private String status = null;
    private String message = null;
    private String messageDetail = null;

    public TimbradoExceptionSAT(String xml, String status, String message, String messageDetail) {
        super(xml, status+";"+message+";"+messageDetail);
        this.status = status;
        this.message = message;
        this.messageDetail = messageDetail;
    }

    public String getStatus() {
        return status;
    }

    public String getMessageError() {
        return message;
    }

    public String getMessageDetail() {
        return messageDetail;
    }
}
