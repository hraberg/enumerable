package org.enumerable.lambda.support.osgi;

import org.enumerable.lambda.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

import static org.enumerable.lambda.Lambda.λ;
import static org.junit.Assert.assertEquals;
import static org.osgi.framework.Constants.*;

@RunWith(Parameterized.class)
public class LambdaOSGiTest {
    public static class TestActivator implements BundleActivator {
        public void start(BundleContext context) throws Exception {
            methodInsideOSGiContainerToBeWovenByWeavingHook(context);
        }

        private void methodInsideOSGiContainerToBeWovenByWeavingHook(BundleContext context) {
            context.registerService(Callable.class, λ("Hello OSGi World from " + context.getProperty(FRAMEWORK_VENDOR)).as(Callable.class), new Hashtable<String, Object>());
        }

        public void stop(BundleContext context) throws Exception {
        }
    }

    @Test
    public void startAndWeaveTestBundle() throws Exception {
        installAndStart("target/enumerable-java-" + Version.getVersion() + ".jar");
        installAndStart("target/enumerable-java-" + Version.getVersion() + "-test.jar");

        assertEquals("Hello OSGi World from " + context.getProperty(FRAMEWORK_VENDOR), service(Callable.class).call());
    }

    @Parameterized.Parameters
    public static List<FrameworkFactory[]> frameworks() {
        List<FrameworkFactory[]> result = new ArrayList<FrameworkFactory[]>();
        for (FrameworkFactory frameworkFactory : ServiceLoader.load(FrameworkFactory.class))
            result.add(new FrameworkFactory[] {frameworkFactory});
        return result;
    }

    public LambdaOSGiTest(FrameworkFactory frameworkFactory) {
        this.framework = frameworkFactory.newFramework(new HashMap<String, String>() {{
            put(FRAMEWORK_STORAGE, new File("target", FRAMEWORK_STORAGE).getAbsolutePath());
            put(FRAMEWORK_STORAGE_CLEAN, FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }});
    }

    @Before
    public void startOSGi() throws BundleException {
        framework.start();
        context = framework.getBundleContext();
    }
    @After
    public void stopOSGi() throws BundleException {
        if (framework != null) framework.stop();
    }

    @SuppressWarnings({"unchecked"})
    private <T> T service(Class<T> aClass) {
        return context.getService(context.getServiceReference(aClass));
    }

    private void installAndStart(String bundle) throws BundleException {
        context.installBundle(new File(bundle).toURI().toString()).start();
    }

    BundleContext context;
    Framework framework;
}
