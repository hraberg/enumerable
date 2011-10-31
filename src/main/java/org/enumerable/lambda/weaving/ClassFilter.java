package org.enumerable.lambda.weaving;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.System.getProperty;

/**
 * Filters the classes to be instrumented, based on a blacklist or a white list
 * and a regexp pattern.
 */
public class ClassFilter {
    @SuppressWarnings("serial")
    private Set<String> packagesToSkip = new HashSet<String>() {
        {
            add("java.");
            add("javax.");
            add("sun.");
            add("$Proxy");
            add("org.eclipse.jdt.internal.");
            add("org.junit.");
            add("junit.");
            add("com.sun.");
            add("clojure.");
            add("org.jruby.");
            add("org.codehaus.groovy.");
            add("org.osgi.");
            add("org.enumerable.lambda.weaving.");
            add("org.enumerable.lambda.asm.");
            add("groovy.");
            add("scala.");
            add("Script");
            add("org.enumerable.lambda.enumerable.jruby.");
            add("Enumerable");
        }
    };

    private Set<String> packagesToInclude = new HashSet<String>();
    private Pattern excludePattern;

    public ClassFilter(String skippedPackages, String includedPackages, String excludePatternString) {
        addSkippedPackages(skippedPackages);
        addIncludedPackages(includedPackages);
        if (!"".equals(excludePatternString.trim())) {
            this.excludePattern = Pattern.compile(excludePatternString);
        }
    }

    public static ClassFilter createClassFilter() {
        return new ClassFilter(getProperty("lambda.weaving.skipped.packages", ""),
                        getProperty("lambda.weaving.included.packages", ""),
                        getProperty("lambda.weaving.exclude.pattern", ""));
    }

    private void addIncludedPackages(String agentArgs) {
        for (String prefix : agentArgs.split(",")) {
            String trim = prefix.trim();
            if (trim.length() > 0)
                packagesToInclude.add(trim);
        }
    }

    private void addSkippedPackages(String agentArgs) {
        for (String prefix : agentArgs.split(",")) {
            String trim = prefix.trim();
            if (trim.length() > 0)
                packagesToSkip.add(trim);
        }
    }

    public boolean isToBeInstrumented(String name) {
        return packageIncluded(name) && (excludePattern == null || !excludePattern.matcher(name).find());
    }

    private boolean packageIncluded(String name) {
        if (packagesToInclude.size() > 0) {
            for (String prefix : packagesToInclude)
                if (name.startsWith(prefix))
                    return true;
            return false;
        }

        for (String prefix : packagesToSkip)
            if (name.startsWith(prefix))
                return false;
        return true;
    }
}
