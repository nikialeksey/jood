package com.nikialeksey.jood.sql;

import com.nikialeksey.jood.JdException;
import org.cactoos.Scalar;

public class JdQuery implements Query {

    private final Scalar<String> query;

    public JdQuery(final String query) {
        this(() -> query);
    }

    public JdQuery(final Scalar<String> query) {
        this.query = query;
    }

    @Override
    public String asString() throws JdException {
        try {
            return query.value();
        } catch (Exception e) {
            throw new JdException(
                "Could not get the query string.",
                e
            );
        }
    }
}
