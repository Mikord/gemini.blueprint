package org.eclipse.gemini.blueprint.context.support;

import org.eclipse.gemini.blueprint.context.support.internal.classloader.ClassLoaderFactory;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourcePatternResolver;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.eclipse.gemini.blueprint.util.internal.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;

import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * @author Abdulin Ildar
 */
public class OsgiBeanDefinitionReader extends XmlBeanDefinitionReader implements ResourceLoader {

  private BundleContext bundleContext;

  /**
   * OSGi bundle - determined from the BundleContext
   */
  private Bundle bundle;

  private ClassLoader classLoader;

  /**
   * Internal pattern resolver. The parent one can't be used since it is being instantiated inside the constructor
   * when the bundle field is not initialized yet.
   */
  private ResourcePatternResolver osgiPatternResolver;

  public OsgiBeanDefinitionReader(BeanDefinitionRegistry registry) {
    super(registry);

    this.bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
    this.bundle = bundleContext.getBundle();
    this.classLoader = createBundleClassLoader(bundle);

    this.osgiPatternResolver = createResourcePatternResolver();

    final Object[] resolvers = new Object[2];

    final BundleContext ctx = bundleContext;

    if (System.getSecurityManager() != null) {
      AccessController.doPrivileged(new PrivilegedAction<Object>() {
        public Object run() {
          String filter = BundleUtils.createNamespaceFilter(ctx);
          resolvers[0] = createNamespaceHandlerResolver(ctx, filter, getClassLoader());
          resolvers[1] = createEntityResolver(ctx, filter, getClassLoader());
          return null;
        }
      });
    } else {
      String filter = BundleUtils.createNamespaceFilter(ctx);
      resolvers[0] = createNamespaceHandlerResolver(ctx, filter, getClassLoader());
      resolvers[1] = createEntityResolver(ctx, filter, getClassLoader());
    }

    setNamespaceHandlerResolver((NamespaceHandlerResolver) resolvers[0]);
    setEntityResolver((EntityResolver) resolvers[1]);
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
  }

  @Override
  public ResourceLoader getResourceLoader() {
    return this;
  }

  protected ResourcePatternResolver createResourcePatternResolver() {
    return new OsgiBundleResourcePatternResolver(getBundle());
  }

  protected Bundle getBundle() {
    return bundle;
  }

  @Override
  public Resource getResource(String location) {
    return (osgiPatternResolver != null ? osgiPatternResolver.getResource(location) : null);
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  private ClassLoader createBundleClassLoader(Bundle bundle) {
    return ClassLoaderFactory.getBundleClassLoaderFor(bundle);
  }

  /**
   * Creates a special OSGi namespace handler resolver that first searches the bundle class path falling back to the
   * namespace service published by Spring-DM. This allows embedded libraries that provide namespace handlers take
   * priority over namespace provided by other bundles.
   *
   * @param bundleContext     the OSGi context of which the resolver should be aware of
   * @param filter            OSGi service filter
   * @param bundleClassLoader classloader for creating the OSGi namespace resolver proxy
   * @return a OSGi aware namespace handler resolver
   */
  private NamespaceHandlerResolver createNamespaceHandlerResolver(BundleContext bundleContext, String filter,
                                                                  ClassLoader bundleClassLoader) {
    Assert.notNull(bundleContext, "bundleContext is required");
    // create local namespace resolver
    // we'll use the default resolver which uses the bundle local class-loader
    NamespaceHandlerResolver localNamespaceResolver = new DefaultNamespaceHandlerResolver(bundleClassLoader);

    // hook in OSGi namespace resolver
    NamespaceHandlerResolver osgiServiceNamespaceResolver =
        lookupNamespaceHandlerResolver(bundleContext, filter, localNamespaceResolver);

    DelegatedNamespaceHandlerResolver delegate = new DelegatedNamespaceHandlerResolver();
    delegate.addNamespaceHandler(localNamespaceResolver, "LocalNamespaceResolver for bundle "
        + OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle()));
    delegate.addNamespaceHandler(osgiServiceNamespaceResolver, "OSGi Service resolver");

    return delegate;
  }

  /**
   * Similar to {@link #createNamespaceHandlerResolver(BundleContext, String, ClassLoader)} , this method creates
   * a special OSGi entity resolver that considers the bundle class path first, falling back to the entity resolver
   * service provided by the Spring DM extender.
   *
   * @param bundleContext     the OSGi context of which the resolver should be aware of
   * @param filter            OSGi service filter
   * @param bundleClassLoader classloader for creating the OSGi namespace resolver proxy
   * @return a OSGi aware entity resolver
   */
  private EntityResolver createEntityResolver(BundleContext bundleContext, String filter,
                                              ClassLoader bundleClassLoader) {
    Assert.notNull(bundleContext, "bundleContext is required");
    // create local namespace resolver
    EntityResolver localEntityResolver = new DelegatingEntityResolver(bundleClassLoader);
    // hook in OSGi namespace resolver
    EntityResolver osgiServiceEntityResolver = lookupEntityResolver(bundleContext, filter, localEntityResolver);

    ChainedEntityResolver delegate = new ChainedEntityResolver();
    delegate.addEntityResolver(localEntityResolver, "LocalEntityResolver for bundle "
        + OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle()));

    // hook in OSGi namespace resolver
    delegate.addEntityResolver(osgiServiceEntityResolver, "OSGi Service resolver");

    return delegate;
  }

  private NamespaceHandlerResolver lookupNamespaceHandlerResolver(final BundleContext bundleContext, String filter,
                                                                  final Object fallbackObject) {
    return (NamespaceHandlerResolver) TrackingUtil.getService(new Class<?>[]{NamespaceHandlerResolver.class},
        filter, NamespaceHandlerResolver.class.getClassLoader(), bundleContext, fallbackObject);
  }

  private EntityResolver lookupEntityResolver(final BundleContext bundleContext, String filter,
                                              final Object fallbackObject) {
    return (EntityResolver) TrackingUtil.getService(new Class<?>[]{EntityResolver.class}, filter,
        EntityResolver.class.getClassLoader(), bundleContext, fallbackObject);
  }
}
