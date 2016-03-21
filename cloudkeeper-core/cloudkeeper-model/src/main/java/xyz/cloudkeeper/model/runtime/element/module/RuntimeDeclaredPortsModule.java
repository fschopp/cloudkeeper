package xyz.cloudkeeper.model.runtime.element.module;

import xyz.cloudkeeper.model.bare.element.module.BareDeclaredPortsModule;
import xyz.cloudkeeper.model.util.ImmutableList;

public interface RuntimeDeclaredPortsModule extends RuntimeModule, BareDeclaredPortsModule {
    @Override
    ImmutableList<? extends RuntimePort> getDeclaredPorts();
}
