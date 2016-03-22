package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.bare.element.module.BareInvokeModule;
import xyz.cloudkeeper.model.bare.element.module.BareModuleDeclaration;
import xyz.cloudkeeper.model.bare.element.module.BareModuleVisitor;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.RuntimeElement;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeInvokeModule;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeModuleVisitor;
import xyz.cloudkeeper.model.util.ImmutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

final class InvokeModuleImpl extends ModuleImpl implements RuntimeInvokeModule {
    private final NameReference declarationReference;

    @Nullable private volatile ModuleDeclarationImpl moduleDeclaration;
    @Nullable private volatile Map<SimpleName, PortImpl> declaredPortsMap;
    @Nullable private volatile ImmutableList<PortImpl> declaredPorts;
    @Nullable private volatile ImmutableList<IInPortImpl> inPorts;
    @Nullable private volatile ImmutableList<IOutPortImpl> outPorts;

    InvokeModuleImpl(BareInvokeModule original, CopyContext parentContext, int index) throws LinkerException {
        super(original, parentContext, index);
        declarationReference
            = new NameReference(original.getDeclaration(), getCopyContext().newContextForProperty("declaration"));
    }

    @Override
    public String toString() {
        return BareInvokeModule.Default.toString(this);
    }

    @Override
    @Nullable
    public <T, P> T accept(BareModuleVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visitLinkedModule(this, parameter);
    }

    @Override
    @Nullable
    public <T, P> T accept(RuntimeModuleVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    public ModuleImpl getSuperAnnotatedConstruct() {
        require(State.LINKED);
        @Nullable ModuleDeclarationImpl localModuleDeclaration = moduleDeclaration;
        assert localModuleDeclaration != null;
        return localModuleDeclaration.getTemplate();
    }

    /**
     * {@inheritDoc}
     *
     * A proxy module does not enclose any elements.
     */
    @Override
    public ImmutableList<? extends IElementImpl> getEnclosedElements() {
        require(State.AUGMENTED);
        @Nullable ImmutableList<PortImpl> localDeclaredPorts = declaredPorts;
        assert localDeclaredPorts != null : "must be non-null when linked";
        return localDeclaredPorts;
    }

    @Override
    public <T extends RuntimeElement> T getEnclosedElement(Class<T> clazz, SimpleName simpleName) {
        require(State.AUGMENTED);
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(simpleName);

        @Nullable Map<SimpleName, PortImpl> localDeclaredPortsMap = declaredPortsMap;
        assert localDeclaredPortsMap != null : "must be non-null when linked";
        @Nullable PortImpl port = localDeclaredPortsMap.get(simpleName);
        if (port != null && clazz.isInstance(port)) {
            @SuppressWarnings("unchecked")
            T typedPort = (T) port;
            return typedPort;
        } else {
            return null;
        }
    }

    @Override
    public ModuleDeclarationImpl getDeclaration() {
        require(State.LINKED);
        @Nullable ModuleDeclarationImpl localModuleDeclaration = moduleDeclaration;
        assert localModuleDeclaration != null : "must be non-null when in state " + State.LINKED;
        return localModuleDeclaration;
    }

    /**
     * {@inheritDoc}
     *
     * Note that a proxy module can only reference simple or composite modules (both of which do not have implicit
     * ports).
     */
    @Override
    public ImmutableList<PortImpl> getPorts() {
        require(State.AUGMENTED);
        @Nullable ImmutableList<PortImpl> localDeclaredPorts = declaredPorts;
        assert localDeclaredPorts != null : "must be non-null when in state " + State.FINISHED;
        return localDeclaredPorts;
    }

    @Override
    public ImmutableList<IInPortImpl> getInPorts() {
        require(State.AUGMENTED);
        @Nullable ImmutableList<IInPortImpl> localInPorts = inPorts;
        assert localInPorts != null : "must be non-null when in state " + State.FINISHED;
        return localInPorts;
    }

    @Override
    public ImmutableList<IOutPortImpl> getOutPorts() {
        require(State.AUGMENTED);
        @Nullable ImmutableList<IOutPortImpl> localOutPorts = outPorts;
        assert localOutPorts != null : "must be non-null when in state " + State.FINISHED;
        return localOutPorts;
    }

    @Override
    void collectEnclosedByAnnotatedConstruct(Collection<AbstractFreezable> freezables) {
        @Nullable ImmutableList<PortImpl> localDeclaredPort = declaredPorts;
        if (localDeclaredPort != null) {
            freezables.addAll(localDeclaredPort);
        }
    }

    @Override
    public ModuleImpl resolveInvocations() {
        ModuleImpl module = this;
        while (module instanceof InvokeModuleImpl) {
            module = ((InvokeModuleImpl) module).getDeclaration().getTemplate();
        }
        return module;
    }

    /**
     * Returns the referenced module declaration as {@link NameReference} object.
     */
    NameReference getDeclarationReference() {
        return declarationReference;
    }

    @Override
    void preProcessModule(FinishContext context) throws LinkerException {
        moduleDeclaration
            = context.getDeclaration(BareModuleDeclaration.NAME, ModuleDeclarationImpl.class, declarationReference);
    }

    @Override
    void augmentFreezable(FinishContext context) throws LinkerException {
        @Nullable ModuleDeclarationImpl localModuleDeclaration = moduleDeclaration;
        assert localModuleDeclaration != null;

        // Add missing ports
        CopyContext copyContext = getCopyContext();
        PortAccumulationState state = new PortAccumulationState();
        Map<SimpleName, PortImpl> newPortMap = new LinkedHashMap<>();
        collect(
            context.getBarePorts(localModuleDeclaration),
            copyContext.newSystemContext("declared ports of " + declarationReference)
                .newContextForListProperty("barePorts"),
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

        // We need to manually link the ports, as they were created while we were already in state State.LINKED
        FinishContext contextForContained = context.newChildContext(this);
        for (PortImpl port: newPortMap.values()) {
            port.linkProxyModules(contextForContained);
        }
    }

    @Override
    void verifyModule(VerifyContext context) { }
}
