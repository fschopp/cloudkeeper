package xyz.cloudkeeper.model.beans.element.module;

import xyz.cloudkeeper.model.bare.element.module.BareModuleVisitor;
import xyz.cloudkeeper.model.bare.element.module.BareInvokeModule;
import xyz.cloudkeeper.model.beans.CopyOption;
import xyz.cloudkeeper.model.beans.element.MutableQualifiedNamable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name = "proxy-module")
public final class MutableInvokeModule extends MutableModule<MutableInvokeModule> implements BareInvokeModule {
    private static final long serialVersionUID = 5394012602906125856L;

    @Nullable private MutableQualifiedNamable declaration;

    public MutableInvokeModule() { }

    private MutableInvokeModule(BareInvokeModule original, CopyOption[] copyOptions) {
        super(original, copyOptions);
        declaration = MutableQualifiedNamable.copyOf(original.getDeclaration(), copyOptions);
    }

    @Nullable
    public static MutableInvokeModule copyOfInvokeModule(
            @Nullable BareInvokeModule original, CopyOption... copyOptions) {
        return original == null
            ? null
            : new MutableInvokeModule(original, copyOptions);
    }

    @Override
    public boolean equals(@Nullable Object otherObject) {
        if (this == otherObject) {
            return true;
        } else if (!super.equals(otherObject)) {
            return false;
        }
        return Objects.equals(declaration, ((MutableInvokeModule) otherObject).declaration);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(declaration);
    }

    @Override
    public String toString() {
        return BareInvokeModule.Default.toString(this);
    }

    @Override
    protected MutableInvokeModule self() {
        return this;
    }

    @Override
    @Nullable
    public <T, P> T accept(BareModuleVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visitLinkedModule(this, parameter);
    }

    @XmlAttribute(name = "ref")
    @Override
    @Nullable
    public MutableQualifiedNamable getDeclaration() {
        return declaration;
    }

    public MutableInvokeModule setDeclaration(@Nullable MutableQualifiedNamable declaration) {
        this.declaration = declaration;
        return this;
    }

    public MutableInvokeModule setDeclaration(String declarationName) {
        return setDeclaration(
            new MutableQualifiedNamable().setQualifiedName(declarationName)
        );
    }
}
