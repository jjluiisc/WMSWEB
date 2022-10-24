package mx.reder.wms.cfdi;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mx.reder.wms.cfdi.entity.CertificadoSelloDigitalCFD;
import org.apache.commons.ssl.PKCS8Key;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class EncriptacionFacade {
    static Logger log = Logger.getLogger(EncriptacionFacade.class.getName());
    private static EncriptacionFacade singleton = null;
    private Map<String, PrivateKey> privateKeys = null;
    private Map<String, CertificadoSelloDigitalCFD> certificados = null;
    private Map<String, String> certificadosBase64 = null;

    private EncriptacionFacade() {
        privateKeys = new HashMap<>();
        certificados = new HashMap<>();
        certificadosBase64 = new HashMap<>();
    }

    public void inicializa(CertificadoSelloDigitalCFD efacturasellos) throws Exception {
        if(certificados.containsKey(efacturasellos.getNocertificado()))
            return;

        log.info("Inicializando el certificado ["+efacturasellos.toString()+"] ...");

        certificados.put(efacturasellos.getNocertificado(), efacturasellos);

        certificadosBase64.put(efacturasellos.getNocertificado(), new String(Base64.getEncoder().encode(efacturasellos.getArchivoCer())));
    }

    public void delete(String nocertificado) {
        certificados.remove(nocertificado);
        certificadosBase64.remove(nocertificado);
    }

    public boolean validar(CertificadoSelloDigitalCFD efacturasellos) throws Exception {
        log.info("Validando el Certificado ["+efacturasellos+"] ...");

        X509Certificate cert = getCertificate(efacturasellos.getArchivoCer());

        byte[] bSerialNumber = cert.getSerialNumber().toByteArray();
        StringBuilder serialNumber = new StringBuilder();
        for(int i=0; i<bSerialNumber.length; i++)
            serialNumber.append((char)bSerialNumber[i]);
        String nocertificado = serialNumber.toString();
        log.info("serialNumber = "+nocertificado);
        if (!nocertificado.equals(efacturasellos.getNocertificado())) {
            log.info("Se cambio el numero del Certificado a "+nocertificado);
            efacturasellos.setNocertificado(nocertificado);
        }
        log.info("notBefore = "+cert.getNotBefore());
        if (cert.getNotBefore().compareTo(efacturasellos.getFechaInicial())!=0) {
            log.info("Se cambio la fecha inicial del Certificado a "+cert.getNotBefore());
            efacturasellos.setFechaInicial(cert.getNotBefore());
        }
        log.info("notAfter = "+cert.getNotAfter());
        if (cert.getNotAfter().compareTo(efacturasellos.getFechaFinal())!=0) {
            log.info("Se cambio la fecha final del Certificado a "+cert.getNotAfter());
            efacturasellos.setFechaFinal(cert.getNotAfter());
        }

        log.info("issuerDN = "+cert.getIssuerDN().toString());
        log.info("issuerX500Principal = "+cert.getIssuerX500Principal().toString());
        log.info("subjectDN = "+cert.getSubjectDN().toString());
        log.info("subjectX500Principal = "+cert.getSubjectX500Principal().toString());
        List<String> usages = cert.getExtendedKeyUsage();
        if (usages!=null) {
            for (String usage : usages) {
                log.info("usage = "+usage);
            }
        }
        log.info("sigAlgOID = "+cert.getSigAlgOID());
        log.info("sigAlgName = "+cert.getSigAlgName());

        log.info("Ahora el certificado es ["+efacturasellos.toString()+"]");

        delete(efacturasellos.getNocertificado());
        inicializa(efacturasellos);

        String data = new java.util.Date().toString();
        log.info("data = "+data);

        String signature = firma(efacturasellos.getNocertificado(), data);
        log.info("signature = "+signature);

        boolean valid = verifica(efacturasellos.getNocertificado(), data, Base64.getDecoder().decode(signature.getBytes()));
        log.info("valid = "+valid);

        return valid;
    }

    public PrivateKey initPrivateKey(byte[] datosArchivoLLave, String password) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        PKCS8Key pkcs8 = new PKCS8Key(datosArchivoLLave, password.toCharArray());
                KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec pkcs8Encoded = new PKCS8EncodedKeySpec(pkcs8.getDecryptedBytes());
                PrivateKey privateKey = privateKeyFactory.generatePrivate(pkcs8Encoded);

        return privateKey;
    }

    private PrivateKey getPrivateKey(CertificadoSelloDigitalCFD efacturasellos) throws Exception {
        PrivateKey privateKey = (PrivateKey)privateKeys.get(efacturasellos.getNocertificado());
        if(privateKey==null) {
            privateKey = initPrivateKey(efacturasellos.getArchivoKey(), efacturasellos.getPassword());
            privateKeys.put(efacturasellos.getNocertificado(), privateKey);
        }
        return privateKey;
    }

    private PublicKey getPublicKey(byte[] certificate) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        X509Certificate cert = getCertificate(certificate);
        return cert.getPublicKey();
    }

    public String firma(String nocertificado, String data) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Signature sig = Signature.getInstance("SHA256withRSA", "BC");

        CertificadoSelloDigitalCFD efacturasellos = (CertificadoSelloDigitalCFD)certificados.get(nocertificado);
        if(efacturasellos==null)
            throw new Exception("No se ha inicializado este certificado ["+nocertificado+"]");

        sig.initSign(getPrivateKey(efacturasellos));
        sig.update(data.getBytes("UTF-8"));
        byte salida[] = sig.sign();

        byte[] all = Base64.getEncoder().encode(salida);

        return new String(all);
    }

    public String firmaBytes(String nocertificado, byte[] data) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Signature sig = Signature.getInstance("SHA256withRSA", "BC");

        CertificadoSelloDigitalCFD efacturasellos = (CertificadoSelloDigitalCFD)certificados.get(nocertificado);
        if(efacturasellos==null)
            throw new Exception("No se ha inicializado este certificado ["+nocertificado+"]");

        sig.initSign(getPrivateKey(efacturasellos));
        sig.update(data);
        byte salida[] = sig.sign();

        byte[] all = Base64.getEncoder().encode(salida);

        return new String(all);
    }

    public boolean verifica(String nocertificado, String data, byte[] signature) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Signature sig = Signature.getInstance("SHA256withRSA", "BC");

        CertificadoSelloDigitalCFD efacturasellos = (CertificadoSelloDigitalCFD)certificados.get(nocertificado);
        if(efacturasellos==null)
            throw new Exception("No se ha inicializado este certificado ["+nocertificado+"]");

        sig.initVerify(getPublicKey(efacturasellos.getArchivoCer()));
        sig.update(data.getBytes("UTF-8"));
        boolean valid = sig.verify(signature);

        return valid;
    }

    public String getCertificadoBase64(String nocertificado) {
        return (String)certificadosBase64.get(nocertificado);
    }

    public X509Certificate getCertificate(byte[] certificate) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        ByteArrayInputStream bais = new ByteArrayInputStream(certificate);
        CertificateFactory fact = CertificateFactory.getInstance("X.509", "BC");
        Certificate cert = fact.generateCertificate(bais);
        return (X509Certificate) cert;
    }

    public static EncriptacionFacade getInstance() {
        if (singleton==null)
            singleton = new EncriptacionFacade();
        return singleton;
    }
}
