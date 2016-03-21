package xyz.cloudkeeper.model.beans.element.module;

import xyz.cloudkeeper.model.bare.element.BarePluginDeclarationVisitor;
import xyz.cloudkeeper.model.bare.element.module.BareModuleDeclaration;
import xyz.cloudkeeper.model.beans.CopyOption;
import xyz.cloudkeeper.model.beans.element.MutablePluginDeclaration;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;

public final class MutableModuleDeclaration
        extends MutablePluginDeclaration<MutableModuleDeclaration>
        implements BareModuleDeclaration {
    private static final long serialVersionUID = -3405633500476304133L;

    @Nullable private MutableDeclarableModule<?> template;

    public MutableModuleDeclaration() { }

    private MutableModuleDeclaration(BareModuleDeclaration original, CopyOption[] copyOptions) {
        super(original, copyOptions);
        template = (MutableDeclarableModule<?>) MutableModule.copyOfModule(original.getTemplate(), copyOptions);
    }

    @Nullable
    public static MutableModuleDeclaration copyOfModuleDeclaration(@Nullable BareModuleDeclaration original,
            CopyOption... copyOptions) {
        return original == null
            ? null
            : new MutableModuleDeclaration(original, copyOptions);
    }

    @Override
    public String toString() {
        return BareModuleDeclaration.Default.toString(this);
    }

    @Override
    @Nullable
    public final <T, P> T accept(BarePluginDeclarationVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    protected MutableModuleDeclaration self() {
        return this;
    }

    @XmlElement
    @Override
    @Nullable
    public MutableDeclarableModule<?> getTemplate() {
        return template;
    }

    public MutableModuleDeclaration setTemplate(@Nullable MutableDeclarableModule<?> template) {
        this.template = template;
        return this;
    }
}
