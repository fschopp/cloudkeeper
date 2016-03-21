package xyz.cloudkeeper.model.runtime.element.module;

import xyz.cloudkeeper.model.api.Executable;
import xyz.cloudkeeper.model.bare.element.module.BareSimpleModule;

import javax.annotation.Nonnull;

/**
 * Simple module with runtime state.
 *
 * This interface models a simple module as part of an optimized abstract syntax tree with runtime state information.
 */
public interface RuntimeSimpleModule extends RuntimeDeclarableModule, BareSimpleModule {
    /**
     * Returns the executable instance for this simple module.
     *
     * @throws IllegalStateException if the executable instance is not available
     */
    @Override
    @Nonnull
    Executable getDefinition();
}
