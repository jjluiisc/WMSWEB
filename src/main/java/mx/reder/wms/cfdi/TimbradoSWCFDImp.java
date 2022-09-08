package mx.reder.wms.cfdi;

import Services.BalanceAccount.SWBalanceAccountService;
import Services.Cancelation.SWCancelationService;
import Services.Stamp.SWStampService;
import Utils.Responses.BalanceAcctResponse;
import Utils.Responses.CancelationResponse;
import Utils.Responses.SuccessV3Response;
import com.atcloud.web.WebException;
import mx.reder.wms.cfdi.entity.TimbradoCFD;
import mx.gob.sat.cfd.x3.ComprobanteDocument;
import org.apache.log4j.Logger;

public class TimbradoSWCFDImp implements TimbradoCFD {
    static Logger log = Logger.getLogger(TimbradoSWCFDImp.class.getName());

    public String url;
    public String usuario;
    public String password;

    public TimbradoSWCFDImp(String url, String usuario, String password) {
        this.url = url;
        this.usuario = usuario;
        this.password = password;
    }

    @Override
    public RespuestaPAC getCFDI(String cfdi) throws Exception {
        log.debug("URL: "+url);
        log.debug("Usuario: "+usuario);

        try {
            SWStampService api = new SWStampService(usuario, password, url);
            SuccessV3Response response = (SuccessV3Response)api.Stamp(cfdi, "v3");

            log.debug("Status: "+response.Status);
            log.debug("HttpStatusCode: "+response.HttpStatusCode);
            log.debug("Message: "+response.message);
            log.debug("MessageDetail: "+response.messageDetail);

            if (response.Status.compareTo("success")==0) {
                ComprobanteDocument cd = ComprobanteDocument.Factory.parse(response.cfdi);

                RespuestaPAC respuesta = new RespuestaPAC();
                respuesta.setCd(cd);

                return respuesta;

            } else {
                throw new Exception(new Exception("ERROR TIMBRADO;"+response.Status+";"+response.message+";"+response.messageDetail));
            }

        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    @Override
    public RespuestaCancelacionPAC cancelaCFDI(String rfcEmisor, String[] UUIDs, String passwordCSD, String base64Cer, String base64Key) throws Exception {
        try {
            if (UUIDs.length>1)
                throw new WebException("Solo se puede cancelar un UUID por llamado");

            SWCancelationService api = new SWCancelationService(usuario, password, url);
            CancelationResponse response = (CancelationResponse)api.Cancelation(UUIDs[0], passwordCSD, rfcEmisor, base64Cer, base64Key);

            log.debug("Status: "+response.Status);
            log.debug("HttpStatusCode: "+response.HttpStatusCode);
            log.debug("Message: "+response.message);
            log.debug("MessageDetail: "+response.messageDetail);
            log.debug("Acuse: "+response.acuse);

            if (response.Status.compareTo("success")==0) {
                RespuestaCancelacionPAC respuesta = new RespuestaCancelacionPAC();
                respuesta.setMensaje(response.messageDetail);
                respuesta.setXmlAcuse(response.acuse);
                return respuesta;

            } else {
                throw new Exception(new Exception("ERROR CANCELANDO TIMBRADO;"+response.Status+";"+response.messageDetail));
            }

        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    @Override
    public String[] consultarCreditos() throws Exception {
        try {
            SWBalanceAccountService api = new SWBalanceAccountService(usuario, password, url);
            BalanceAcctResponse response = (BalanceAcctResponse)api.GetBalanceAccount();

            log.debug("Status: "+response.Status);
            log.debug("HttpStatusCode: "+response.HttpStatusCode);
            log.debug("Message: "+response.message);
            log.debug("MessageDetail: "+response.messageDetail);
            log.debug("fechaExpiracion: "+response.fechaExpiracion);
            log.debug("idClienteUsuario: "+response.idClienteUsuario);
            log.debug("idSaldoCliente: "+response.idSaldoCliente);
            log.debug("saldoTimbres: "+response.saldoTimbres);
            log.debug("timbresAsignados: "+response.timbresAsignados);
            log.debug("timbresUtilizados: "+response.timbresUtilizados);
            log.debug("unlimited: "+response.unlimited);

            return new String[] {
                String.valueOf(response.fechaExpiracion),
                String.valueOf(response.saldoTimbres),
                String.valueOf(response.timbresAsignados),
                String.valueOf(response.timbresUtilizados),
            };

        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }
}
