package org.enumerable.lambda.support.osgi;

import org.enumerable.lambda.weaving.Version;
import org.enumerable.lambda.weaving.LambdaLoader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.framework.wiring.BundleWiring;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import static org.enumerable.lambda.weaving.Debug.debug;

public class LambdaWeavingActivator implements BundleActivator, WeavingHook {
    private LambdaLoader loader;
    private ServiceRegistration weavingHook;

    public void start(BundleContext bundleContext) throws Exception {
        debug("[osgi] " + Version.getVersionString());

        loader = new LambdaLoader();
        weavingHook = bundleContext.registerService(WeavingHook.class, this, new Hashtable<String, Object>());
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (weavingHook != null) weavingHook.unregister();
    }

    public void weave(WovenClass wovenClass) {
        BundleWiring wiring = wovenClass.getBundleWiring();
        ByteArrayInputStream in = new ByteArrayInputStream(wovenClass.getBytes());
        byte[] newBytes = loader.transformClass(wiring.getClassLoader(), wovenClass.getClassName(), in);
        if (newBytes != null) wovenClass.setBytes(newBytes);
    }
}
