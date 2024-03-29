package com.sofar.network.https;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.util.Pair;

/**
 * Created by sf on 18/04/04.
 * 提供了3种方式的证书校验，自选合适的即可
 */
public class HttpsUtil {

  /**
   * 公签
   */
  public static SSLSocketFactory getStandardSocketFactory() {
    try {
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, null, null);
      return context.getSocketFactory();
    } catch (KeyManagementException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 信任所有证书
   */
  public static Pair<SSLSocketFactory, X509TrustManager> ignoreAllSocketFactory() {
    try {
      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
          return new java.security.cert.X509Certificate[]{};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
          throws CertificateException {
          // Not implemented
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
          throws CertificateException {
          // Not implemented
        }
      }};

      // Create an SSLContext that uses our TrustManager
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, trustAllCerts, null);
      return new Pair<>(context.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * 自签名证书
   */
  public static SSLSocketFactory getSslSocketFactory(InputStream[] certificates) {
    try {
      // 用我们的证书创建一个keystore
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null);
      int index = 0;
      for (InputStream certificate : certificates) {
        String certificateAlias = "server" + Integer.toString(index++);
        keyStore.setCertificateEntry(certificateAlias,
          certificateFactory.generateCertificate(certificate));
        try {
          if (certificate != null) {
            certificate.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // 创建一个trustmanager，只信任我们创建的keystore
      SSLContext sslContext = SSLContext.getInstance("TLS");
      TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);
      sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
      return sslContext.getSocketFactory();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}
