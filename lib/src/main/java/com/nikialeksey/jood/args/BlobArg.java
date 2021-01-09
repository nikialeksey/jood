package com.nikialeksey.jood.args;

import com.nikialeksey.jood.JdException;
import org.cactoos.Scalar;

import java.io.InputStream;
import java.sql.PreparedStatement;

public final class BlobArg implements Arg {

    private final Scalar<InputStream> value;

    public BlobArg(final InputStream value) {
        this(() -> value);
    }

    public BlobArg(final Scalar<InputStream> value) {
        this.value = value;
    }

    @Override
    public void printTo(
            final PreparedStatement stmt,
            final int index
    ) throws JdException {
        try {
            stmt.setBlob(index, value.value());
        } catch (Exception e) {
            throw new JdException(
                    "Could not get the stream value for argument",
                    e
            );
        }
    }
}
