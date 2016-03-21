package xyz.cloudkeeper.model.beans.element;

import xyz.cloudkeeper.model.bare.element.BareReferenceable;
import xyz.cloudkeeper.model.beans.CopyOption;
import xyz.cloudkeeper.model.beans.MutableLocatable;

import java.net.URI;
import java.util.Objects;
import javax.annotation.Nullable;

public final class MutableReferenceable extends MutableLocatable<MutableReferenceable> implements BareReferenceable {
    private static final long serialVersionUID = -1450458332971962882L;

    @Nullable
    private URI uri;

    public MutableReferenceable() { }

    private MutableReferenceable(BareReferenceable original, CopyOption[] copyOptions) {
        super(original, copyOptions);
        uri = original.getURI();
    }

    /**
     * Returns a copy of the given instance, or {@code null} if the given instance is {@code null}.
     */
    @Nullable
    public static MutableReferenceable copyOf(@Nullable BareReferenceable original, CopyOption... copyOptions) {
        return original != null
            ? new MutableReferenceable(original, copyOptions)
            : null;
    }

    @Override
    public boolean equals(@Nullable Object otherObject) {
        if (this == otherObject) {
            return true;
        } else if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        return Objects.equals(uri, ((MutableReferenceable) otherObject).uri);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri);
    }

    @Override
    public String toString() {
        return BareReferenceable.Default.toString(this);
    }

    @Override
    protected MutableReferenceable self() {
        return this;
    }

    @Override
    @Nullable
    public URI getURI() {
        return uri;
    }

    public MutableReferenceable setURI(@Nullable URI uri) {
        this.uri = uri;
        return this;
    }

    public MutableReferenceable setURI(String uri) {
        return setURI(URI.create(uri));
    }
}
