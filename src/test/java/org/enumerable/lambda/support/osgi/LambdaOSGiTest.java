package org.enumerable.lambda.support.osgi;

import org.enumerable.lambda.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

import static org.enumerable.lambda.Lambda.λ;
import static org.junit.Assert.assertEquals;

public class LambdaOSGiTest implements BundleActivator {
    public void start(BundleContext context) throws Exception {
        methodInsideOSGiContainerToBeWovenByWeavingHook(context);
    }

    private void methodInsideOSGiContainerToBeWovenByWeavingHook(BundleContext context) {
        context.registerService(Callable.class, λ("Hello OSGi World").as(Callable.class), new Hashtable<String, Object>());
    }

    public void stop(BundleContext context) throws Exception {
    }

    @Test
    public void startAndWeaveTestBundle() throws Exception {
        installAndStart("target/enumerable-java-" + Version.getVersion() + ".jar");
        installAndStart("target/enumerable-java-" + Version.getVersion() + "-test.jar");

        assertEquals("Hello OSGi World", service(Callable.class).call());
    }

    BundleContext context;
    Framework framework;

    @Before
    public void startOSGi() throws BundleException {
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        framework = loader.iterator().next().newFramework(new HashMap<String, String>() {{
            put("org.osgi.framework.storage", new File("target/org.osgi.framework.storage").getAbsolutePath());
            put("org.osgi.framework.storage.clean", "onFirstInit");
        }});
        framework.start();
        context = framework.getBundleContext();
    }
    @After
    public void stopOSGi() throws BundleException {
        if (framework != null) framework.stop();
    }

    @SuppressWarnings({"unchecked"})
    private <T> T service(Class<T> aClass) {
        return (T) context.getService(context.getServiceReference(aClass));
    }

    private void installAndStart(String bundle) throws BundleException {
        context.installBundle(new File(bundle).toURI().toString()).start();
    }
}
