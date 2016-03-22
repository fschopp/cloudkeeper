package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.bare.element.annotation.BareAnnotation;
import xyz.cloudkeeper.model.bare.element.annotation.BareAnnotationTypeDeclaration;
import xyz.cloudkeeper.model.immutable.element.Name;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.annotation.RuntimeAnnotation;
import xyz.cloudkeeper.model.util.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static xyz.cloudkeeper.model.immutable.element.SimpleName.identifier;

final class AnnotationImpl extends LocatableImpl implements RuntimeAnnotation {
    private final NameReference declarationReference;
    private final Map<SimpleName, AnnotationEntryImpl> entriesMap;

    @Nullable private volatile AnnotationTypeDeclarationImpl annotationTypeDeclaration;
    @Nullable private volatile Annotation javaAnnotation;

    AnnotationImpl(BareAnnotation original, CopyContext parentContext) throws LinkerException {
        super(original, parentContext);

        CopyContext context = getCopyContext();
        declarationReference
            = new NameReference(original.getDeclaration(), context.newContextForProperty("declaration"));
        entriesMap = unmodifiableMapOf(original.getEntries(), "entries", AnnotationEntryImpl::new,
            AnnotationEntryImpl::getKeyName);
    }

    Name getKey() {
        return declarationReference.getQualifiedName();
    }

    @Override
    @Nonnull
    public AnnotationTypeDeclarationImpl getDeclaration() {
        require(State.LINKED);
        @Nullable AnnotationTypeDeclarationImpl localDeclaration = annotationTypeDeclaration;
        assert localDeclaration != null : "must be non-null when in state " + State.LINKED;
        return localDeclaration;
    }

    @Override
    public ImmutableList<AnnotationEntryImpl> getEntries() {
        return ImmutableList.copyOf(entriesMap.values());
    }

    @Override
    @Nullable
    public AnnotationValueImpl getValue(SimpleName name) {
        require(State.LINKED);
        @Nullable AnnotationTypeDeclarationImpl localDeclaration = annotationTypeDeclaration;
        assert localDeclaration != null : "must be non-null when in state " + State.LINKED;

        @Nullable AnnotationEntryImpl entry = entriesMap.get(name);
        @Nullable AnnotationValueImpl value;
        if (entry == null) {
            @Nullable AnnotationTypeElementImpl annotationTypeElement
                = localDeclaration.getEnclosedElement(AnnotationTypeElementImpl.class, name);
            value = annotationTypeElement == null
                ? null
                : annotationTypeElement.getDefaultValue();
        } else {
            value = entry.getValue();
        }
        return value;
    }

    @Override
    public String toString() {
        return Default.toString(this);
    }

    @Override
    public <T extends Annotation> T getJavaAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        require(State.LINKED);
        @Nullable AnnotationTypeDeclarationImpl localDeclaration = annotationTypeDeclaration;
        assert localDeclaration != null : "must be non-null when in state " + State.LINKED;

        Name expectedName = localDeclaration.getQualifiedName();
        if (!annotationClass.getName().contentEquals(expectedName)) {
            throw new IllegalArgumentException(String.format(
                "Expected class with name '%s', but got %s.", expectedName, annotationClass
            ));
        }

        // §17.7 JLS specifies: Writes to and reads of references are always atomic. We can therefore live without
        // synchronization. Note, however, that the local variable *is* necessary.
        @Nullable Annotation localAnnotation = javaAnnotation;
        if (localAnnotation == null || !localAnnotation.getClass().equals(annotationClass)) {
            localAnnotation = (Annotation) Proxy.newProxyInstance(
                annotationClass.getClassLoader(),
                new Class<?>[] {annotationClass},
                new AnnotationInvocationHandler(annotationClass)
            );
            javaAnnotation = localAnnotation;
        }

        @SuppressWarnings("unchecked")
        T typedAnnotation = (T) localAnnotation;
        return typedAnnotation;
    }

    @Override
    void collectEnclosed(Collection<AbstractFreezable> freezables) {
        freezables.addAll(entriesMap.values());
    }

    @Override
    void preProcessFreezable(FinishContext context) throws LinkerException {
        annotationTypeDeclaration = context.getDeclaration(
            BareAnnotationTypeDeclaration.NAME, AnnotationTypeDeclarationImpl.class, declarationReference);
    }

    @Override
    void augmentFreezable(FinishContext context) { }

    @Override
    void finishFreezable(FinishContext context) { }

    @Override
    void verifyFreezable(VerifyContext context) throws LinkerException {
        @Nullable AnnotationTypeDeclarationImpl localDeclaration = annotationTypeDeclaration;
        assert localDeclaration != null : "must be non-null when in state " + State.LINKED;

        for (AnnotationTypeElementImpl element: localDeclaration.getElements()) {
            Preconditions.requireCondition(
                element.getDefaultValue() != null || entriesMap.get(element.getSimpleName()) != null,
                getCopyContext(),
                "Missing value for annotation element '%s', which does not have a default value.",
                element.getSimpleName()
            );
        }
    }

    private final class AnnotationInvocationHandler implements InvocationHandler {
        private final Class<? extends Annotation> annotationClass;

        AnnotationInvocationHandler(Class<? extends Annotation> annotationClass) {
            this.annotationClass = annotationClass;
        }

        private boolean isProxyEqualToAnnotation(Object proxy, Annotation other) {
            if (proxy == other) {
                return true;
            } else if (!annotationClass.equals(other.annotationType())) {
                return false;
            }

            try {
                for (Method method: annotationClass.getDeclaredMethods()) {
                    if (method.getParameterTypes().length == 0) {
                        @Nullable AnnotationValueImpl ownValue = getValue(identifier(method.getName()));
                        if (ownValue != null) {
                            // If ownValue == null, then the method does not actually refer to an annotation element,
                            // but is some other interface method.
                            final Object otherElementValue = method.invoke(other);
                            if (!AnnotationValueImpl.isEqual(otherElementValue, ownValue.toNativeValue())) {
                                return false;
                            }
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return false;
            }
            return true;
        }

        private int proxyHashCode() {
            int hashCode = 0;
            for (AnnotationEntryImpl element: entriesMap.values()) {
                hashCode += element.hashCode();
            }
            return hashCode;
        }

        @Override
        public Object invoke(Object proxy, Method method, @Nullable Object[] args) {
            final String name = method.getName();
            final int numArguments = method.getParameterTypes().length;

            if ("equals".equals(name) && numArguments == 1) {
                assert args != null;
                return isProxyEqualToAnnotation(proxy, (Annotation) args[0]);
            } else if ("annotationType".equals(name) && numArguments == 0) {
                return annotationClass;
            } else if ("hashCode".equals(name) && numArguments == 0) {
                return proxyHashCode();
            } else if ("toString".equals(name) && numArguments == 0) {
                return AnnotationImpl.this.toString();
            } else {
                @Nullable AnnotationValueImpl value = getValue(identifier(method.getName()));
                if (value == null) {
                    throw new IllegalStateException(String.format(
                        "Invocation of unexpected method %s. Not one of equals(), annotationType(), hashCode(), "
                            + "toString(), or a declared annotation type element.", method));
                }
                return value.toNativeValue();
            }
        }
    }
}
