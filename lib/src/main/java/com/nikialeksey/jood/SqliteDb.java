package com.nikialeksey.jood;

import com.nikialeksey.jood.args.Arg;
import org.cactoos.Scalar;
import org.cactoos.scalar.Solid;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class SqliteDb implements Db {

    private final SimpleDb db;

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
        this(new SimpleDb(conn));
    }

    public SqliteDb(final SimpleDb db) {
        this.db = db;
    }

    @Override
    public QueryResult read(final String query, final Arg... args) throws
        DbException {
        return db.read(query, args);
    }

    @Override
    public void write(final String query, final Arg... args) throws DbException {
        db.write(query, args);
    }

    @Override
    public QueryResult writeReturnGenerated(
        final String query,
        final Arg... args
    ) throws DbException {
        return db.writeReturnGenerated(query, args);
    }

    @Override
    public void run(final Transaction transaction) throws DbException {
        db.run(transaction);
    }
}
