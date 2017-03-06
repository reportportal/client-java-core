package com.epam.reportportal.utils;

import com.github.avarabyeu.restendpoint.http.IOUtils;
import com.google.common.io.Resources;

import java.io.InputStream;
import java.security.KeyStore;

/**
 * @author Andrei Varabyeu
 */
public class SslUtils {

    /**
     * Load keystore
     *
     * @param keyStore keystore resource
     * @param password keystore password
     * @return JKD keystore representation
     */
    public static KeyStore loadKeyStore(String keyStore, String password) {
        InputStream is = null;
        try {
            is = Resources.asByteSource(Resources.getResource(keyStore)).openStream();
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(is, password.toCharArray());
            return trustStore;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load trust store", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
