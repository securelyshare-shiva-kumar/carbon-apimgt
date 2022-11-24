/**
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.apimgt.rest.api.store.v1.HttpClientContextListener.APIM_STORE_HTTPCLIENT;
import static org.wso2.carbon.apimgt.rest.api.store.v1.HttpClientContextListener.ELASTIC_SEARCH_USER_NAME;
import static org.wso2.carbon.apimgt.rest.api.store.v1.HttpClientContextListener.ELASTIC_SEARCH_USER_PWD;
import static org.wso2.carbon.apimgt.rest.api.store.v1.impl.AnalyticsApiServiceImpl.ELASTIC_SEARCH_BASE_URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.store.v1.AnalyticsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.HttpClientContextListener;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AnalyticsSearchRequestDTO;

/**
 * @author shiva.kr (24-Nov-2022)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalyticsApiServiceImplTest {

    AnalyticsApiService service;

    @Mock(answer = RETURNS_DEEP_STUBS)
    MessageContext messageContext;

    @Mock(answer = RETURNS_DEEP_STUBS)
    ServletContextEvent sce;

    @Mock(answer = RETURNS_DEEP_STUBS)
    ServletContext sc;

    HttpClientContextListener hc = new HttpClientContextListener();

    CloseableHttpClient httpClient;

    @Before
    public void setUp() {
        System.setProperty(ELASTIC_SEARCH_BASE_URL, "https://localhost:9200");
        System.setProperty(ELASTIC_SEARCH_USER_NAME, "ss");
        System.setProperty(ELASTIC_SEARCH_USER_PWD, "f");
        service = new AnalyticsApiServiceImpl();
        when(sce.getServletContext()).thenReturn(sc);
        Mockito.doAnswer(i -> {
            httpClient = i.getArgumentAt(1, CloseableHttpClient.class);
            return null;
        }).when(sc).setAttribute(eq(APIM_STORE_HTTPCLIENT), any(CloseableHttpClient.class));
        hc.contextInitialized(sce);
    }

    @After
    public void cleanUp() {
        when(sce.getServletContext().getAttribute(eq(APIM_STORE_HTTPCLIENT))).thenReturn(httpClient);
        hc.contextDestroyed(sce);
    }

    @Test
    public void searchAnalyticsDataCount() {
        try {
            AnalyticsSearchRequestDTO analyticsSearchRequestDTO = new AnalyticsSearchRequestDTO();
            analyticsSearchRequestDTO.setEndpoint("/_count");
            analyticsSearchRequestDTO.setIndex("apim_event_response");
            when(messageContext.getServletContext().getAttribute(eq(APIM_STORE_HTTPCLIENT))).thenReturn(httpClient);
            service.searchAnalyticsData(analyticsSearchRequestDTO, messageContext);
        } catch (APIManagementException e) {
            Assert.fail(e.getMessage());
        }
    }
}
