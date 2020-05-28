package com.nikialeksey.jood;

import com.nikialeksey.jood.sql.Sql;
import org.cactoos.Scalar;
import org.cactoos.scalar.Solid;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class SqliteDb implements Db {

    private final JdDb db;

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
        this(new JdDb(conn));
    }

    public SqliteDb(final JdDb db) {
        this.db = db;
    }

    @Override
    public QueryResult read(final Sql sql) throws JdException {
        return db.read(sql);
    }

    @Override
    public void write(final Sql sql) throws JdException {
        db.write(sql);
    }

    @Override
    public QueryResult writeReturnGenerated(final Sql sql) throws JdException {
        return db.writeReturnGenerated(sql);
    }

    @Override
    public void run(final Transaction transaction) throws JdException {
        db.run(transaction);
    }
}
