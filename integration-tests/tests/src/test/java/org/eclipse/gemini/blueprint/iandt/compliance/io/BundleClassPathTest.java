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

package org.eclipse.gemini.blueprint.iandt.compliance.io;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarInputStream;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

/**
 * IO compliance test for bundles containing Bundle ClassPath entries.
 *
 * @author Costin Leau
 */
@RunWith(PaxExam.class)
@ContextConfiguration
public class BundleClassPathTest extends BaseIoTest {
    @Override
    protected boolean shouldWaitForSpringBundlesContextCreation() {
        return false;
    }


    // TODO: Here we need to postprocess the test-probe to include a bundle-classpath
//	protected Manifest getManifest() {
//		Manifest mf = super.getManifest();
//		// add bundle classpath
//		mf.getMainAttributes().putValue(Constants.BUNDLE_CLASSPATH,
//				".,bundleclasspath/folder,bundleclasspath/simple.jar");
//		return mf;
//	}

    protected String[] getBundleContentPattern() {
        String pkg = getClass().getPackage().getName().replace('.', '/').concat("/");
        String[] patterns =
                new String[]{BaseIntegrationTest.class.getName().replace('.', '/').concat(".class"), pkg + "**/*",
                        "bundleclasspath/**/*"};
        return patterns;
    }

    private Bundle bundle;
    private String classLocation;

    @Before
    public void onSetUp() throws Exception {
        bundle = bundleContext.getBundle();
        classLocation = BundleClassPathTest.class.getName().replace('.', '/') + ".class";
    }

    @Test @Ignore("still working it out")
    public void testGetResourceOnClassInsideBundle() throws Exception {
        assertNotNull(bundle.getResource(classLocation));
    }

    @Test @Ignore("still working it out")
    public void testGetResourceOnFileJustInsideBundle() throws Exception {
        assertNotNull(bundle.getResource("org/eclipse/gemini/blueprint/iandt/compliance/io/package.file"));
        assertNotNull(bundle.getResource("org/eclipse/gemini/blueprint/iandt/compliance/io/test.file"));
    }

    @Test @Ignore("still working it out")
    public void testGetResourceOnFileOnBundleClassPathAndBundleJar() throws Exception {
        assertNotNull(bundle.getResource("org/eclipse/gemini/blueprint/iandt/compliance/io/test.file"));
    }

    @Test @Ignore("still working it out")
    public void testGetResourceOnFileJustInsideFolderOnClassPath() throws Exception {
        assertNotNull(bundle.getResource("org/eclipse/gemini/blueprint/iandt/compliance/io/folder-test.file"));
    }

    @Test @Ignore("still working it out")
    public void testGetResourceOnFileJustInsideJarOnClassPath() throws Exception {
        assertNotNull(bundle.getResource("jar.file"));
    }

    @Test @Ignore("still working it out")
    public void testGetResourcesOnFilePresentMultipleTimesOnTheClassPathAndInsideTheBundle() throws Exception {
        //System.out.println("running test " + this.getName());
        Enumeration enm = bundle.getResources("org/eclipse/gemini/blueprint/iandt/compliance/io/test.file");
        int count = 0;
        while (enm.hasMoreElements()) {
            Object object = (Object) enm.nextElement();
            //System.out.println("found res " + object);
            count++;
        }
        assertEquals("not all resources are found", 3, count);
    }

    @Test @Ignore("still working it out")
    public void testFindEntriesOnFileJustInsideFolderOnClassPath() throws Exception {
        //System.out.println("running test" + this.getName());
        Enumeration enm =
                bundle.findEntries("org/eclipse/gemini/blueprint/iandt/compliance/io/", "folder-test.file", false);
        assertNull("findEntries doesn't work on bundle classpath entries", enm);
    }

    @Test @Ignore("still working it out")
    public void testFindEntriesOnFileJustInsideJarOnClassPath() throws Exception {
        //System.out.println("running test" + this.getName());
        Enumeration enm = bundle.findEntries("/", "jar.file", false);
        assertNull("findEntries doesn't work on bundle classpath entries", enm);
    }

    // disabled as it fails on the server for some reason (linux + equinox)
    // TODO: investigate
    @Test @Ignore("still working it out")
    public void tstFindEntriesOnFilePresentMultipleTimesOnTheClassPathAndInsideTheBundle() throws Exception {
        //System.out.println("running test" + this.getName());
        Enumeration enm = bundle.findEntries("org/eclipse/gemini/blueprint/iandt/compliance/io/", "test.file", false);
        int count = 0;
        while (enm.hasMoreElements()) {
            count++;
        }
        assertEquals("bundle only resources are found", 1, count);
    }

    @Test @Ignore("still working it out")
    public void testGetEntryOnFileJustInsideFolderOnClassPath() throws Exception {
        //System.out.println("running test" + this.getName());
        URL url = bundle.getEntry("org/eclipse/gemini/blueprint/iandt/compliance/io/folder-test.file");
        assertNull("findEntries doesn't work on bundle classpath entries", url);
    }

    @Test @Ignore("still working it out")
    public void testGetEntryOnFileJustInsideJarOnClassPath() throws Exception {
        //System.out.println("running test" + this.getName());
        URL url = bundle.getEntry("jar.file");
        assertNull("findEntries doesn't work on bundle classpath entries", url);
    }

    // fails on Felix + KF
    @Test @Ignore("still working it out")
    public void tstFindEntriesOnMetaInfEntryOnSystemBundle() throws Exception {
        Bundle sysBundle = bundleContext.getBundle(0);
        Enumeration enm = sysBundle.findEntries("/", "META-INF", false);
        assertNotNull("system bundle doesn't return META-INF", enm);
    }

    // fails on Felix + KF
    @Test @Ignore("still working it out")
    public void tstGetEntryOnMetaInfEntryOnSystemBundle() throws Exception {
        Bundle sysBundle = bundleContext.getBundle(0);
        URL url = sysBundle.getEntry("/META-INF");
        assertNotNull("system bundle doesn't consider META-INF on classpath", url);
    }

    // simple debugging test (no need to keep it running)
    @Test @Ignore("still working it out")
    public void tstConnectionToJarOnClassPath() throws Exception {
        URL url = bundle.getEntry("bundleclasspath/simple.jar");
        //System.out.println("jar url is " + url);
        URLConnection con = url.openConnection();
        //System.out.println(con);
        //System.out.println(con.getContentType());
        InputStream stream = con.getInputStream();
        JarInputStream jis = new JarInputStream(stream);
        //System.out.println(jis);
        //System.out.println(jis.available());
        //System.out.println(jis.getNextJarEntry());
        //System.out.println(jis.getNextJarEntry());
        //System.out.println(jis.getNextJarEntry());
        //System.out.println(jis.available());
        //System.out.println(jis.getNextJarEntry());
        //System.out.println(jis.available());
        jis.close();
    }

    // @Override
    // protected OsgiPlatform createPlatform() {
    // OsgiPlatform platform = super.createPlatform();
    // platform.getConfigurationProperties().setProperty("org.knopflerfish.framework.debug.framework", "true");
    // platform.getConfigurationProperties().setProperty("org.knopflerfish.framework.debug.errors", "true");
    // platform.getConfigurationProperties().setProperty("org.knopflerfish.framework.debug.classloader", "true");
    // platform.getConfigurationProperties().setProperty("org.knopflerfish.framework.debug.packages", "true");
    //
    // return platform;
    // }


}