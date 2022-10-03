package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AnalyticsSearchRequestDTO   {
  
    private String endpoint = null;
    private String index = null;
    private Map<String, Object> query = new HashMap<String, Object>();

  /**
   **/
  public AnalyticsSearchRequestDTO endpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   **/
  public AnalyticsSearchRequestDTO index(String index) {
    this.index = index;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("index")
  public String getIndex() {
    return index;
  }
  public void setIndex(String index) {
    this.index = index;
  }

  /**
   **/
  public AnalyticsSearchRequestDTO query(Map<String, Object> query) {
    this.query = query;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("query")
  public Map<String, Object> getQuery() {
    return query;
  }
  public void setQuery(Map<String, Object> query) {
    this.query = query;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnalyticsSearchRequestDTO analyticsSearchRequest = (AnalyticsSearchRequestDTO) o;
    return Objects.equals(endpoint, analyticsSearchRequest.endpoint) &&
        Objects.equals(index, analyticsSearchRequest.index) &&
        Objects.equals(query, analyticsSearchRequest.query);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endpoint, index, query);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnalyticsSearchRequestDTO {\n");
    
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    index: ").append(toIndentedString(index)).append("\n");
    sb.append("    query: ").append(toIndentedString(query)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

