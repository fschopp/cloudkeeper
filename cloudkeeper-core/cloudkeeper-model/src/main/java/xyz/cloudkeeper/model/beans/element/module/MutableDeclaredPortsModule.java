package xyz.cloudkeeper.model.beans.element.module;

import xyz.cloudkeeper.model.bare.element.module.BareDeclaredPortsModule;
import xyz.cloudkeeper.model.bare.element.module.BarePort;
import xyz.cloudkeeper.model.beans.CopyOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;

public abstract class MutableDeclaredPortsModule<D extends MutableDeclaredPortsModule<D>>
        extends MutableModule<D>
        implements BareDeclaredPortsModule {
    private static final long serialVersionUID = -4418097975113396495L;

    private final ArrayList<MutablePort<?>> declaredPorts = new ArrayList<>();

    MutableDeclaredPortsModule() { }

    MutableDeclaredPortsModule(BareDeclaredPortsModule original, CopyOption[] copyOptions) {
        super(original, copyOptions);

        for (BarePort port: original.getDeclaredPorts()) {
            declaredPorts.add(MutablePort.copyOfPort(port, copyOptions));
        }
    }

    @Override
    public boolean equals(@Nullable Object otherObject) {
        // Note that super.equals() performs the verification that otherObject is of the same class
        if (!super.equals(otherObject)) {
            return false;
        }

        MutableDeclaredPortsModule<?> other = (MutableDeclaredPortsModule<?>) otherObject;
        return Objects.equals(declaredPorts, other.declaredPorts);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(declaredPorts);
    }

    @XmlElementWrapper(name = "ports")
    @XmlElementRef
    @Override
    public final List<MutablePort<?>> getDeclaredPorts() {
        return declaredPorts;
    }

    public D setDeclaredPorts(List<MutablePort<?>> declaredPorts) {
        Objects.requireNonNull(declaredPorts);
        List<MutablePort<?>> backup = new ArrayList<>(declaredPorts);
        this.declaredPorts.clear();
        this.declaredPorts.addAll(backup);
        return self();
    }
}
