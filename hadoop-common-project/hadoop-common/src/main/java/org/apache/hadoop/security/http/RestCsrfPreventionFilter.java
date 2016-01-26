/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.security.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter provides protection against cross site request forgery (CSRF)
 * attacks for REST APIs. Enabling this filter on an endpoint results in the
 * requirement of all client to send a particular (configurable) HTTP header
 * with every request. In the absense of this header the filter will reject the
 * attempt as a bad request.
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class RestCsrfPreventionFilter implements Filter {

  private static final Logger LOG =
      LoggerFactory.getLogger(RestCsrfPreventionFilter.class);

  public static final String CUSTOM_HEADER_PARAM = "custom-header";
  public static final String CUSTOM_METHODS_TO_IGNORE_PARAM =
      "methods-to-ignore";
  static final String HEADER_DEFAULT = "X-XSRF-HEADER";
  static final String  METHODS_TO_IGNORE_DEFAULT = "GET,OPTIONS,HEAD,TRACE";
  private String  headerName = HEADER_DEFAULT;
  private Set<String> methodsToIgnore = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String customHeader = filterConfig.getInitParameter(CUSTOM_HEADER_PARAM);
    if (customHeader != null) {
      headerName = customHeader;
    }
    String customMethodsToIgnore =
        filterConfig.getInitParameter(CUSTOM_METHODS_TO_IGNORE_PARAM);
    if (customMethodsToIgnore != null) {
      parseMethodsToIgnore(customMethodsToIgnore);
    } else {
      parseMethodsToIgnore(METHODS_TO_IGNORE_DEFAULT);
    }
    LOG.info("Adding cross-site request forgery (CSRF) protection, "
        + "headerName = {}, methodsToIgnore = {}", headerName, methodsToIgnore);
  }

  void parseMethodsToIgnore(String mti) {
    String[] methods = mti.split(",");
    methodsToIgnore = new HashSet<String>();
    for (int i = 0; i < methods.length; i++) {
      methodsToIgnore.add(methods[i]);
    }
  }

  /**
   * Returns the configured custom header name.
   *
   * @return custom header name
   */
  public String getHeaderName() {
    return headerName;
  }

  /**
   * Returns whether or not the request is allowed.
   *
   * @param method HTTP Method
   * @param header value of HTTP header defined by {@link #getHeaderName()}
   * @return true if the request is allowed, otherwise false
   */
  public boolean isRequestAllowed(String method, String header) {
    return methodsToIgnore.contains(method) || header != null;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest)request;
    if (isRequestAllowed(httpRequest.getMethod(),
        httpRequest.getHeader(headerName))) {
      chain.doFilter(request, response);
    } else {
      ((HttpServletResponse)response).sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "Missing Required Header for Vulnerability Protection");
    }
  }

  @Override
  public void destroy() {
  }

  /**
   * Constructs a mapping of configuration properties to be used for filter
   * initialization.  The mapping includes all properties that start with the
   * specified configuration prefix.  Property names in the mapping are trimmed
   * to remove the configuration prefix.
   *
   * @param conf configuration to read
   * @param confPrefix configuration prefix
   * @return mapping of configuration properties to be used for filter
   *     initialization
   */
  public static Map<String, String> getFilterParams(Configuration conf,
      String confPrefix) {
    Map<String, String> filterConfigMap = new HashMap<>();
    for (Map.Entry<String, String> entry : conf) {
      String name = entry.getKey();
      if (name.startsWith(confPrefix)) {
        String value = conf.get(name);
        name = name.substring(confPrefix.length());
        filterConfigMap.put(name, value);
      }
    }
    return filterConfigMap;
  }
}
