package com.nikialeksey.jood.args;

import com.nikialeksey.jood.DbException;
import org.cactoos.Scalar;

import java.sql.PreparedStatement;

public class StringArg implements Arg {
    private final Scalar<String> value;

    public StringArg(final String value) {
        this(() -> value);
    }

    public StringArg(final Scalar<String> value) {
        this.value = value;
    }

    @Override
    public void printTo(
        final PreparedStatement stmt,
        final int index
    ) throws DbException {
        try {
            stmt.setString(index, value.value());
        } catch (Exception e) {
            throw new DbException(
                "Could not get the string value for argument",
                e
            );
        }
    }
}
