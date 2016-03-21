package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.api.Executable;
import xyz.cloudkeeper.model.bare.element.module.BareModuleVisitor;
import xyz.cloudkeeper.model.bare.element.module.BarePort;
import xyz.cloudkeeper.model.bare.element.module.BareSimpleModule;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.RuntimeElement;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeModuleVisitor;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeSimpleModule;
import xyz.cloudkeeper.model.util.ImmutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

final class SimpleModuleImpl extends ModuleImpl implements RuntimeSimpleModule {
    private final Map<SimpleName, PortImpl> declaredPortsMap;
    private final ImmutableList<PortImpl> declaredPorts;
    private final ImmutableList<IInPortImpl> inPorts;
    private final ImmutableList<IOutPortImpl> outPorts;
    @Nullable private volatile Executable executable;

    SimpleModuleImpl(BareSimpleModule original, CopyContext parentContext, int index) throws LinkerException {
        super(original, parentContext, index);

        List<? extends BarePort> originalPorts = original.getDeclaredPorts();

        PortAccumulationState state = new PortAccumulationState();
        Map<SimpleName, PortImpl> newPortMap = new LinkedHashMap<>();
        collect(
            originalPorts,
            "declaredPorts",
            portConstructor(state),
            Arrays.asList(
                portAccumulator(state),
                mapAccumulator(newPortMap, PortImpl::getSimpleName)
            )
        );
        declaredPortsMap = Collections.unmodifiableMap(newPortMap);
        declaredPorts = ImmutableList.copyOf(newPortMap.values());
        inPorts = ImmutableList.copyOf(state.getInPorts());
        outPorts = ImmutableList.copyOf(state.getOutPorts());
    }

    @Override
    public String toString() {
        return BareSimpleModule.Default.toString(this);
    }

    /**
     * {@inheritDoc}
     *
     * A simple module does not inherit any annotations.
     */
    @Override
    public IElementImpl getSuperAnnotatedConstruct() {
        return null;
    }

    @Override
    @Nullable
    public <T, P> T accept(BareModuleVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visitSimpleModule(this, parameter);
    }

    @Override
    @Nullable
    public <T, P> T accept(RuntimeModuleVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    public ImmutableList<PortImpl> getEnclosedElements() {
        return declaredPorts;
    }

    @Override
    @Nullable
    public <T extends RuntimeElement> T getEnclosedElement(Class<T> clazz, SimpleName simpleName) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(simpleName);

        PortImpl port = declaredPortsMap.get(simpleName);
        if (clazz.isInstance(port)) {
            @SuppressWarnings("unchecked")
            T typedElement = (T) port;
            return typedElement;
        } else {
            return null;
        }
    }

    @Override
    public ImmutableList<PortImpl> getDeclaredPorts() {
        return declaredPorts;
    }

    @Override
    public ImmutableList<PortImpl> getPorts() {
        return declaredPorts;
    }

    @Override
    public ImmutableList<IInPortImpl> getInPorts() {
        return inPorts;
    }

    @Override
    public ImmutableList<IOutPortImpl> getOutPorts() {
        return outPorts;
    }

    @Override
    public Executable getDefinition() {
        require(State.FINISHED);
        @Nullable Executable localExecutable = executable;
        if (localExecutable == null) {
            throw new IllegalStateException(String.format(
                "toExecutable() called on %s even though instance is not available. This indicates invalid linker "
                    + "options.", this
            ));
        }
        return localExecutable;
    }

    @Override
    void collectEnclosedByAnnotatedConstruct(Collection<AbstractFreezable> freezables) {
        freezables.addAll(declaredPorts);
    }

    @Override
    void preProcessFreezable(FinishContext context) { }

    @Override
    void finishModule(FinishContext context) throws LinkerException {
        executable = context.getExecutable(this);
    }

    @Override
    void verifyFreezable(VerifyContext context) { }
}
