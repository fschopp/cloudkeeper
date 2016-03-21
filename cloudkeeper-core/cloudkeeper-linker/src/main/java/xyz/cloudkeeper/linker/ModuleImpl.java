package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.bare.element.module.BareCompositeModule;
import xyz.cloudkeeper.model.bare.element.module.BareInputModule;
import xyz.cloudkeeper.model.bare.element.module.BareInvokeModule;
import xyz.cloudkeeper.model.bare.element.module.BareLoopModule;
import xyz.cloudkeeper.model.bare.element.module.BareModule;
import xyz.cloudkeeper.model.bare.element.module.BareModuleDeclaration;
import xyz.cloudkeeper.model.bare.element.module.BareModuleVisitor;
import xyz.cloudkeeper.model.bare.element.module.BareSimpleModule;
import xyz.cloudkeeper.model.immutable.element.Name;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeModule;
import xyz.cloudkeeper.model.runtime.element.module.TypeRelationship;
import xyz.cloudkeeper.model.util.ImmutableList;

import javax.annotation.Nullable;

/**
 * Abstract module instance.
 */
abstract class ModuleImpl extends AnnotatedConstructImpl implements RuntimeModule, IElementImpl {
    @Nullable private final SimpleName name;
    private final int index;

    @Nullable private volatile IElementImpl enclosingElement;
    @Nullable private volatile Name qualifiedName;

    ModuleImpl(@Nullable BareModule original, CopyContext parentContext, int index) throws LinkerException {
        super(original, parentContext);
        assert original != null;
        name = original.getSimpleName();
        this.index = index;
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(Object otherObject) {
        return this == otherObject;
    }

    @Override
    public abstract String toString();

    private static final class CopyVisitor implements BareModuleVisitor<Try<? extends ModuleImpl>, CopyContext> {
        private final int index;

        private CopyVisitor(int index) {
            this.index = index;
        }

        @Override
        @Nullable
        public Try<InputModuleImpl> visitInputModule(BareInputModule original, @Nullable CopyContext parentContext) {
            assert parentContext != null;
            return Try.run(() -> new InputModuleImpl(original, parentContext, index));
        }

        @Override
        @Nullable
        public Try<CompositeModuleImpl> visitCompositeModule(
                BareCompositeModule original, @Nullable CopyContext parentContext) {
            assert parentContext != null;
            return Try.run(() -> new CompositeModuleImpl(original, parentContext, index));
        }

        @Override
        @Nullable
        public Try<LoopModuleImpl> visitLoopModule(BareLoopModule original, @Nullable CopyContext parentContext) {
            assert parentContext != null;
            return Try.run(() -> new LoopModuleImpl(original, parentContext, index));
        }

        @Override
        @Nullable
        public Try<InvokeModuleImpl> visitLinkedModule(BareInvokeModule linkedModule,
                @Nullable CopyContext parentContext) {
            assert parentContext != null;
            return Try.run(() -> new InvokeModuleImpl(linkedModule, parentContext, index));
        }

        @Nullable
        @Override
        public Try<? extends ModuleImpl> visitSimpleModule(BareSimpleModule simpleModule,
                @Nullable CopyContext parentContext) {
            assert parentContext != null;
            return Try.run(() -> new SimpleModuleImpl(simpleModule, parentContext, index));
        }
    }

    static ModuleImpl copyOf(@Nullable BareModule original, CopyContext parentContext, int index)
            throws LinkerException {
        Preconditions.requireNonNull(original, parentContext);
        @Nullable Try<? extends ModuleImpl> copyTry = original.accept(new CopyVisitor(index), parentContext);
        assert copyTry != null;
        return copyTry.get();
    }

    @Override
    public abstract ImmutableList<PortImpl> getPorts();

    @Override
    public abstract ImmutableList<IInPortImpl> getInPorts();

    @Override
    public abstract ImmutableList<IOutPortImpl> getOutPorts();

    @Override
    @Nullable
    public IElementImpl getEnclosingElement() {
        require(State.FINISHED);
        return enclosingElement;
    }

    @Override
    public Name getQualifiedName() {
        require(State.FINISHED);
        @Nullable Name localQualifiedName = qualifiedName;
        assert localQualifiedName != null : "must be non-null when finished";
        return localQualifiedName;
    }

    @Override
    @Nullable
    public final SimpleName getSimpleName() {
        return name;
    }

    @Override
    public final ParentModuleImpl getParent() {
        require(State.FINISHED);
        @Nullable IElementImpl localEnclosingElement = enclosingElement;
        return localEnclosingElement instanceof ParentModuleImpl
            ? (ParentModuleImpl) localEnclosingElement
            : null;
    }

    @Override
    public final int getIndex() {
        return index;
    }

    @Override
    @Nullable
    public final ConnectionImpl getApplyToAllConnection() {
        for (IInPortImpl inPort: getInPorts()) {
            for (ConnectionImpl connection: inPort.getInConnections()) {
                if (connection.getTypeRelationship() == TypeRelationship.APPLY_TO_ALL) {
                    return connection;
                }
            }
        }
        return null;
    }

    @Override
    final void finishFreezable(FinishContext context) throws LinkerException {
        @Nullable IElementImpl localEnclosingElement
            = context.getOptionalEnclosingFreezable(IElementImpl.class).orElse(null);
        enclosingElement = localEnclosingElement;
        Preconditions.requireCondition((name != null) == (enclosingElement instanceof ParentModuleImpl),
            getCopyContext(),
            "Expected non-empty module name if and only if module has an enclosing parent module."
        );
        if (localEnclosingElement instanceof ParentModuleImpl) {
            assert name != null;
            qualifiedName = localEnclosingElement.getQualifiedName().join(name);
        } else if (localEnclosingElement instanceof ModuleDeclarationImpl) {
            qualifiedName = localEnclosingElement.getQualifiedName()
                .join(SimpleName.identifier(BareModuleDeclaration.TEMPLATE_ELEMENT_NAME));
        } else {
            qualifiedName = SimpleName.identifier(BareModuleDeclaration.TEMPLATE_ELEMENT_NAME);
        }
        finishModule(context);
    }

    abstract void finishModule(FinishContext context) throws LinkerException;
}
