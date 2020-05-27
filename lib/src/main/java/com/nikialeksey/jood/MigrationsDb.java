package com.nikialeksey.jood;

import com.nikialeksey.jood.args.IntArg;
import com.nikialeksey.jood.sql.JdSql;
import com.nikialeksey.jood.sql.Sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MigrationsDb implements Db {

    private final Db origin;
    private final Migrations migrations;
    private final int dbVersion;

    public MigrationsDb(
        final Db origin,
        final Migrations migrations,
        final int dbVersion
    ) {
        this.origin = origin;
        this.migrations = migrations;
        this.dbVersion = dbVersion;
    }

    @Override
    public QueryResult read(final Sql sql) throws JbException {
        ensureMigrations();
        return origin.read(sql);
    }

    @Override
    public void write(final Sql sql) throws JbException {
        ensureMigrations();
        origin.write(sql);
    }

    @Override
    public QueryResult writeReturnGenerated(final Sql sql) throws JbException {
        ensureMigrations();
        return origin.writeReturnGenerated(sql);
    }

    @Override
    public void run(final Transaction transaction) throws JbException {
        ensureMigrations();
        origin.run(transaction);
    }

    private synchronized void ensureMigrations() throws JbException {
        ensureMigrationsTable();
        int oldVersion = oldVersion();
        if (oldVersion != dbVersion) {
            for (int version = oldVersion; version < dbVersion; version++) {
                migrations.apply(version, origin);
            }
            origin.write(
                new JdSql(
                    "UPDATE migrations SET version = ?",
                    new IntArg(dbVersion)
                )
            );
        }
    }

    private int oldVersion() throws JbException {
        try (
            final QueryResult result = origin.read(
                new JdSql("SELECT version FROM migrations")
            )
        ) {
            final ResultSet rs = result.rs();
            rs.next();
            return rs.getInt("version");
        } catch (SQLException e) {
            throw new JbException("Can not get the old db version.", e);
        }
    }

    private void ensureMigrationsTable() throws JbException {
        try (
            final QueryResult ignored = origin.read(
                new JdSql("SELECT 1 FROM migrations")
            )
        ) {
            // yeah, migrations table exists
        } catch (JbException ignored) {
            // Most likely reason for exception in this place - table
            // migrations does not exists, lets create and initialize it.
            origin.write(
                new JdSql(
                    "CREATE TABLE migrations (version INTEGER NOT NULL DEFAULT 0)"
                )
            );
            try (
                final QueryResult result = origin.read(
                    new JdSql(
                        "SELECT 1 FROM migrations"
                    )
                )
            ) {
                final ResultSet rs = result.rs();
                if (!rs.next()) {
                    origin.write(
                        new JdSql(
                            "INSERT INTO migrations (version) VALUES (0)"
                        )
                    );
                }
            } catch (SQLException e) {
                throw new JbException("Can not initialize migrations table.", e);
            }
        }
    }
}
