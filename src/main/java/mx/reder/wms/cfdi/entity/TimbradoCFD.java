package mx.reder.wms.cfdi.entity;

import mx.reder.wms.cfdi.RespuestaCancelacionPAC;
import mx.reder.wms.cfdi.RespuestaPAC;

public interface TimbradoCFD {
    public RespuestaPAC getCFDI(String cfdi) throws Exception;
    public RespuestaCancelacionPAC cancelaCFDI(String rfcEmisor, String rfcReceptor, double total, String uuid, String motivo, String folioSustitucion,
            String noCertificado, String passwordCSD, String base64Cer, String base64Key) throws Exception;
    public String[] consultarCreditos() throws Exception;
}
