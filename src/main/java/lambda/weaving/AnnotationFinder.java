package lambda.weaving;

import static org.objectweb.asm.Type.*;

import java.lang.annotation.Annotation;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

class AnnotationFinder extends EmptyVisitor {
    String name;
    String desc;
    boolean found;
    Class<? extends Annotation> annotation;

    AnnotationFinder(Class<? extends Annotation> annotation, String name, String desc) {
        this.annotation = annotation;
        this.name = name;
        this.desc = desc;
    }

    public FieldVisitor visitField(int access, final String name, String
            desc, String signature, Object value) {
        if (this.name.equals(name))
            return this;
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String
            desc, String signature, String[] exceptions) {
        if (this.name.equals(name) && this.desc.equals(desc))
            return this;
        return null;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (annotation.getName().equals(getType(desc).getClassName()))
            found = true;
        return null;
    }
}