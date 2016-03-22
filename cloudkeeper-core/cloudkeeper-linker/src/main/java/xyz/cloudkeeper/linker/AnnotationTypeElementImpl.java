package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.bare.element.annotation.BareAnnotationTypeElement;
import xyz.cloudkeeper.model.bare.element.annotation.BareAnnotationValue;
import xyz.cloudkeeper.model.beans.type.MutableTypeMirror;
import xyz.cloudkeeper.model.immutable.element.Name;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.RuntimeElement;
import xyz.cloudkeeper.model.runtime.element.annotation.RuntimeAnnotationTypeElement;
import xyz.cloudkeeper.model.util.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collection;

final class AnnotationTypeElementImpl
        extends AnnotatedConstructImpl
        implements RuntimeAnnotationTypeElement, IElementImpl {
    private final SimpleName simpleName;
    private final TypeMirrorImpl returnType;
    @Nullable private final TypeMirrorImpl typeOfDefaultValue;
    @Nullable private final AnnotationValueImpl defaultValue;

    @Nullable private volatile AnnotationTypeDeclarationImpl enclosingElement;
    @Nullable private volatile Name qualifiedName;

    AnnotationTypeElementImpl(BareAnnotationTypeElement original, CopyContext parentContext) throws LinkerException {
        super(original, parentContext);

        CopyContext context = getCopyContext();
        simpleName
            = Preconditions.requireNonNull(original.getSimpleName(), context.newContextForProperty("simpleName"));

        // defaultValue may be null.
        @Nullable BareAnnotationValue originalDefaultValue = original.getDefaultValue();
        defaultValue = originalDefaultValue == null
            ? null
            : new AnnotationValueImpl(originalDefaultValue, context.newContextForProperty("defaultValue"));
        returnType
            = TypeMirrorImpl.copyOf(original.getReturnType(), context.newContextForProperty("returnType"));

        if (defaultValue != null) {
            MutableTypeMirror<?> mutableType
                = MutableTypeMirror.fromJavaType(unbox(defaultValue.toNativeValue().getClass()));
            typeOfDefaultValue = TypeMirrorImpl.optionalCopyOf(mutableType, context);
        } else {
            typeOfDefaultValue = null;
        }
    }

    private static Class<?> unbox(Class<?> clazz) {
        if (Double.class.equals(clazz)) {
            return double.class;
        } else if (Float.class.equals(clazz)) {
            return float.class;
        } else if (Long.class.equals(clazz)) {
            return long.class;
        } else if (Integer.class.equals(clazz)) {
            return int.class;
        } else if (Short.class.equals(clazz)) {
            return short.class;
        } else if (Byte.class.equals(clazz)) {
            return byte.class;
        } else if (Character.class.equals(clazz)) {
            return char.class;
        } else if (Boolean.class.equals(clazz)) {
            return boolean.class;
        } else {
            return clazz;
        }
    }

    @Override
    public String toString() {
        return BareAnnotationTypeElement.Default.toHumanReadableString(this);
    }

    @Override
    public IElementImpl getSuperAnnotatedConstruct() {
        return null;
    }

    @Override
    public AnnotationTypeDeclarationImpl getEnclosingElement() {
        require(State.LINKED);
        @Nullable AnnotationTypeDeclarationImpl localEnclosingElement = enclosingElement;
        assert localEnclosingElement != null;
        return localEnclosingElement;
    }

    @Override
    public <T extends RuntimeElement> T getEnclosedElement(Class<T> clazz, SimpleName simpleName) {
        return null;
    }

    @Override
    public ImmutableList<? extends IElementImpl> getEnclosedElements() {
        return ImmutableList.of();
    }

    @Override
    public Name getQualifiedName() {
        require(State.LINKED);
        @Nullable Name localName = qualifiedName;
        assert localName != null;
        return localName;
    }

    @Override
    public SimpleName getSimpleName() {
        return simpleName;
    }

    @Override
    public TypeMirrorImpl getReturnType() {
        return returnType;
    }

    @Override
    @Nullable
    public AnnotationValueImpl getDefaultValue() {
        return defaultValue;
    }

    @Override
    void collectEnclosedByAnnotatedConstruct(Collection<AbstractFreezable> freezables) {
        freezables.add(returnType);
        if (typeOfDefaultValue != null) {
            freezables.add(typeOfDefaultValue);
        }
    }

    @Override
    void preProcessFreezable(FinishContext context) {
        AnnotationTypeDeclarationImpl localEnclosingElement
            = context.getRequiredEnclosingFreezable(AnnotationTypeDeclarationImpl.class);
        enclosingElement = localEnclosingElement;
        qualifiedName = localEnclosingElement.getQualifiedName().join(simpleName);
    }

    @Override
    void augmentFreezable(FinishContext context) { }

    @Override
    void finishFreezable(FinishContext context) { }

    @Override
    void verifyFreezable(VerifyContext context) throws LinkerException {
        if (typeOfDefaultValue != null) {
            Preconditions.requireCondition(
                returnType.equals(typeOfDefaultValue),
                getCopyContext(),
                "Explicit return type of annotation element and type of default value do not match."
            );
        }
    }
}
