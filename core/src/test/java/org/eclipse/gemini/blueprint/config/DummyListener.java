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

package org.eclipse.gemini.blueprint.config;

import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;

import java.util.Map;

/**
 * @author Costin Leau
 */
public class DummyListener implements OsgiServiceLifecycleListener {

    static int BIND_CALLS = 0;
    static int UNBIND_CALLS = 0;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gemini.blueprint.service.OsgiServiceLifecycleListener#bind(java.lang.String,
     *      java.lang.Object)
     */
    public void bind(Object service, Map props) {
        BIND_CALLS++;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gemini.blueprint.service.OsgiServiceLifecycleListener#unbind(java.lang.String,
     *      java.lang.Object)
     */
    public void unbind(Object service, Map props) {
        UNBIND_CALLS++;
    }

}
