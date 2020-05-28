package com.nikialeksey.jood.sql;

import com.nikialeksey.jood.JdException;
import com.nikialeksey.jood.args.Arg;
import org.cactoos.Scalar;
import org.cactoos.list.ListOf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class ReturnGeneratedSql implements Sql {
    private final Scalar<String> query;
    private final List<Arg> args;

    public ReturnGeneratedSql(final String query, final Arg... args) {
        this(
            () -> query,
            new ListOf<Arg>(args)
        );
    }

    public ReturnGeneratedSql(
        final Scalar<String> query,
        final List<Arg> args
    ) {
        this.query = query;
        this.args = args;
    }

    @Override
    public PreparedStatement prepare(
        final Connection connection
    ) throws JdException {
        try {
            final PreparedStatement statement = connection.prepareStatement(
                query.value(),
                Statement.RETURN_GENERATED_KEYS
            );
            for (int i = 1; i <= args.size(); i++) {
                args.get(i - 1).printTo(statement, i);
            }
            return statement;
        } catch (Exception e) {
            throw new JdException(
                "Could not prepare the statement.",
                e
            );
        }
    }
}
