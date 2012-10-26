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
package org.apache.hadoop.gateway.filter;

import org.apache.http.auth.Credentials;

import javax.security.auth.Subject;
import javax.servlet.*;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.AccessController;

/**
 *
 */
public abstract class AbstractGatewayFilter implements Filter {

  private static final String SUBJECT_ATTRIBUTE = "org.apache.hadoop.gateway.user.subject";
  private static final String CREDENTIALS_ATTRIBUTE = "org.apache.hadoop.gateway.user.credentials";

  private FilterConfig config;

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {
    this.config = filterConfig;
  }

  protected FilterConfig getConfig() {
    return config;
  }

  @Override
  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {
    //System.out.println( this.getClass().getSimpleName()+".doFilter: name=" + config.getFilterName() );
    try {
      doFilter( (HttpServletRequest)request, (HttpServletResponse)response, chain );
    } catch( IOException e ) {
      e.printStackTrace();
      throw e;
    } catch( ServletException e ) {
      e.printStackTrace();
      throw e;
    } catch( Throwable t ) {
      t.printStackTrace();
      throw new ServletException( t );
    }
  }

  protected abstract void doFilter( HttpServletRequest request, HttpServletResponse response, FilterChain chain ) throws IOException, ServletException;

  @Override
  public void destroy() {
  }

  protected boolean isUserAuthenticated( HttpServletRequest request ) {
    return( getAuthenticatedUser( request ) != null );
  }

  protected Subject getAuthenticatedUser( HttpServletRequest request ) {
    return Subject.getSubject( AccessController.getContext() );
  }

  protected Credentials getUserCredentials( HttpServletRequest request ) {
    return (Credentials)request.getAttribute( CREDENTIALS_ATTRIBUTE );
  }

  public void setUserCredentials( HttpServletRequest request, Credentials credentials ) {
    request.setAttribute( CREDENTIALS_ATTRIBUTE, credentials );
  }

}