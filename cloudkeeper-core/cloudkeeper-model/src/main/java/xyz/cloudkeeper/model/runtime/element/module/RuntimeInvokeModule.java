package xyz.cloudkeeper.model.runtime.element.module;

import xyz.cloudkeeper.model.bare.element.module.BareInvokeModule;

import javax.annotation.Nonnull;

/**
 * Linked module.
 *
 * <p>{@link RuntimeInvokeModule} are similar to function calls in a regular programming language. They contain a
 * reference to a module declaration.
 */
public interface RuntimeInvokeModule extends BareInvokeModule, RuntimeModule {
    /**
     * Returns the declaration.
     *
     * @return declaration (guaranteed to be non-null)
     */
    @Override
    @Nonnull
    RuntimeModuleDeclaration getDeclaration();
}
