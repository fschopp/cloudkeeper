package xyz.cloudkeeper.dsl;

import xyz.cloudkeeper.model.Immutable;
import xyz.cloudkeeper.model.bare.element.BarePluginDeclarationVisitor;
import xyz.cloudkeeper.model.bare.element.annotation.BareAnnotation;
import xyz.cloudkeeper.model.bare.element.annotation.BareAnnotationTypeDeclaration;
import xyz.cloudkeeper.model.bare.element.annotation.BareAnnotationTypeElement;
import xyz.cloudkeeper.model.beans.element.annotation.MutableAnnotationTypeDeclaration;
import xyz.cloudkeeper.model.beans.element.annotation.MutableAnnotationTypeElement;
import xyz.cloudkeeper.model.immutable.Location;
import xyz.cloudkeeper.model.immutable.element.SimpleName;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

final class DSLAnnotationTypeDeclaration implements BareAnnotationTypeDeclaration, Immutable {
    private final SimpleName simpleName;
    private final List<DSLAnnotation> annotations;
    private final MutableAnnotationTypeDeclaration mutableAnnotationTypeDeclaration;

    DSLAnnotationTypeDeclaration(Class<? extends Annotation> annotationType) {
        simpleName = Shared.simpleNameOfClass(annotationType);
        annotations = DSLAnnotation.unmodifiableAnnotationList(annotationType);
        mutableAnnotationTypeDeclaration
            = MutableAnnotationTypeDeclaration.fromClass(annotationType, DSLNestedNameCopyOption.INSTANCE);
    }

    @Override
    public String toString() {
        return Default.toString(this);
    }

    @Override
    public <T, P> T accept(BarePluginDeclarationVisitor<T, P> visitor, P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public SimpleName getSimpleName() {
        return simpleName;
    }

    @Override
    public List<? extends BareAnnotationTypeElement> getElements() {
        // Defensive copy
        List<MutableAnnotationTypeElement> elements = mutableAnnotationTypeDeclaration.getElements();
        List<MutableAnnotationTypeElement> copiedElements = new ArrayList<>(elements.size());
        for (MutableAnnotationTypeElement element: elements) {
            copiedElements.add(MutableAnnotationTypeElement.copyOf(element));
        }
        return copiedElements;
    }

    @Override
    public List<? extends BareAnnotation> getDeclaredAnnotations() {
        return annotations;
    }
}
