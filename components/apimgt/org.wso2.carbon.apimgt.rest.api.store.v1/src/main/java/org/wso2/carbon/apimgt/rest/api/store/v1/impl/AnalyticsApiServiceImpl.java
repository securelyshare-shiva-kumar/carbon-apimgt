package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.wso2.carbon.apimgt.rest.api.store.v1.HttpClientContextListener.APIM_STORE_HTTPCLIENT;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.store.v1.AnalyticsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.HttpClientContextListener;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AnalyticsSearchRequestDTO;

import com.google.gson.Gson;

public class AnalyticsApiServiceImpl implements AnalyticsApiService {
    private static final Log log = LogFactory.getLog(AnalyticsApiServiceImpl.class);

    public static final String ELASTIC_SEARCH_BASE_URL = "ELASTIC_SEARCH_BASE_URL";
    private final String esBaseUrl;
    private static final Gson GSON = new Gson();

    public AnalyticsApiServiceImpl() {
        esBaseUrl = HttpClientContextListener.getPropertyValue(ELASTIC_SEARCH_BASE_URL, "http://localhost:9200")
            .replaceFirst("/*$", "/");
    }

    @Override
    public Response searchAnalyticsData(AnalyticsSearchRequestDTO analyticsSearchRequestDTO,
        MessageContext messageContext) throws APIManagementException {
        log.trace("--> searchAnalyticsData");
        log.trace(analyticsSearchRequestDTO.getQuery());
        log.trace("<-- searchAnalyticsData");
        CloseableHttpClient httpClient =
            (CloseableHttpClient) messageContext.getServletContext().getAttribute(APIM_STORE_HTTPCLIENT);
        try {
            HttpPost req = new HttpPost(
                esBaseUrl + analyticsSearchRequestDTO.getIndex() + analyticsSearchRequestDTO.getEndpoint());
            req.setEntity(new StringEntity(GSON.toJson(analyticsSearchRequestDTO.getQuery()), APPLICATION_JSON));
            try (CloseableHttpResponse hr = httpClient.execute(req)) {
                if (SC_OK == hr.getStatusLine().getStatusCode()) {
                    return Response.status(Response.Status.OK).entity(EntityUtils.toString(hr.getEntity())).build();
                } else {
                    return Response.status(hr.getStatusLine().getStatusCode())
                        .entity(EntityUtils.toString(hr.getEntity())).build();
                }
            }
        } catch (IOException e) {
            throw new APIManagementException(e);
        }
    }
}
