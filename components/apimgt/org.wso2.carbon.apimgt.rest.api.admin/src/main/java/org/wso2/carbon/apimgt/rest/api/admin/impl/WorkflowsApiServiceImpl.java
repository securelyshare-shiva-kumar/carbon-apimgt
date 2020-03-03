/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.rest.api.admin.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.WorkflowMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * This class is contains rest apis related to workflows
 */
public class WorkflowsApiServiceImpl extends WorkflowsApiService {

    private static final Log log = LogFactory.getLog(WorkflowsApiService.class);


    /**
     * This is used to get the workflow pending request according to ExternalWorkflowReference
     *
     * @param externalWorkflowRef is the unique identifier for workflow request
     * @param ifNoneMatch If-None-Match header value
     * @return
     */

    @Override
    public Response workflowsExternalWorkflowRefGet(String externalWorkflowRef, String ifNoneMatch) {
        WorkflowInfoDTO workflowinfoDTO;
        try {
            Workflow workflow;

            String status="CREATED";
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            APIAdmin apiAdmin = new APIAdminImpl();
            workflow=apiAdmin.getworkflowReferenceByExternalWorkflowReferenceID(externalWorkflowRef, status ,tenantDomain);

            try {
                workflowinfoDTO = WorkflowMappingUtil.fromWorkflowsToInfoDTO(workflow);
                return Response.ok().entity(workflowinfoDTO).build();
            }
            catch(NullPointerException e){
                RestApiUtil.handleInternalServerError("Error while retrieving workflow request by the external workflow reference" , e,log);
            }
        }

        catch(APIManagementException e){
            RestApiUtil.handleInternalServerError("Error while retrieving workflow request by the external workflow reference" , e, log);
        }
        return null;
    }

    /**
     * This is used to get the workflow pending requests
     *
     * @param limit       maximum number of workflow returns
     * @param offset      starting index
     * @param accept      accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param workflowType is the the type of the workflow request. (e.g: Application Creation, Application Subscription etc.)
     * @return
     */

    @Override
    public Response workflowsGet(Integer limit, Integer offset, String accept, String ifNoneMatch, String workflowType) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;


        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

        WorkflowListDTO workflowListDTO;
        try {
            Workflow[] workflows;
            String status="CREATED";

            APIAdmin apiAdmin = new APIAdminImpl();
            workflows=apiAdmin.getworkflows(workflowType, status ,tenantDomain);


            try {
                workflowListDTO = WorkflowMappingUtil.fromWorkflowsToDTO(workflows, limit, offset);
                WorkflowMappingUtil.setPaginationParams(workflowListDTO, limit, offset,
                        workflows.length);
                return Response.ok().entity(workflowListDTO).build();
            }
            catch( NullPointerException e){
                RestApiUtil.handleInternalServerError("Error while retrieving workflow requests" , e, log);
            }
        }
        catch(APIManagementException e){
            RestApiUtil.handleInternalServerError("Error while retrieving workflow requests" , e, log);
        }
        return null;
    }

    /**
     * This is used to update the workflow status
     *
     * @param workflowReferenceId workflow reference id that is unique to each workflow
     * @param body                body should contain the status, optionally can contain a
     *                            description and an attributes object
     * @return
     */
    @Override
    public Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId, WorkflowDTO body) {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        boolean isTenantFlowStarted = false;

        try {
            if (workflowReferenceId == null) {
                RestApiUtil.handleBadRequest("workflowReferenceId is empty", log);
            }

            org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO = apiMgtDAO.retrieveWorkflow(workflowReferenceId);

            if (workflowDTO == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_WORKFLOW, workflowReferenceId, log);
            }

            String tenantDomain = workflowDTO.getTenantDomain();
            if (tenantDomain != null && !SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            if (body == null) {
                RestApiUtil.handleBadRequest("Request payload is missing", log);
            }

            if (body.getDescription() != null) {
                workflowDTO.setWorkflowDescription(body.getDescription());
            }

            if (body.getStatus() == null) {
                RestApiUtil.handleBadRequest("Workflow status is not defined", log);
            } else {
                workflowDTO.setStatus(WorkflowStatus.valueOf(body.getStatus().toString()));
            }

            if (body.getAttributes() != null) {
                workflowDTO.setAttributes(body.getAttributes());
            }

            String workflowType = workflowDTO.getWorkflowType();
            WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
            workflowExecutor.complete(workflowDTO);
            return Response.ok().entity(body).build();

        } catch (APIManagementException e) {
            String msg = "Error while resuming workflow " + workflowReferenceId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        } catch (WorkflowException e) {
            String msg = "Error while resuming workflow " + workflowReferenceId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return null;
    }
}
