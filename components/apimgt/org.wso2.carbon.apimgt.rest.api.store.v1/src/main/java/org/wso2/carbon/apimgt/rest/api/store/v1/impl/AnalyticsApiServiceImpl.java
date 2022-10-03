package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.store.v1.AnalyticsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AnalyticsSearchRequestDTO;

import com.google.gson.Gson;


public class AnalyticsApiServiceImpl implements AnalyticsApiService {
    private static final Log log = LogFactory.getLog(AnalyticsApiServiceImpl.class);

    private static final String ELASTIC_SEARCH_BASE_URL = "ELASTIC_SEARCH_BASE_URL";
    private static final String ELASTIC_SEARCH_USER_NAME = "ELASTIC_SEARCH_USER_NAME";
    private static final String ELASTIC_SEARCH_USER_PWD = "ELASTIC_SEARCH_USER_PWD";
    private String apiUsageUrl = "http://localhost:9200";
    private static final Gson GSON = new Gson();

    public AnalyticsApiServiceImpl() {
        if (isNotBlank(System.getenv(ELASTIC_SEARCH_BASE_URL))) {
            apiUsageUrl =
                System.getenv(ELASTIC_SEARCH_BASE_URL).replaceFirst("/*$", "/");
        }
    }

    @Override
    public Response searchAnalyticsData(AnalyticsSearchRequestDTO analyticsSearchRequestDTO,
        MessageContext messageContext) throws APIManagementException {
        log.trace("--> searchAnalyticsData");
        log.trace(analyticsSearchRequestDTO.getQuery());
        log.trace("<-- searchAnalyticsData");
        String u;
        String p;
        if (isNotBlank(u = System.getenv(ELASTIC_SEARCH_USER_NAME))
            && isNotBlank(p = System.getenv(ELASTIC_SEARCH_USER_PWD))) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(u, p);
            provider.setCredentials(AuthScope.ANY, credentials);
        }
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost req = new HttpPost(apiUsageUrl + analyticsSearchRequestDTO.getIndex() + analyticsSearchRequestDTO.getEndpoint());
            req.setEntity(new StringEntity(GSON.toJson(analyticsSearchRequestDTO.getQuery()), APPLICATION_JSON));
            HttpResponse hr = httpClient.execute(req);
            if (SC_OK == hr.getStatusLine().getStatusCode()) {
                return Response.status(Response.Status.OK).entity(EntityUtils.toString(hr.getEntity())).build();
            } else {
                return Response.status(hr.getStatusLine().getStatusCode())
                                .entity(EntityUtils.toString(hr.getEntity())).build();
            }
        } catch (IOException e) {
            throw new APIManagementException(e);
        }
    }
}
