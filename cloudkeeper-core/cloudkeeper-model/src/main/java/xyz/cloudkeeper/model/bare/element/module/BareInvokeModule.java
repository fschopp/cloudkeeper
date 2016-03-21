package xyz.cloudkeeper.model.bare.element.module;

import xyz.cloudkeeper.model.bare.element.BareQualifiedNameable;

import javax.annotation.Nullable;

/**
 * Invoke module.
 *
 * <p>Modules of this kind <em>invoke</em> a declared module.
 */
public interface BareInvokeModule extends BareModule {
    /**
     * Human-readable name of the entity modeled by this interface.
     */
    String NAME = "invoke module";

    /**
     * Returns the module declaration.
     */
    @Nullable
    BareQualifiedNameable getDeclaration();


    /**
     * Default implementations for standard methods.
     */
    final class Default {
        private Default() { }

        /**
         * Default implementation for {@link BareInvokeModule#toString()}.
         */
        public static String toString(BareInvokeModule instance) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(NAME).append(" '").append(instance.getSimpleName()).append('\'');

            @Nullable BareQualifiedNameable declaration = instance.getDeclaration();
            if (declaration != null) {
                stringBuilder.append(" (").append(declaration.getQualifiedName()).append(')');
            }
            return stringBuilder.toString();
        }
    }
}
