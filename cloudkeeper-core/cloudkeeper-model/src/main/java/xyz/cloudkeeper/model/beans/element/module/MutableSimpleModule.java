package xyz.cloudkeeper.model.beans.element.module;

import xyz.cloudkeeper.model.bare.element.module.BareModuleVisitor;
import xyz.cloudkeeper.model.bare.element.module.BareSimpleModule;
import xyz.cloudkeeper.model.beans.CopyOption;
import xyz.cloudkeeper.model.beans.element.MutableReferenceable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "simple-module")
public final class MutableSimpleModule
        extends MutableDeclaredPortsModule<MutableSimpleModule>
        implements BareSimpleModule {
    private static final long serialVersionUID = -2753710509379024416L;

    @Nullable private MutableReferenceable definition;

    public MutableSimpleModule() { }

    private MutableSimpleModule(BareSimpleModule original, CopyOption[] copyOptions) {
        super(original, copyOptions);
    }

    @Nullable
    public static MutableSimpleModule copyOfSimpleModule(@Nullable BareSimpleModule original,
            CopyOption... copyOptions) {
        return original == null
            ? null
            : new MutableSimpleModule(original, copyOptions);
    }

    @Override
    public String toString() {
        return BareSimpleModule.Default.toString(this);
    }

    @Override
    protected MutableSimpleModule self() {
        return this;
    }

    @Override
    @Nullable
    public <T, P> T accept(BareModuleVisitor<T, P> visitor, @Nullable P parameter) {
        return visitor.visitSimpleModule(this, parameter);
    }

    @XmlElement(name = "definition")
    @Nullable
    @Override
    public MutableReferenceable getDefinition() {
        return definition;
    }

    public MutableSimpleModule setDefinition(@Nullable MutableReferenceable definition) {
        this.definition = definition;
        return this;
    }

    public MutableSimpleModule setDefinition(String definition) {
        return setDefinition(
            new MutableReferenceable()
                .setURI(definition)
        );
    }
}
