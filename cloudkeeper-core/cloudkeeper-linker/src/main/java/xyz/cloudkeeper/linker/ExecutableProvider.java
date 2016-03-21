package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.api.Executable;
import xyz.cloudkeeper.model.api.RuntimeStateProvisionException;
import xyz.cloudkeeper.model.immutable.element.Name;

import java.net.URI;
import java.util.Optional;

/**
 * Provider that accepts a {@link URI} and returns an optional {@link Executable} instance.
 */
@FunctionalInterface
public interface ExecutableProvider {
    /**
     * Returns an optional {@link Executable} instance that can evaluate the simple-module definition identified by the
     * given URI.
     *
     * @param uri URI that identifies the simple-module definition
     * @return the optional {@link Executable} object
     * @throws RuntimeStateProvisionException if the executable instance cannot be created for any reason
     */
    Optional<Executable> provideExecutable(URI uri) throws RuntimeStateProvisionException;
}
