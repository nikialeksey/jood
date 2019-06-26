package com.nikialeksey.jood;

import org.cactoos.Scalar;
import org.cactoos.scalar.Solid;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class SqliteDb implements Db {

    private final Scalar<Connection> conn;

    public SqliteDb() {
        this(":memory:");
    }

    public SqliteDb(final File dbFile) {
        this(dbFile.getAbsolutePath());
    }

    public SqliteDb(final String name) {
        this(
            new Solid<>(
                () -> DriverManager.getConnection(
                    String.format("jdbc:sqlite:%s", name)
                )
            )
        );
    }

    public SqliteDb(final Scalar<Connection> conn) {
        this.conn = conn;
    }

    @Override
    public QueryResult read(final String query, final String[] args) throws DbException {
        try {
            final PreparedStatement statement = conn.value().prepareStatement(query);
            for (int i = 1; i <= args.length; i++) {
                statement.setString(i, args[i - 1]);
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
    public void write(final String query, final String[] args) throws DbException {
        try (
            final PreparedStatement statement = conn.value().prepareStatement(query)
        ) {
            for (int i = 1; i <= args.length; i++) {
                statement.setString(i, args[i - 1]);
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
