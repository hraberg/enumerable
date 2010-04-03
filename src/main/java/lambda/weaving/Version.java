package lambda.weaving;

import static lambda.exception.UncheckedException.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lambda.enumerable.Enumerable;

public class Version {
    public static Properties buildProperties = new Properties();
    static {
        ClassLoader loader = Enumerable.class.getClassLoader();
        InputStream in = loader.getResourceAsStream(Version.class.getName().toLowerCase().replace('.', '/') + ".properties");
        try {
            if (in != null)
                buildProperties.load(in);
        } catch (IOException e) {
            throw uncheck(e);
        } finally {
            try {
                in.close();
            } catch (IOException slient) {
            }
        }
    }

    public static String getVersionString() {
        return "Enumerable.java version " + buildProperties.get("enumerable.version") + " (built on "
                + buildProperties.getProperty("enumerable.build.date") + " from "
                + buildProperties.getProperty("enumerable.git.commit") + ")";
    }
}
