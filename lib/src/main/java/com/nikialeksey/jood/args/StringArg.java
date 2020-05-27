package com.nikialeksey.jood.args;

import com.nikialeksey.jood.JbException;
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
    ) throws JbException {
        try {
            stmt.setString(index, value.value());
        } catch (Exception e) {
            throw new JbException(
                "Could not get the string value for argument",
                e
            );
        }
    }
}
