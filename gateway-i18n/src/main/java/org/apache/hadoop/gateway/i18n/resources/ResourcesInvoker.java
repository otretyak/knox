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
package org.apache.hadoop.gateway.i18n.resources;

import org.apache.hadoop.gateway.i18n.messages.Messages;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class ResourcesInvoker implements InvocationHandler {

  private static ResourceBundle MISSING_BUNDLE = new ListResourceBundle() {
    @Override
    protected Object[][] getContents() {
      return null;
    }
  };

  private Class bundleClass;
  private String bundleName;
  private ConcurrentHashMap<Locale, ResourceBundle> bundles;

  public ResourcesInvoker( Class<?> bundleClass ) {
    this.bundleClass = bundleClass;
    this.bundleName = calcBundleName( bundleClass );
    this.bundles = new ConcurrentHashMap<Locale, ResourceBundle>();
  }

  @Override
  public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
    return getText( method, args );
  }

  protected String getText( Method method, Object[] args ) {
    String pattern = getPattern( method );
    String text = MessageFormat.format( pattern, args );
    return text;
  }

  protected String getPattern( Method method ) {
    String pattern = getBundlePattern( method );
    if( pattern == null ) {
      pattern = getAnnotationPattern( method );
      if( pattern == null || Resource.DEFAULT_TEXT.equals( pattern ) ) {
        pattern = getDefaultPattern( method );
      }
    }
    return pattern;
  }

  protected String getAnnotationPattern( Method method ) {
    String pattern = null;
    Resource anno = method.getAnnotation( Resource.class );
    if( anno != null ) {
      pattern = anno.text();
    }
    return pattern;
  }

  protected String getBundlePattern( final Method method ) {
    String pattern = null;
    ResourceBundle bundle = findBundle();
    if( bundle != null && bundle.containsKey( method.getName() ) ) {
      pattern = bundle.getString( method.getName() );
    }
    return pattern;
  }

  protected static String getDefaultPattern( Method method ) {
    String prefix = method.getName();
    String suffix;
    int params = method.getParameterTypes().length;
    switch( params ) {
      case( 0 )  : suffix = ""; break;
      case( 1 )  : suffix = "(\"{0}\")"; break;
      case( 2 )  : suffix = "(\"{0}\",\"{1}\")"; break;
      case( 3 )  : suffix = "(\"{0}\",\"{1}\",\"{2}\")"; break;
      case( 4 )  : suffix = "(\"{0}\",\"{1}\",\"{2}\",\"{3}\")"; break;
      case( 5 )  : suffix = "(\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\")"; break;
      case( 6 )  : suffix = "(\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\",\"{5}\")"; break;
      case( 7 )  : suffix = "(\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\",\"{5}\",\"{6}\")"; break;
      case( 8 )  : suffix = "(\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\",\"{5}\",\"{6}\",\"{7}\")"; break;
      case( 9 )  : suffix = "(\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\",\"{5}\",\"{6}\",\"{7}\",\"{8}\")"; break;
      case( 10 ) : suffix = "(\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\",\"{5}\",\"{6}\",\"{7}\",\"{8}\",\"{9}\")"; break;
      default    : suffix = createDefaultPatternSuffix( params );
    }
    return prefix + suffix;
  }

  private static String createDefaultPatternSuffix( int size ) {
    StringBuilder builder = new StringBuilder( 1 + size*7 );
    builder.append( "(" );
    for( int i=0; i<size; i++ ) {
      if( i>0 ) {
        builder.append( "," );
      }
      builder.append( "\"{" ).append( i ).append( "}\"" );
    }
    builder.append( ")" );
    return builder.toString();

  }

  private static String calcBundleName( Class<?> clazz ) {
    String bundle = null;
    Resources anno = clazz.getAnnotation( Resources.class );
    if( anno != null ) {
      bundle = anno.bundle();
      if( Resources.DEFAULT_BUNDLE.equals( bundle ) ) {
        bundle = null;
      }
    }
    if( bundle == null ) {
      bundle = clazz.getCanonicalName().replace( '.', '/' );
    }
    return bundle;
  }


  protected String getBundleName() {
    return bundleName;
  }

  protected ResourceBundle findBundle() {
    Locale locale = Locale.getDefault();
    ResourceBundle bundle = bundles.get( locale );
    if( bundle == MISSING_BUNDLE ) {
      bundle = null;
    } else if ( bundle == null ) {
      try {
        bundle = ResourceBundle.getBundle( getBundleName(), locale, bundleClass.getClassLoader() );
        bundles.put( locale, bundle );
      } catch( MissingResourceException e ) {
        bundles.put( locale, MISSING_BUNDLE );
      }
    }
    return bundle;
  }

}
