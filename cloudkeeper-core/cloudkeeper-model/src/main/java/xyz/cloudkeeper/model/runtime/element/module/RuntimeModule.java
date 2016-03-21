package xyz.cloudkeeper.model.runtime.element.module;

import xyz.cloudkeeper.model.Immutable;
import xyz.cloudkeeper.model.bare.element.module.BareModule;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.runtime.element.RuntimeElement;
import xyz.cloudkeeper.model.util.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * CloudKeeper Module.
 *
 * Instances are immutable. Each instance is guaranteed to implement one (and only one) of the following interfaces:
 * <ul><li>
 *     {@link RuntimeCompositeModule}
 * </li><li>
 *     {@link RuntimeInputModule}
 * </li><li>
 *     {@link RuntimeInvokeModule}
 * </li><li>
 *     {@link RuntimeLoopModule}
 * </li><li>
 *     {@link RuntimeSimpleModule}
 * </li></ul>
 *
 * Linked module instances have an identity, so {@code equals()} and {@code hashCode()} are expected to behave like
 * {@link Object#equals(Object)} and {@link Object#hashCode()}, respectively.
 */
public interface RuntimeModule extends RuntimeElement, BareModule, Immutable {
    /**
     * {@inheritDoc}
     *
     * The simple name is null if and only if {@link #getParent()} is null.
     */
    @Override
    @Nullable
    SimpleName getSimpleName();

    /**
     * Calls the visitor method that is appropriate for the actual type of this module.
     *
     * @param visitor the visitor operating on this module
     * @param parameter additional parameter to the visitor
     * @param <T> the return type of the visitor's methods
     * @param <P> the type of the additional parameter to the visitor's methods
     * @return a visitor-specified result
     */
    @Nullable
    <T, P> T accept(RuntimeModuleVisitor<T, P> visitor, @Nullable P parameter);

    /**
     * Returns the list of all ports.
     *
     * The returned list includes both declared ports and implicit ports (like, e.g., the out-port of an input module).
     */
    ImmutableList<? extends RuntimePort> getPorts();

    /**
     * Returns the list of all in-ports that are contained in {@link #getPorts()}.
     */
    ImmutableList<? extends RuntimeInPort> getInPorts();

    /**
     * Returns the list of all out-ports that are contained in {@link #getPorts()}.
     */
    ImmutableList<? extends RuntimeOutPort> getOutPorts();

    // TODO: Remove in favor of getEnclosingElement()?
    /**
     * Returns this module's parent module, or {@code null} if this is a root module.
     *
     * The parent module is null if and only if {@link #getSimpleName()} is null.
     *
     * @return the parent module
     */
    @Nullable
    RuntimeParentModule getParent();

    /**
     * Returns the apply-to-all connection of this module (or {@code} null if there is none).
     *
     * @return connection
     */
    @Nullable
    RuntimeConnection getApplyToAllConnection();

    /**
     * Returns the index of this module within the list returned by {@code getParent().getModules()}.
     *
     * <p>The relative ID of a module is the 0-based index of the current module within the list of all children of the
     * parent module. Specifically, the relative ID is the position in the {@link List} that is obtained by calling
     * {@link RuntimeCompositeModule#getModules()} on {@link #getParent()}).
     *
     * @return relative ID, or -1 if {@link #getParent()} is null
     */
    int getIndex();
}
