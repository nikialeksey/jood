package com.nikialeksey.jood;

import com.nikialeksey.jood.args.Arg;
import org.cactoos.Scalar;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SimpleDb implements Db {
    private final Scalar<Connection> conn;

    public SimpleDb(final Scalar<Connection> conn) {
        this.conn = conn;
    }

    @Override
    public QueryResult read(final String query, final Arg... args) throws DbException {
        try {
            final PreparedStatement statement = conn.value().prepareStatement(query);
            for (int i = 1; i <= args.length; i++) {
                args[i - 1].printTo(statement, i);
            }
            return new SimpleQueryResult(
                statement,
                statement.executeQuery()
            );
        } catch (Exception e) {
            throw new DbException(
                String.format(
                    "Can not execute the read query '%s'.",
                    query
                ),
                e
            );
        }
    }

    @Override
    public void write(final String query, final Arg... args) throws DbException {
        try (
            final PreparedStatement statement = conn.value().prepareStatement(query)
        ) {
            for (int i = 1; i <= args.length; i++) {
                args[i - 1].printTo(statement, i);
            }
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DbException(
                String.format(
                    "Can not execute the write query '%s'.",
                    query
                ),
                e
            );
        }
    }
}
