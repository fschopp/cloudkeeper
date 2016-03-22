package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.bare.element.BarePluginDeclarationVisitor;
import xyz.cloudkeeper.model.bare.element.module.BareModule;
import xyz.cloudkeeper.model.bare.element.module.BareModuleDeclaration;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.RuntimeElement;
import xyz.cloudkeeper.model.runtime.element.RuntimePluginDeclarationVisitor;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeModuleDeclaration;
import xyz.cloudkeeper.model.util.ImmutableList;

import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nullable;

final class ModuleDeclarationImpl extends PluginDeclarationImpl implements RuntimeModuleDeclaration {
    private final ModuleImpl template;

    ModuleDeclarationImpl(@Nullable  BareModuleDeclaration original, CopyContext parentContext) throws LinkerException {
        super(original, parentContext);
        assert original != null;

        CopyContext context = getCopyContext();
        @Nullable BareModule originalTemplate = original.getTemplate();
        CopyContext templateContext = context.newContextForProperty("template");
        template = ModuleImpl.copyOf(originalTemplate, templateContext, -1);
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

    @Override
    public ModuleImpl getTemplate() {
        return template;
    }

    @Override
    void collectEnclosedByAnnotatedConstruct(Collection<AbstractFreezable> freezables) {
        freezables.add(template);
    }

    @Override
    void preProcessPluginDeclaration(FinishContext context) { }

    @Override
    void verifyFreezable(VerifyContext context) { }
}
