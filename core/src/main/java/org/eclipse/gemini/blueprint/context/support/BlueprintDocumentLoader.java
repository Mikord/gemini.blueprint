/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 * <p>
 * Contributors:
 * VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Specialized {@link DocumentLoader} that allows blueprint configurations without a schema location to be properly
 * validated.
 *
 * @author Costin Leau
 */
class BlueprintDocumentLoader extends DefaultDocumentLoader {

  static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
  static final String BLUEPRINT_SCHEMA = "http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd";

  /**
   * logger
   */
  private static final Log log = LogFactory.getLog(BlueprintDocumentLoader.class);

  @Override
  protected DocumentBuilderFactory createDocumentBuilderFactory(int validationMode, boolean namespaceAware)
      throws ParserConfigurationException {
    Bundle bundle = FrameworkUtil.getBundle(getClass());
    DocumentBuilderFactory factory = null;

    if (bundle != null && bundle.getBundleContext() != null) {
      ServiceReference<DocumentBuilderFactory> serviceReference = bundle.getBundleContext().getServiceReference(DocumentBuilderFactory.class);

      if (serviceReference != null) {
        factory = bundle.getBundleContext().getService(serviceReference);
        factory.setNamespaceAware(namespaceAware);

        if (validationMode != 0) {
          factory.setValidating(true);
        }
        if (validationMode == 3) {
          factory.setNamespaceAware(true);
        }
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
      }
    }

    if (factory == null) {
      factory = super.createDocumentBuilderFactory(validationMode, namespaceAware);
    }

    try {
      factory.setAttribute(JAXP_SCHEMA_SOURCE, BLUEPRINT_SCHEMA);
    }
    catch (IllegalArgumentException ex) {
      log.warn("Cannot work with attribute " + JAXP_SCHEMA_SOURCE
          + " - configurations w/o a schema locations will likely fail to validate", ex);
    }

    return factory;
  }
}
