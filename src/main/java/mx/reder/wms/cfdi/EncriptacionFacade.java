package mx.reder.wms.cfdi;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import mx.reder.wms.dao.entity.CertificadoSelloDigitalDAO;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.EncryptionScheme;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RC2CBCParameter;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class EncriptacionFacade {
    static Logger log = Logger.getLogger(EncriptacionFacade.class.getName());
    private static EncriptacionFacade singleton = null;
    private Map privateKeys = null;
    private Map certificados = null;
    private Map certificadosBase64 = null;

    private EncriptacionFacade() {
        privateKeys = new HashMap();
        certificados = new HashMap();
        certificadosBase64 = new HashMap();
    }

    public void inicializa(CertificadoSelloDigitalDAO efacturasellos) throws Exception {
        if(certificados.containsKey(efacturasellos.nocertificado))
            return;

        log.info("Inicializando el certificado ["+efacturasellos.toString()+"] ...");

        certificados.put(efacturasellos.nocertificado, efacturasellos);

        Base64 base = new Base64();
        certificadosBase64.put(efacturasellos.nocertificado, new String(base.encode(efacturasellos.archivocer)));
    }

    public boolean validar(CertificadoSelloDigitalDAO efacturasellos) throws Exception {
        log.info("Validando el Certificado ["+efacturasellos+"] ...");

        X509Certificate cert = getCertificate(efacturasellos);

        boolean salvar = false;
        byte[] bSerialNumber = cert.getSerialNumber().toByteArray();
        StringBuilder serialNumber = new StringBuilder();
        for(int i=0; i<bSerialNumber.length; i++)
            serialNumber.append((char)bSerialNumber[i]);
        String nocertificado = serialNumber.toString();
        log.info("serialNumber = "+nocertificado);
        if (!nocertificado.equals(efacturasellos.nocertificado)) {
            log.info("Se cambio el numero del Certificado a "+nocertificado);
            efacturasellos.nocertificado = nocertificado;
            salvar = true;
        }
        log.info("notBefore = "+cert.getNotBefore());
        if (cert.getNotBefore().compareTo(efacturasellos.fechainicial)!=0) {
            log.info("Se cambio la fecha inicial del Certificado a "+cert.getNotBefore());
            efacturasellos.fechainicial = new java.sql.Timestamp(cert.getNotBefore().getTime());
            salvar = true;
        }
        log.info("notAfter = "+cert.getNotAfter());
        if (cert.getNotAfter().compareTo(efacturasellos.fechafinal)!=0) {
            log.info("Se cambio la fecha final del Certificado a "+cert.getNotAfter());
            efacturasellos.fechafinal = new java.sql.Timestamp(cert.getNotAfter().getTime());
            salvar = true;
        }

        log.info("issuerDN = "+cert.getIssuerDN().toString());
        log.info("subjectDN = "+cert.getSubjectDN().toString());

        if(salvar)
            log.info("Ahora el certificado es ["+efacturasellos.toString()+"]");

        if(!certificados.containsKey(efacturasellos.nocertificado))
            inicializa(efacturasellos);

        String signature = firma(efacturasellos.nocertificado, new java.util.Date().toString());
        log.info("signature = "+signature);
        return salvar;
    }

    private byte[] obtenDatosLLavePrivada(byte[] datosArchivo, String passwordStr) throws Exception {
        ByteArrayInputStream    bIn = new ByteArrayInputStream(datosArchivo);
        ASN1InputStream         aIn = new ASN1InputStream(bIn);
        EncryptedPrivateKeyInfo info = new EncryptedPrivateKeyInfo((ASN1Sequence)aIn.readObject());
        PBES2Parameters         alg = new PBES2Parameters((ASN1Sequence)info.getEncryptionAlgorithm().getParameters());
        PBKDF2Params            func = PBKDF2Params.getInstance(alg.getKeyDerivationFunc().getParameters());
        EncryptionScheme        scheme = alg.getEncryptionScheme();
        int keySize = 0;
        if (func.getKeyLength() != null) {
            keySize = func.getKeyLength().intValue() * 8;
        }
        int     iterationCount = func.getIterationCount().intValue();
        byte[]  salt = func.getSalt();
        char[]  password = passwordStr.toCharArray();

        PBEParametersGenerator  generator = new PKCS5S2ParametersGenerator();
        generator.init(
            PBEParametersGenerator.PKCS5PasswordToBytes(password),
            salt,
            iterationCount);

        CipherParameters    param;
        BufferedBlockCipher cipherAux =  null;
        if (PKCSObjectIdentifiers.RC2_CBC.equals( scheme.getObjectId() )) {
            RC2CBCParameter rc2Params = new RC2CBCParameter((ASN1Sequence)scheme.getObject());
            byte[]  iv = rc2Params.getIV();
            param = new ParametersWithIV(generator.generateDerivedParameters(keySize), iv);
            cipherAux = new PaddedBufferedBlockCipher(new CBCBlockCipher(new RC2Engine()));
        } else if (PKCSObjectIdentifiers.des_EDE3_CBC.equals(scheme.getObjectId())) {
            keySize=192;//Tamano de llave
            byte[]  iv = ((ASN1OctetString)scheme.getObject()).getOctets();
            param = new ParametersWithIV(generator.generateDerivedParameters(keySize), iv);
            cipherAux = new PaddedBufferedBlockCipher(new CBCBlockCipher(new DESedeEngine()));
        } else {
            byte[]  iv = ((ASN1OctetString)scheme.getObject()).getOctets();
            param = new ParametersWithIV(generator.generateDerivedParameters(keySize), iv);
        }

        cipherAux.init(false, param);

        byte[]  data = info.getEncryptedData();
        byte[]  out = new byte[cipherAux.getOutputSize(data.length)];
        int     len = cipherAux.processBytes(data, 0, data.length, out, 0);

        len += cipherAux.doFinal(out, len);
        return out;
    }

    private PrivateKey obtenLLavePrivada(byte[] datosLLave) throws Exception {
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(datosLLave);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
        return keyFactory.generatePrivate(privateKeySpec);
    }

    private PrivateKey initPrivateKey(byte[] datosArchivoLLave, String password) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        byte[] datosLLave = obtenDatosLLavePrivada(datosArchivoLLave, password);
        return obtenLLavePrivada(datosLLave);
    }

    private PrivateKey getPrivateKey(CertificadoSelloDigitalDAO efacturasellos) throws Exception {
        PrivateKey privateKey = (PrivateKey)privateKeys.get(efacturasellos.nocertificado);
        if(privateKey==null) {
            privateKey = initPrivateKey(efacturasellos.archivokey, efacturasellos.password);
            privateKeys.put(efacturasellos.nocertificado, privateKey);
        }
        return privateKey;
    }

    public String firma(String nocertificado, String data) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Signature sig = Signature.getInstance("SHA256withRSA", "BC");

        CertificadoSelloDigitalDAO efacturasellos = (CertificadoSelloDigitalDAO)certificados.get(nocertificado);
        if(efacturasellos==null)
            throw new Exception("No se ha inicializado este certificado ["+nocertificado+"]");

        sig.initSign(getPrivateKey(efacturasellos));
        sig.update(data.getBytes("UTF-8"));
        byte salida[] = sig.sign();

        Base64 base = new Base64();
        byte[] all = base.encode(salida);

        return new String(all);
    }

    public String getCertificadoBase64(String nocertificado) {
        return (String)certificadosBase64.get(nocertificado);
    }

    public X509Certificate getCertificate(CertificadoSelloDigitalDAO efacturasellos) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        ByteArrayInputStream bais = new ByteArrayInputStream(efacturasellos.archivocer);
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
