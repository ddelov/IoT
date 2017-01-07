package com.estafet.iot.util;

import com.amazonaws.services.iot.client.AWSIotMqttClient;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Delcho Delov on 21.12.2016 г..
 */
public class SampleUtil {
    private static final String PropertyFile = "aws-iot-sdk-samples.properties";
    private static Logger log = Logger.getLogger(SampleUtil.class.getName());

    public static AWSIotMqttClient initClientWithCert(String clientEndpoint, String clientId, String certificateFile, String privateKeyFile, String algorithm) {
        if(clientEndpoint==null || clientId==null || certificateFile==null || privateKeyFile==null){
            throw new IllegalArgumentException("Failed to construct client due to missing certificate.");
        }
        KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile, algorithm);
        final AWSIotMqttClient awsIotMqttClient = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
        if (awsIotMqttClient == null) {
            throw new IllegalArgumentException("Failed to construct client due to missing certificate.");
        }
        return awsIotMqttClient;
    }
    public static AWSIotMqttClient initClientWithToken(String clientEndpoint, String clientId, String awsAccessKeyId, String awsSecretAccessKey, String sessionToken){
        if(clientEndpoint==null || clientId==null || awsAccessKeyId==null || awsSecretAccessKey==null){
            throw new IllegalArgumentException("Failed to construct client due to missing credentials.");
        }
        final AWSIotMqttClient awsIotMqttClient = new AWSIotMqttClient(clientEndpoint, clientId, awsAccessKeyId, awsSecretAccessKey,
                sessionToken);
        if (awsIotMqttClient == null) {
            throw new IllegalArgumentException("Failed to construct client due to missing credentials.");
        }
        return awsIotMqttClient;
    }

//    public static AWSIotMqttClient initClient(CommandArguments arguments) {
//        String clientEndpoint = arguments.getNotNull("clientEndpoint", SampleUtil.getConfig("clientEndpoint"));
//        String clientId = arguments.getNotNull("clientId", SampleUtil.getConfig("clientId"));
//
//        String certificateFile = arguments.get("certificateFile", SampleUtil.getConfig("certificateFile"));
//        String privateKeyFile = arguments.get("privateKeyFile", SampleUtil.getConfig("privateKeyFile"));
//        AWSIotMqttClient awsIotClient=null;
//        if (/*awsIotClient == null && */certificateFile != null && privateKeyFile != null) {
//            String algorithm = arguments.get("keyAlgorithm", SampleUtil.getConfig("keyAlgorithm"));
//            KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile, algorithm);
//
//            awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
//        }
//
//        if (awsIotClient == null) {
//            String awsAccessKeyId = arguments.get("awsAccessKeyId", SampleUtil.getConfig("awsAccessKeyId"));
//            String awsSecretAccessKey = arguments.get("awsSecretAccessKey", SampleUtil.getConfig("awsSecretAccessKey"));
//            String sessionToken = arguments.get("sessionToken", SampleUtil.getConfig("sessionToken"));
//
//            if (awsAccessKeyId != null && awsSecretAccessKey != null) {
//                awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, awsAccessKeyId, awsSecretAccessKey,
//                        sessionToken);
//            }
//        }
//
//        if (awsIotClient == null) {
//            throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
//        }
//        return awsIotClient;
//    }

    public static class KeyStorePasswordPair {
        public KeyStore keyStore;
        public String keyPassword;

        public KeyStorePasswordPair(KeyStore keyStore, String keyPassword) {
            this.keyStore = keyStore;
            this.keyPassword = keyPassword;
        }
    }

    public static String getConfig(String name) {
        Properties prop = new Properties();
        URL resource = SampleUtil.class.getResource(PropertyFile);
        if (resource == null) {
            return null;
        }
        try (InputStream stream = resource.openStream()) {
            prop.load(stream);
        } catch (IOException e) {
            return null;
        }
        String value = prop.getProperty(name);
        if (value == null || value.trim().length() == 0) {
            return null;
        } else {
            return value;
        }
    }

    public static KeyStorePasswordPair getKeyStorePasswordPair(String certificateFile, String privateKeyFile) {
        return getKeyStorePasswordPair(certificateFile, privateKeyFile, null);
    }

    public static KeyStorePasswordPair getKeyStorePasswordPair(String certificateFile, String privateKeyFile,
                                                               String keyAlgorithm) {
        if (certificateFile == null || privateKeyFile == null) {
            System.out.println("Certificate or private key file missing");
            return null;
        }

        Certificate certificate = loadCertificateFromFile(certificateFile);
        PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFile, keyAlgorithm);
        if (certificate == null || privateKey == null) {
            return null;
        }

        return getKeyStorePasswordPair(certificate, privateKey);
    }

    public static KeyStorePasswordPair getKeyStorePasswordPair(Certificate certificate, PrivateKey privateKey) {
        KeyStore keyStore = null;
        String keyPassword = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry("alias", certificate);

            // randomly generated key password for the key in the KeyStore
            keyPassword = new BigInteger(128, new SecureRandom()).toString(32);
            keyStore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), new Certificate[] { certificate });
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            System.out.println("Failed to create key store");
            return null;
        }

        return new KeyStorePasswordPair(keyStore, keyPassword);
    }

    private static Certificate loadCertificateFromFile(String filename) {
        Certificate certificate = null;

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Certificate file not found: " + filename);
            return null;
        }
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            certificate = certFactory.generateCertificate(stream);
        } catch (IOException | CertificateException e) {
            System.out.println("Failed to load certificate file " + filename);
        }

        return certificate;
    }

    private static PrivateKey loadPrivateKeyFromFile(String filename, String algorithm) {
        PrivateKey privateKey = null;

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Private key file not found: " + filename);
            return null;
        }
        try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
            privateKey = PrivateKeyReader.getPrivateKey(stream, algorithm);
        } catch (IOException | GeneralSecurityException e) {
            System.out.println("Failed to load private key from file " + filename);
        }

        return privateKey;
    }
    private static String readJsonPayload(String fileWithoutSuffix) {
        String content = null;

        URL url = SampleUtil.class.getResource("/json/" + fileWithoutSuffix + ".json");
        try {
            System.out.println("Resources Path : " + url.getFile());
            content = new String(Files.readAllBytes(Paths.get(url.getFile())));
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }

        return content;
    }

}
