package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AnalyticsSearchRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.AnalyticsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.AnalyticsApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/analytics")

@Api(description = "the analytics API")




public class AnalyticsApi  {

  @Context MessageContext securityContext;

AnalyticsApiService delegate = new AnalyticsApiServiceImpl();


    @POST
    @Path("/search")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Advance search on raw analytics data. ", notes = "This operation provides you ability to search and aggregate on raw analytics data. ", response = Object.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "AdvancedSearch" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Raw analytics response. ", response = Object.class) })
    public Response searchAnalyticsData(@ApiParam(value = "Elasticsearch query " ,required=true) AnalyticsSearchRequestDTO analyticsSearchRequestDTO) throws APIManagementException{
        return delegate.searchAnalyticsData(analyticsSearchRequestDTO, securityContext);
    }
}
