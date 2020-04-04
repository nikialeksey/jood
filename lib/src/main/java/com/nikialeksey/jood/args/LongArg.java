package com.nikialeksey.jood.args;

import com.nikialeksey.jood.DbException;
import org.cactoos.Scalar;

import java.sql.PreparedStatement;

public class LongArg implements Arg {
    private final Scalar<Long> value;

    public LongArg(final long value) {
        this(() -> value);
    }

    public LongArg(final Scalar<Long> value) {
        this.value = value;
    }

    @Override
    public void printTo(
        final PreparedStatement stmt,
        final int index
    ) throws DbException {
        try {
            stmt.setLong(index, value.value());
        } catch (Exception e) {
            throw new DbException(
                "Could not get the long value for argument",
                e
            );
        }
    }
}