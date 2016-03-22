package xyz.cloudkeeper.linker.examples;

import xyz.cloudkeeper.model.beans.element.module.MutableInPort;
import xyz.cloudkeeper.model.beans.element.module.MutableModuleDeclaration;
import xyz.cloudkeeper.model.beans.element.module.MutableOutPort;
import xyz.cloudkeeper.model.beans.element.module.MutableSimpleModule;
import xyz.cloudkeeper.model.beans.type.MutableDeclaredType;

import java.util.Arrays;

public final class Decrease {
    private Decrease() { }

    public static MutableModuleDeclaration declaration() {
        return new MutableModuleDeclaration()
            .setSimpleName(Decrease.class.getSimpleName())
            .setTemplate(
                new MutableSimpleModule()
                    .setDefinition("x-example:" + Decrease.class.getName())
                    .setDeclaredPorts(Arrays.asList(
                        new MutableInPort()
                            .setSimpleName("num")
                            .setType(MutableDeclaredType.fromType(Integer.class)),
                        new MutableOutPort()
                            .setSimpleName("result")
                            .setType(MutableDeclaredType.fromType(Integer.class))
                    ))
            );
    }
}
