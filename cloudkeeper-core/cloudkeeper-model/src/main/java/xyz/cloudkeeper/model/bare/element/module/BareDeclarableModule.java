package xyz.cloudkeeper.model.bare.element.module;

import java.util.List;

/**
 * Mixin interface for modules that can be declared in a {@link BareModuleDeclaration}.
 */
public interface BareDeclarableModule extends BareModule {
    /**
     * Returns all declared ports, but not implicit ports (such as the continue-port in a
     * {@link BareLoopModule}).
     */
    List<? extends BarePort> getDeclaredPorts();
}
