package xyz.cloudkeeper.model.runtime.element.module;

import xyz.cloudkeeper.model.api.Executable;
import xyz.cloudkeeper.model.bare.element.BareReferenceable;

import java.net.URI;
import javax.annotation.Nonnull;

/**
 * Simple-module body.
 *
 * <p>Contains an {@link Executable} instance for evaluation of a simple module, given actual arguments.
 */
public interface RuntimeSimpleModuleBody extends BareReferenceable {
    @Override
    @Nonnull
    URI getURI();

    @Nonnull
    Executable getExecutable();
}
