package xyz.cloudkeeper.model.bare.element.module;

import java.util.List;

public interface BareParentModule extends BareDeclarableModule {
    /**
     * Returns the child modules in this parent module.
     */
    List<? extends BareModule> getModules();

    /**
     * Returns all connections within this parent module (in a non-transitive fashion).
     */
    List<? extends BareConnection> getConnections();
}
