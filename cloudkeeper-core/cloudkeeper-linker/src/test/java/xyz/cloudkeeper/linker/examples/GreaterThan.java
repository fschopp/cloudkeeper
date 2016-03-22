package xyz.cloudkeeper.linker.examples;

import xyz.cloudkeeper.model.beans.element.module.MutableInPort;
import xyz.cloudkeeper.model.beans.element.module.MutableModuleDeclaration;
import xyz.cloudkeeper.model.beans.element.module.MutableOutPort;
import xyz.cloudkeeper.model.beans.element.module.MutableSimpleModule;
import xyz.cloudkeeper.model.beans.type.MutableDeclaredType;

import java.util.Arrays;

public final class GreaterThan {
    private GreaterThan() { }

    public static MutableModuleDeclaration declaration() {
        return new MutableModuleDeclaration()
            .setSimpleName(GreaterThan.class.getSimpleName())
            .setTemplate(
                new MutableSimpleModule()
                    .setDefinition("x-example:" + GreaterThan.class.getName())
                    .setDeclaredPorts(Arrays.asList(
                        new MutableInPort()
                            .setSimpleName("num1")
                            .setType(MutableDeclaredType.fromType(Integer.class)),
                        new MutableInPort()
                            .setSimpleName("num2")
                            .setType(MutableDeclaredType.fromType(Integer.class)),
                        new MutableOutPort()
                            .setSimpleName("result")
                            .setType(new MutableDeclaredType().setDeclaration(Boolean.class.getName()))
                    ))
            );
    }
}
