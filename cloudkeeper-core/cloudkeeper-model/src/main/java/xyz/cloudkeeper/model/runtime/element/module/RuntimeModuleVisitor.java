package xyz.cloudkeeper.model.runtime.element.module;

import javax.annotation.Nullable;

/**
 * Visitor of modules, in the style of the visitor design pattern.
 *
 * @see xyz.cloudkeeper.model.bare.element.module.BareModuleVisitor
 */
public interface RuntimeModuleVisitor<T, P> {
    /**
     * Visits an input module.
     */
    @Nullable
    T visit(RuntimeInputModule module, @Nullable P parameter);

    /**
     * Visits a composite module.
     */
    @Nullable
    T visit(RuntimeCompositeModule module, @Nullable P parameter);

    /**
     * Visits a loop module.
     */
    @Nullable
    T visit(RuntimeLoopModule module, @Nullable P parameter);

    /**
     * Visits a proxy module.
     */
    @Nullable
    T visit(RuntimeInvokeModule module, @Nullable P parameter);

    /**
     * Visits a simple module.
     */
    @Nullable
    T visit(RuntimeSimpleModule module, @Nullable P parameter);
}
