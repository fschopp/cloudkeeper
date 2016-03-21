package xyz.cloudkeeper.model.bare.element.module;

import xyz.cloudkeeper.model.bare.element.BarePluginDeclaration;

import javax.annotation.Nullable;

public interface BareModuleDeclaration extends BarePluginDeclaration {
    /**
     * Human-readable name of the entity modeled by this interface.
     */
    String NAME = "module declaration";

    /**
     * Name of of the template element that every composite-module declaration implicitly contains.
     */
    String TEMPLATE_ELEMENT_NAME = "$template";

    /**
     * Returns the module of this declaration.
     */
    @Nullable
    BareDeclarableModule getTemplate();

    /**
     * Default implementations for standard methods.
     */
    final class Default {
        private Default() { }

        /**
         * Default implementation for {@link BareModuleDeclaration#toString()}.
         */
        public static String toString(BareModuleDeclaration instance) {
            return String.format("%s %s", NAME, instance.getSimpleName());
        }
    }
}
