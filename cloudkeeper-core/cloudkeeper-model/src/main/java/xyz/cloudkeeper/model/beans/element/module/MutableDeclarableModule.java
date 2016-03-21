package xyz.cloudkeeper.model.beans.element.module;

import xyz.cloudkeeper.model.bare.element.module.BareDeclarableModule;
import xyz.cloudkeeper.model.beans.CopyOption;

public abstract class MutableDeclarableModule<D extends MutableDeclarableModule<D>>
        extends MutableModule<D>
        implements BareDeclarableModule {
    private static final long serialVersionUID = -4418097975113396495L;

    MutableDeclarableModule() { }

    MutableDeclarableModule(BareDeclarableModule original, CopyOption[] copyOptions) {
        super(original, copyOptions);
    }
}
