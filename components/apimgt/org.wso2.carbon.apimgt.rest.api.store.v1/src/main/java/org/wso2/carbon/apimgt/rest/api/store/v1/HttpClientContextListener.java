/**
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.v1;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * @author shiva.kr (24-Nov-2022)
 *
 */
public class HttpClientContextListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(HttpClientContextListener.class);

    public static final String APIM_STORE_HTTPCLIENT = "APIM_STORE_HTTPCLIENT";
    public static final String ELASTIC_SEARCH_USER_NAME = "ELASTIC_SEARCH_USER_NAME";
    public static final String ELASTIC_SEARCH_USER_PWD = "ELASTIC_SEARCH_USER_PWD";

    public static String getPropertyValue(String key, String def) {
        if (isNotBlank(System.getProperty(key))) {
            return System.getProperty(key);
        } else if (isNotBlank(System.getenv(key))) {
            return System.getenv(key);
        }
        return def;
    }

    private static CloseableHttpClient buildClient()
        throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        String esUsername = getPropertyValue(ELASTIC_SEARCH_USER_NAME, null);
        String esPassword = getPropertyValue(ELASTIC_SEARCH_USER_PWD, null);
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (isNotBlank(esUsername) && isNotBlank(esPassword)) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(esUsername, esPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            builder.setDefaultCredentialsProvider(provider);
        }
        return builder.setMaxConnTotal(50).setMaxConnPerRoute(20)
            .setSSLSocketFactory(new SSLConnectionSocketFactory(
                SSLContextBuilder.create().loadTrustMaterial(null, (arg0, arg1) -> true).build(),
                new NoopHostnameVerifier()))
            .build();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            sce.getServletContext().setAttribute(APIM_STORE_HTTPCLIENT, buildClient());
            log.debug("Httpclient created again.");
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (null != sce.getServletContext().getAttribute(APIM_STORE_HTTPCLIENT)) {
                ((CloseableHttpClient) sce.getServletContext().getAttribute(APIM_STORE_HTTPCLIENT)).close();
                log.debug("httpclient closed");
            }
        } catch (IOException e) {
            log.error("Failed to close Httpclient", e);
        }
    }

}
