package xyz.cloudkeeper.dsl;

import xyz.cloudkeeper.dsl.exception.InvalidClassException;
import xyz.cloudkeeper.model.Immutable;
import xyz.cloudkeeper.model.bare.element.BarePluginDeclarationVisitor;
import xyz.cloudkeeper.model.bare.element.module.BareDeclaredPortsModule;
import xyz.cloudkeeper.model.bare.element.module.BareModuleDeclaration;
import xyz.cloudkeeper.model.immutable.Location;
import xyz.cloudkeeper.model.immutable.element.SimpleName;

import java.util.List;

final class DSLModuleDeclaration implements BareModuleDeclaration, Immutable {
    private final SimpleName simpleName;
    private final List<DSLAnnotation> annotations;
    private final Module<?> template;

    /**
     * Construct a composite-module declaration from a Java class.
     *
     * @param clazz class
     * @param expectedClass class object for either {@link CompositeModule} or {@link SimpleModule}
     * @param moduleFactory module factory that will dynamically create a subclass and load this class
     * @throws InvalidClassException if the given class is not a composite-module declaration
     */
    <T extends Module<T>> DSLModuleDeclaration(Class<T> clazz, Class<?> expectedClass, ModuleFactory moduleFactory) {
        if (!expectedClass.isAssignableFrom(clazz)) {
            throw new InvalidClassException(String.format(
                "Expected subclass of %s, but got %s.", expectedClass, clazz
            ));
        }

        simpleName = Shared.simpleNameOfClass(clazz);
        annotations = DSLAnnotation.unmodifiableAnnotationList(clazz);
        template = moduleFactory.create(clazz);
    }

    @Override
    public String toString() {
        return BareModuleDeclaration.Default.toString(this);
    }

    @Override
    public Location getLocation() {
        return template.getDeclarationLocation();
    }

    @Override
    public <T, P> T accept(BarePluginDeclarationVisitor<T, P> visitor, P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    public SimpleName getSimpleName() {
        return simpleName;
    }

    @Override
    public List<DSLAnnotation> getDeclaredAnnotations() {
        return annotations;
    }

    @Override
    public BareDeclaredPortsModule getTemplate() {
        return (BareDeclaredPortsModule) template;
    }
}
