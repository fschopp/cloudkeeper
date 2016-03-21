package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.bare.element.BarePluginDeclarationVisitor;
import xyz.cloudkeeper.model.bare.element.module.BareDeclarableModule;
import xyz.cloudkeeper.model.bare.element.module.BareModuleDeclaration;
import xyz.cloudkeeper.model.bare.element.module.BarePort;
import xyz.cloudkeeper.model.beans.element.module.MutablePort;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.RuntimeElement;
import xyz.cloudkeeper.model.runtime.element.RuntimePluginDeclarationVisitor;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeDeclarableModule;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeModuleDeclaration;
import xyz.cloudkeeper.model.util.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

final class ModuleDeclarationImpl extends PluginDeclarationImpl implements RuntimeModuleDeclaration {
    private final ModuleImpl template;

    ModuleDeclarationImpl(@Nullable  BareModuleDeclaration original, CopyContext parentContext) throws LinkerException {
        super(original, parentContext);
        assert original != null;

        CopyContext context = getCopyContext();
        @Nullable BareDeclarableModule originalTemplate = original.getTemplate();
        CopyContext templateContext = context.newContextForProperty("template");
        template = ModuleImpl.copyOf(originalTemplate, templateContext, -1);
        assert originalTemplate != null : "non-null due to successful ModuleImpl#copyOf";
        Preconditions.requireCondition(template.getDeclaredAnnotations().isEmpty(), templateContext,
            "Template module of composite-module declaration cannot have annotations.");

        // At this point, it should be perfectly safe to create a mutable copy of the port list as well. All possible
        // errors should have been detected when creating the list of runtime (immutable) ports.
        List<? extends BarePort> originalPorts = originalTemplate.getDeclaredPorts();
        mutablePorts = originalPorts.stream().map(MutablePort::copyOfPort).collect(Collectors.toList());
    }

    @Override
    @Nullable
    public final <T, P> T accept(BarePluginDeclarationVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    @Nullable
    public final <T, P> T accept(RuntimePluginDeclarationVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    public final IElementImpl getEnclosingElement() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Since there is currently no support for inheritance of module declarations, annotations can likewise not be
     * inherited.
     */
    @Override
    @Nullable
    public ModuleDeclarationImpl getSuperAnnotatedConstruct() {
        return null;
    }

    // No need to be volatile because instance variable is only accessed before object is finished, which will always
    // be from a single thread.
    @Nullable private List<MutablePort<?>> mutablePorts;

    @Override
    public String toString() {
        return BareModuleDeclaration.Default.toString(this);
    }

    @Override
    public ImmutableList<? extends IElementImpl> getEnclosedElements() {
        return ImmutableList.of(template);
    }

    @Override
    public <T extends RuntimeElement> T getEnclosedElement(Class<T> clazz, SimpleName simpleName) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(simpleName);

        if (simpleName.contentEquals(TEMPLATE_ELEMENT_NAME) && clazz.isInstance(template)) {
            @SuppressWarnings("unchecked")
            T typedTemplate = (T) template;
            return typedTemplate;
        } else {
            return null;
        }
    }

    /**
     * Returns the list of ports, similar to {@link ModuleImpl#getPorts()} but with weaker return type.
     *
     * <p>This method may be called (and its return value be used) safely even before the module declaration is fully
     * constructed; that is, before the state is {@link State#PRECOMPUTED}. Once this instance is frozen, this method
     * will return the same reference as {@code #getPorts()}.
     *
     * <p>Note that this method can be called also if the state is {@link State#PRECOMPUTED}: For instance, when a new
     * proxy module is linked against a fully linked and verified repository.
     *
     * <p>Since this method is a package-private method, no precaution is taken to prevent exposing internal mutable
     * data structures. Callers are expected not to modify the returned list.
     *
     * @see InvokeModuleImpl#preProcessFreezable(FinishContext)
     */
    List<? extends BarePort> getBarePorts() {
        if (getState().compareTo(State.FINISHED) >= 0) {
            return template.getPorts();
        } else {
            assert mutablePorts != null : "must be non-null while not yet finished";
            return mutablePorts;
        }
    }

    @Override
    public RuntimeDeclarableModule getTemplate() {
        return (RuntimeDeclarableModule) template;
    }

    @Override
    void collectEnclosedByAnnotatedConstruct(Collection<AbstractFreezable> freezables) {
        freezables.add(template);
    }

    @Override
    void finishFreezable(FinishContext context) {
        mutablePorts = null;
    }

    @Override
    void verifyFreezable(VerifyContext context) { }
}
