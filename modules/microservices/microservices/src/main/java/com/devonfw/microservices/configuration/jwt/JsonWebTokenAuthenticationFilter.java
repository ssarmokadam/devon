/*******************************************************************************
 * Copyright 2015-2018 Capgemini SE.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package com.devonfw.microservices.configuration.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
public class JsonWebTokenAuthenticationFilter extends RequestHeaderAuthenticationFilter {

  public static final String ACCESS_HEADER_NAME = "Authorization";

  public static final String REFRESH_HEADER_NAME = "Authorization-Refresh";

  public static final ThreadLocal<SecurityContext> SecurityContext = new ThreadLocal<>();

  public JsonWebTokenAuthenticationFilter() {
    // Don't throw exceptions if the header is missing
    setExceptionIfHeaderMissing(false);

    // This is the request header it will look for
    setPrincipalRequestHeader(ACCESS_HEADER_NAME);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    JsonWebTokenAuthenticationFilter.SecurityContext.set(SecurityContextHolder.getContext());

    super.doFilter(request, response, chain);
  }

  @Override
  @Autowired
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {

    super.setAuthenticationManager(authenticationManager);
  }
}
