package org.enumerable.lambda.weaving;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.enumerable.lambda.exception.UncheckedException.uncheck;

public class Version {
    public static Properties buildProperties = new Properties();
    static {
        ClassLoader loader = Version.class.getClassLoader();
        InputStream in = loader.getResourceAsStream(Version.class.getName().toLowerCase().replace('.', '/')
                + ".properties");
        try {
            if (in != null)
                buildProperties.load(in);
        } catch (IOException e) {
            throw uncheck(e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException silent) {
            }
        }
    }

    public static String getVersion() {
        return (String) buildProperties.get("enumerable.version");
    }

    public static String getBuildDate() {
        return buildProperties.getProperty("enumerable.build.date");
    }

    public static String getGitCommit() {
        return buildProperties.getProperty("enumerable.git.commit");
    }

    public static String getVersionString() {
        return "Enumerable.java version " + getVersion() + " (built on " + getBuildDate() + " from "
                + getGitCommit() + ")";
    }
}
