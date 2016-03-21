package xyz.cloudkeeper.model.runtime.element.module;

import xyz.cloudkeeper.model.bare.element.module.BareModuleDeclaration;
import xyz.cloudkeeper.model.runtime.element.RuntimePluginDeclaration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface RuntimeModuleDeclaration extends RuntimePluginDeclaration, BareModuleDeclaration {
    @Override
    @Nonnull
    RuntimeDeclarableModule getTemplate();
}
