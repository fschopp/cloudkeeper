package xyz.cloudkeeper.linker;

import xyz.cloudkeeper.model.LinkerException;
import xyz.cloudkeeper.model.api.Executable;
import xyz.cloudkeeper.model.bare.element.BareReferenceable;
import xyz.cloudkeeper.model.runtime.element.module.RuntimeSimpleModuleBody;

import java.net.URI;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class SimpleModuleBodyImpl extends LocatableImpl implements RuntimeSimpleModuleBody {
    private final URI uri;
    @Nullable private volatile Executable executable;

    SimpleModuleBodyImpl(@Nullable BareReferenceable original, CopyContext parentContext) throws LinkerException {
        super(original, parentContext);
        assert original != null;
        uri = Preconditions.requireNonNull(original.getURI(), getCopyContext().newContextForProperty("uri"));
    }

    @Nonnull
    @Override
    public URI getURI() {
        return uri;
    }

    @Nonnull
    @Override
    public Executable getExecutable() {
        require(State.LINKED);
        @Nullable Executable localExecutable = executable;
        if (localExecutable == null) {
            throw new IllegalStateException(String.format(
                "getExecutable() called on %s even though instance is not available. This indicates invalid linker "
                    + "options.", this
            ));
        }
        return localExecutable;
    }

    @Override
    void collectEnclosed(Collection<AbstractFreezable> freezables) { }

    @Override
    void preProcessFreezable(FinishContext context) throws LinkerException {
        executable = context.getExecutable(this);
    }

    @Override
    void augmentFreezable(FinishContext context) { }

    @Override
    void finishFreezable(FinishContext context) { }

    @Override
    void verifyFreezable(VerifyContext context) { }
}
