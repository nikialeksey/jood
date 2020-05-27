package com.nikialeksey.jood;

import com.nikialeksey.jood.sql.Sql;
import org.cactoos.scalar.Solid;

import java.sql.DriverManager;

public class H2Db implements Db {

    private final Db origin;

    public H2Db() {
        this("test");
    }

    public H2Db(final String name) {
        this(
            new JdDb(
                new Solid<>(() -> {
                    try {
                        Class.forName("org.h2.Driver");
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    return DriverManager.getConnection(
                        "jdbc:h2:mem:" + name,
                        "",
                        ""
                    );
                })
            )
        );
    }

    public H2Db(final Db origin) {
        this.origin = origin;
    }

    @Override
    public QueryResult read(final Sql sql) throws JbException {
        return origin.read(sql);
    }

    @Override
    public void write(final Sql sql) throws JbException {
        origin.write(sql);
    }

    @Override
    public QueryResult writeReturnGenerated(final Sql sql) throws JbException {
        return origin.writeReturnGenerated(sql);
    }

    @Override
    public void run(final Transaction transaction) throws JbException {
        origin.run(transaction);
    }
}
