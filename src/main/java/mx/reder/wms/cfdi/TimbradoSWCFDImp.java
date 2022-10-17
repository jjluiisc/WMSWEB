package mx.reder.wms.cfdi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import mx.reder.wms.cfdi.entity.TimbradoCFD;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class TimbradoSWCFDImp implements TimbradoCFD {
    static Logger log = Logger.getLogger(TimbradoSWCFDImp.class.getName());

    public int CONNECTION_TIMEOUT_MS = 3000; // Timeout in millis.
    public String url;
    public String usuario;
    public String password;

    public TimbradoSWCFDImp(HashMap<String, String> propiedades) {
        url = propiedades.get("url");
        usuario = propiedades.get("usuario");
        password = propiedades.get("password");
    }

    @Override
    public RespuestaPAC getCFDI(String xmlStr) throws Exception {
        log.debug("URL: "+url);
        log.debug("Usuario: "+usuario);

        try {
            String token = getToken();

            String boundary = UUID.randomUUID().toString();
            String raw = "--" + boundary
                    +"\r\nContent-Disposition: form-data; name=xml; filename=xml\r\nContent-Type: application/xml\r\n\r\n"
                    +xmlStr+"\r\n--"+boundary+"--";

            HttpPost httpPost = new HttpPost(url+"/cfdi33/stamp/v3");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setResponseTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .setConnectTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Authorization", "Bearer "+token);
            httpPost.addHeader("Content-Type", "multipart/form-data; boundary="+boundary);
            httpPost.addHeader("Content-Disposition", "form-data; name=xml; filename=xml");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            Charset chars = Charset.forName("UTF-8");
            builder.setCharset(chars);
            builder.addTextBody("xml", raw, ContentType.create("text/plain", chars));
            httpPost.setEntity(builder.build());

            String responseString = null;
            int resultCode;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse responsePost = httpClient.execute(httpPost);
            try {
                resultCode = responsePost.getCode();
                log.debug(resultCode+";"+responsePost.getReasonPhrase());
                HttpEntity entity = responsePost.getEntity();
                responseString = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                if (resultCode>=500)
                    throw new Exception("ERROR TIMBRADO;"+responsePost.getCode()+";"+responsePost.getReasonPhrase());
            } catch(Exception e) {
                log.error(e.getMessage(), e);
                throw new TimbradoException(xmlStr, "ERROR TIMBRADO;"+responsePost.getCode()+";"+responsePost.getReasonPhrase());
            } finally {
                responsePost.close();
            }

            //log.debug("responseString: "+responseString);

            JSONObject json = (JSONObject)JSONValue.parse(responseString);
            String status = (String)json.get("status");
            String message = (String)json.get("message");
            String messageDetail = (String)json.get("messageDetail");

            if (status.compareTo("success")==0) {
                JSONObject data = (JSONObject)json.get("data");
                String cfdi = (String)data.get("cfdi");

                RespuestaPAC respuesta = new RespuestaPAC();
                respuesta.setCfdi(cfdi);
                return respuesta;
            } else {
                int begin = message.indexOf("CFDI");
                int end = begin==-1 ? -1 : message.indexOf(" ", begin+1);
                String code = begin!=-1&&end!=-1 ? message.substring(begin, end) : status;
                throw new TimbradoExceptionSAT(xmlStr, code, message, messageDetail);
            }

        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public RespuestaCancelacionPAC cancelaCFDI(String rfcEmisor, String rfcReceptor, double total, String uuid, String motivo, String folioSustitucion,
            String noCertificado, String passwordCSD, String base64Cer, String base64Key) throws Exception {
        try {
            String token = getToken();

            HttpPost httpPost = new HttpPost(url+"/cfdi33/cancel/csd");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer "+token);

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("uuid", uuid);
            jsonRequest.put("password", passwordCSD);
            jsonRequest.put("rfc", rfcEmisor);
            jsonRequest.put("motivo", motivo);
            if (folioSustitucion!=null)
                jsonRequest.put("folioSustitucion", folioSustitucion);
            jsonRequest.put("b64Cer", base64Cer);
            jsonRequest.put("b64Key", base64Key);
            //log.debug(jsonRequest);

            StringEntity stringEntity = new StringEntity(jsonRequest.toJSONString());
            httpPost.setEntity(stringEntity);

            String responseString = null;
            int resultCode;
            CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse responsePost = httpclient.execute(httpPost);
            try {
                //log.debug(responsePost.getCode()+";"+responsePost.getReasonPhrase());
                resultCode = responsePost.getCode();
                HttpEntity entity = responsePost.getEntity();
                responseString = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
            } catch(Exception e) {
                throw e;
            } finally {
                responsePost.close();
            }

            //log.debug("responseString: "+responseString);

            JSONObject json = (JSONObject)JSONValue.parse(responseString);
            String status = (String)json.get("status");
            String message = (String)json.get("message");
            String messageDetail = (String)json.get("messageDetail");

            if (status.compareTo("success")==0) {
                JSONObject data = (JSONObject)json.get("data");
                String acuse = (String)data.get("acuse");

                RespuestaCancelacionPAC respuesta = new RespuestaCancelacionPAC();
                respuesta.setMensaje(messageDetail);
                respuesta.setXmlAcuse(acuse);
                return respuesta;
            } else {
                throw new Exception("ERROR CANCELANDO TIMBRADO;"+status+";"+message+";"+messageDetail);
            }

        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    @Override
    public String[] consultarCreditos() throws Exception {
        try {
            String token = getToken();

            HttpPost httpPost = new HttpPost(url+"/account/balance/");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setResponseTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .setConnectTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Authorization", "Bearer "+token);

            String request = "{}";
            log.debug(request);

            StringEntity stringEntity = new StringEntity(request);
            httpPost.setEntity(stringEntity);

            String responseString = null;
            int resultCode;
            CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse responsePost = httpclient.execute(httpPost);
            try {
                log.debug(responsePost.getCode()+";"+responsePost.getReasonPhrase());
                resultCode = responsePost.getCode();
                HttpEntity entity = responsePost.getEntity();
                responseString = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
            } catch(Exception e) {
                throw e;
            } finally {
                responsePost.close();
            }

            log.debug("responseString: "+responseString);

            JSONObject json = (JSONObject)JSONValue.parse(responseString);
            String status = (String)json.get("status");
            String message = (String)json.get("message");
            String messageDetail = (String)json.get("messageDetail");

            if (status.compareTo("success")==0) {
		JSONObject data = (JSONObject)json.get("data");

                return new String[] {
                    String.valueOf(data.get("fechaExpiracion")),
                    String.valueOf(data.get("saldoTimbres")),
                    String.valueOf(data.get("timbresAsignados")),
                    String.valueOf(data.get("timbresUtilizados")),
                };
            } else {
                throw new Exception("ERROR CONSULTANDO CREDITOS;"+status+";"+message+";"+messageDetail);
            }

        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    private String getToken() throws Exception {
        URL postUrl = new URL(url+"/security/authenticate");
        HttpURLConnection urlConnection = (HttpURLConnection)postUrl.openConnection();
        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("user", usuario);
        urlConnection.setRequestProperty("password", password);

        DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
        wr.write("{}".getBytes());

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        StringBuilder sb = new StringBuilder();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        br.close();
        in.close();

        //log.debug(sb.toString());

        JSONObject json = (JSONObject)JSONValue.parse(sb.toString());
        JSONObject data = (JSONObject)json.get("data");
        return (String)data.get("token");
    }
}
