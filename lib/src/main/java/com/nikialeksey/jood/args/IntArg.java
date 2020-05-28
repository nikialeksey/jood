package com.nikialeksey.jood.args;

import com.nikialeksey.jood.JdException;
import org.cactoos.Scalar;

import java.sql.PreparedStatement;

public class IntArg implements Arg {
    private final Scalar<Integer> value;

    public IntArg(final int value) {
        this(() -> value);
    }

    public IntArg(final Scalar<Integer> value) {
        this.value = value;
    }

    @Override
    public void printTo(
        final PreparedStatement stmt,
        final int index
    ) throws JdException {
        try {
            stmt.setInt(index, value.value());
        } catch (Exception e) {
            throw new JdException(
                "Could not get the int value for argument",
                e
            );
        }
    }
}
