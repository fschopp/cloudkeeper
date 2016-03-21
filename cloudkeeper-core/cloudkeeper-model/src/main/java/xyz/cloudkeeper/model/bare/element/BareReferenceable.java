package xyz.cloudkeeper.model.bare.element;

import xyz.cloudkeeper.model.bare.BareLocatable;

import java.net.URI;
import javax.annotation.Nullable;

/**
 * A mixin interface for an element that has an {@link java.net.URI}.
 */
public interface BareReferenceable extends BareLocatable {
    /**
     * Human-readable name of the entity modeled by this interface.
     */
    String NAME = "reference";

    /**
     * Returns the {@link URI} of this element (or reference to an element with this name).
     */
    @Nullable
    URI getURI();

    /**
     * Default implementations for standard methods.
     */
    final class Default {
        private Default() { }

        /**
         * Default implementation for {@link BareSimpleNameable#toString()}.
         */
        public static String toString(BareReferenceable instance) {
            return String.format("%s '%s'", NAME, instance.getURI());
        }
    }
}
