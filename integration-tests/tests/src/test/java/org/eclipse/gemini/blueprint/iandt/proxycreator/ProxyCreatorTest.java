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

package org.eclipse.gemini.blueprint.iandt.proxycreator;

import java.security.Permission;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

/**
 * Integration test that checks that a new classloader is created when the
 * bundle is refreshed. The test updates a bundle that internally creates JDK
 * and CGLIB proxies which, will fail in case the old CL is preserved.
 * 
 * @author Costin Leau
 * 
 */
public class ProxyCreatorTest extends BaseIntegrationTest {

	private static final String PROXY_CREATOR_SYM_NAME = "org.eclipse.gemini.blueprint.iandt.proxy.creator";

	@Override
	public Option[] getExtraConfig()
	{
		return options(
				mavenBundle("org.eclipse.gemini.blueprint.iandt", "proxy.creator").versionAsInProject(),
				mavenBundle("net.sourceforge.cglib", "com.springsource.net.sf.cglib", "2.2.0")
		);
	}

	@Test
	public void testNewProxiesCreatedOnBundleRefresh() throws Exception {
		// get a hold of the bundle proxy creator bundle and update it
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, PROXY_CREATOR_SYM_NAME);

		assertNotNull("proxy creator bundle not found", bundle);
		// update bundle (and thus create a new version of the classes)
		bundle.update();

		// make sure it starts-up
		try {
			waitOnContextCreation(PROXY_CREATOR_SYM_NAME, 60);
		}
		catch (Exception ex) {
			fail("updating the bundle failed");
		}
	}

	protected List<Permission> getTestPermissions() {
		List<Permission>  perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		return perms;
	}
}
