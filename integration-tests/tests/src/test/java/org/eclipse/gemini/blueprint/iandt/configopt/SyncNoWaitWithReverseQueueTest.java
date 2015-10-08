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

package org.eclipse.gemini.blueprint.iandt.configopt;

import java.awt.Shape;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Test the sync behaviour but this time, by first staring the dependency and the
 * the bundle.
 * 
 * @author Costin Leau
 * 
 */
public class SyncNoWaitWithReverseQueueTest extends BehaviorBaseTest {

	@Test
	public void testBehaviour() throws Exception {

		final Bundle bundle = installTestBundle("sync-nowait-bundle");
		final Bundle tail = installAndStartTestBundle("sync-tail-bundle");

		Thread.sleep(500);

		// followed by the bundle
		bundle.start();

		assertTrue("bundle " + tail + "hasn't been fully started", OsgiBundleUtils.isBundleActive(tail));
		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// wait for the listener to get the bundles and wait for timeout
		assertContextServiceIs(bundle, true, 2000);

		// check that the dependency service is actually started as the
		// dependency
		// bundle has started
		assertNotNull(bundleContext.getServiceReference(Shape.class.getName()));

	}

}
