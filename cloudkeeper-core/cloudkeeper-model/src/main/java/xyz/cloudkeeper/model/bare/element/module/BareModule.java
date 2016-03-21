package xyz.cloudkeeper.model.bare.element.module;

import xyz.cloudkeeper.model.bare.element.BareElement;
import xyz.cloudkeeper.model.bare.element.BareSimpleNameable;
import xyz.cloudkeeper.model.immutable.element.SimpleName;

import javax.annotation.Nullable;

/**
 * CloudKeeper bare module.
 *
 * Each instance is guaranteed to implement one (and only one) of the following interfaces:
 * <ul><li>
 *     {@link BareCompositeModule}
 * </li><li>
 *     {@link BareInputModule}
 * </li><li>
 *     {@link BareInvokeModule}
 * </li><li>
 *     {@link BareLoopModule}
 * </li><li>
 *     {@link BareSimpleModule}
 * </li></ul>
 *
 * This interface requires implementations to be neither mutable nor immutable.
 */
public interface BareModule extends BareElement, BareSimpleNameable {
    /**
     * Human-readable name of the entity modeled by this interface.
     */
    String NAME = "module";

    /**
     * Returns the name of this module, or {@code null} of this module does not have a name.
     *
     * <p>A module is only allowed not to have a name if it is a top-level module that does not have an enclosing
     * (parent) module. As an example, {@link BareModuleDeclaration#getTemplate()} always returns a top-level
     * module.
     *
     * @return name of this module, or {@code null} of this module does not have a name
     */
    @Override
    @Nullable
    SimpleName getSimpleName();


    /**
     * Calls the visitor method that is appropriate for the actual type of this instance.
     *
     * @param visitor module visitor
     * @param parameter parameter that will be passed to the visit method, may be null
     * @param <T> return type of visitor
     * @return result returned by visitor
     */
    @Nullable
    <T, P> T accept(BareModuleVisitor<T, P> visitor, @Nullable P parameter);
}
