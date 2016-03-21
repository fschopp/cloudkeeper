package xyz.cloudkeeper.model.bare.element.module;

import xyz.cloudkeeper.model.bare.element.BareReferenceable;

import javax.annotation.Nullable;

public interface BareSimpleModule extends BareDeclaredPortsModule {
    /**
     * Human-readable name of the entity modeled by this interface.
     */
    String NAME = "simple module";

    /**
     * Returns the definition of this simple module.
     */
    @Nullable
    BareReferenceable getDefinition();

    /**
     * Default implementations for standard methods.
     */
    final class Default {
        private Default() { }

        /**
         * Default implementation for {@link BareSimpleModule#toString()}.
         */
        public static String toString(BareSimpleModule instance) {
            return String.format("%s '%s'", NAME, instance.getSimpleName());
        }
    }
}
