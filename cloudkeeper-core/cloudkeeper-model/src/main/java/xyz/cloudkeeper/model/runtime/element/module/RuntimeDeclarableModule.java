package xyz.cloudkeeper.model.runtime.element.module;

import xyz.cloudkeeper.model.bare.element.module.BareDeclarableModule;
import xyz.cloudkeeper.model.util.ImmutableList;

public interface RuntimeDeclarableModule extends RuntimeModule, BareDeclarableModule {
    @Override
    ImmutableList<? extends RuntimePort> getDeclaredPorts();
}
