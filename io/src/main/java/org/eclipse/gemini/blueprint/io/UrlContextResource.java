/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.io;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.io.internal.OsgiResourceUtils;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;

/**
 * Extension to {@link UrlResource} that adds support for
 * {@link ContextResource}. This resource is used by the
 * {@link OsgiBundleResourcePatternResolver} with the URLs returned by the OSGi
 * API.
 * 
 * @author Costin Leau
 */
class UrlContextResource extends UrlResource implements ContextResource {
  private static final Log logger = LogFactory.getLog(UrlContextResource.class);
	private final String pathWithinContext;


	/**
	 * Constructs a new <code>UrlContextResource</code> instance.
	 * 
	 * @param path
	 * @throws MalformedURLException
	 */
	public UrlContextResource(String path) throws MalformedURLException {
		super(path);
		pathWithinContext = checkPath(path);
	}

	private String checkPath(String path) {
		return (path.startsWith(OsgiResourceUtils.FOLDER_DELIMITER) ? path : OsgiResourceUtils.FOLDER_DELIMITER + path);
	}

	/**
	 * Constructs a new <code>UrlContextResource</code> instance.
	 * 
	 * @param url
	 */
	public UrlContextResource(URL url, String path) {
		super(url);
		this.pathWithinContext = checkPath(path);
	}

	public String getPathWithinContext() {
		return pathWithinContext;
	}

	  @Override
  public int hashCode() {
    try {
      URI uri = getURI();
      URL url = getURL();
      logger.debug("Trying to lookupAllHostAddr for URL = " + url.toString() + " host = " + url.getHost() + " port = " + url.getPort());
      URL cleanedUrl = getCleanedUrl(url, uri.toString());
      return cleanedUrl.toString().hashCode();
    }
    catch (IOException e) {
      try {
        return getURL().toString().hashCode();
      }
      catch (IOException e1) {
        throw new RuntimeException(e1);
      }
    }
  }

  private URL getCleanedUrl(URL originalUrl, String originalPath) {
    try {
      return new URL(StringUtils.cleanPath(originalPath));
    }
    catch (MalformedURLException ex) {
      // Cleaned URL path cannot be converted to URL
      // -> take original URL.
      return originalUrl;
    }
  }
}
