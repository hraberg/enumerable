package lambda.weaving;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;

import static lambda.exception.UncheckedException.*;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Type.*;

class AnnotationCache {
    Map<String, Boolean> elementsToHasAnnotation = new HashMap<String, Boolean>();
    Class<? extends Annotation> annotation;

    AnnotationCache(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    boolean hasAnnotation(String owner, String name, String desc) {
        try {
            String key = owner + "." + name + desc;
            if (elementsToHasAnnotation.containsKey(key))
                return elementsToHasAnnotation.get(key);
            AnnotationFinder finder = new AnnotationFinder(annotation, name, desc);
            new ClassReader(getObjectType(owner).getClassName()).accept(finder, SKIP_CODE | SKIP_DEBUG
                    | SKIP_FRAMES);
            elementsToHasAnnotation.put(key, finder.found);
            return finder.found;
        } catch (IOException e) {
            throw uncheck(e);
        }
    }
}