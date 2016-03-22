package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.bare.element.module.BareInputModule;
import xyz.cloudkeeper.model.bare.element.module.BareModuleVisitor;
import xyz.cloudkeeper.model.bare.element.serialization.BareSerializationRoot;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.RuntimeElement;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeInputModule;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeModuleVisitor;
import xyz.cloudkeeper.model.runtime.type.RuntimeTypeMirror;
import xyz.cloudkeeper.model.util.ImmutableList;

import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * An input module is specified by an object-store key or content specified inline.
 * It has no in-ports and a single out-port named "value", through which it provides the content it encloses.
 */
final class InputModuleImpl extends ModuleImpl implements RuntimeInputModule {
    private final TypeMirrorImpl outPortType;
    private final PortImpl.OutPortImpl outPort;

    private final boolean rawLifecycleReponsible;
    @Nullable private volatile SerializationRootImpl raw;
    @Nullable private volatile Object value;

    InputModuleImpl(BareInputModule original, CopyContext parentContext, int index) throws LinkerException {
        super(original, parentContext, index);

        CopyContext context = getCopyContext();
        outPortType = TypeMirrorImpl.copyOf(original.getOutPortType(), context.newContextForProperty("outPortType"));
        outPort = new PortImpl.OutPortImpl(context.newSystemContext("implicit out-port"),
            SimpleName.identifier(OUT_PORT_NAME), outPortType, 0, 0);

        value = original.getValue();

        @Nullable BareSerializationRoot originalRaw = original.getRaw();
        raw = originalRaw == null
            ? null
            : new SerializationRootImpl(originalRaw, context.newContextForProperty("raw"));
        rawLifecycleReponsible = raw != null;
    }

    @Override
    public String toString() {
        return BareInputModule.Default.toString(this);
    }

    @Override
    @Nullable
    public <T, P> T accept(BareModuleVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visitInputModule(this, parameter);
    }

    @Override
    @Nullable
    public <T, P> T accept(RuntimeModuleVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visit(this, parameter);
    }

    /**
     * {@inheritDoc}
     *
     * An input module does not inherit any annotations.
     */
    @Override
    public IElementImpl getSuperAnnotatedConstruct() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * An input module only encloses its out-port.
     */
    @Override
    public ImmutableList<PortImpl.OutPortImpl> getEnclosedElements() {
        return ImmutableList.of(outPort);
    }

    @Override
    @Nullable
    public <T extends RuntimeElement> T getEnclosedElement(Class<T> clazz, SimpleName simpleName) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(simpleName);

        if (simpleName.contentEquals(BareInputModule.OUT_PORT_NAME) && clazz.isInstance(outPort)) {
            @SuppressWarnings("unchecked")
            T typedOutPort = (T) outPort;
            return typedOutPort;
        } else {
            return null;
        }
    }

    @Override
    public ImmutableList<PortImpl> getPorts() {
        return ImmutableList.of(outPort);
    }

    @Override
    public ImmutableList<IInPortImpl> getInPorts() {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<IOutPortImpl> getOutPorts() {
        return ImmutableList.of(outPort);
    }

    @Override
    public RuntimeTypeMirror getOutPortType() {
        return outPort.getType();
    }

    @Override
    @Nullable
    public Object getValue() {
        require(State.PRECOMPUTED);
        return value;
    }

    @Override
    @Nullable
    public SerializationRootImpl getRaw() {
        require(State.PRECOMPUTED);
        return raw;
    }

    @Override
    public InputModuleImpl resolveInvocations() {
        return this;
    }

    @Override
    void collectEnclosedByAnnotatedConstruct(Collection<AbstractFreezable> freezables) {
        freezables.add(outPortType);
        freezables.add(outPort);
        if (rawLifecycleReponsible) {
            freezables.add(raw);
        }
    }

    @Override
    void preProcessModule(FinishContext context) { }

    @Override
    void augmentFreezable(FinishContext context) { }

    @Override
    void verifyModule(VerifyContext context) throws LinkerException {
        CopyContext copyContext = getCopyContext();
        Preconditions.requireCondition(value != null || raw != null, copyContext,
            "Expected either value or raw to be non-null");

        if (value == null) {
            @Nullable SerializationRootImpl localRaw = raw;
            assert localRaw != null;
            value = context.valueFromSerializationTree(localRaw);
        } else if (raw == null) {
            assert value != null;
            raw = context.serializationTreeFromValue(this);
        }
    }
}
