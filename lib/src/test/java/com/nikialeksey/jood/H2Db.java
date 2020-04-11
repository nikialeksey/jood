package com.nikialeksey.jood;

import com.nikialeksey.jood.args.Arg;

import java.sql.DriverManager;

public class H2Db implements Db {

    private final Db origin;

    public H2Db() {
        this("test");
    }

    public H2Db(final String name) {
        this(
            new SimpleDb(() -> {
                try {
                    Class.forName("org.h2.Driver");
                } catch (ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                }

                return DriverManager.getConnection(
                    "jdbc:h2:mem:" + name,
                    "",
                    ""
                );
            })
        );
    }

    public H2Db(final Db origin) {
        this.origin = origin;
    }

    @Override
    public QueryResult read(
        final String query,
        final Arg... args
    ) throws DbException {
        return origin.read(query, args);
    }

    @Override
    public void write(
        final String query,
        final Arg... args
    ) throws DbException {
        origin.write(query, args);
    }

    @Override
    public QueryResult writeReturnGenerated(
        final String query,
        final Arg... args
    ) throws DbException {
        return origin.writeReturnGenerated(query, args);
    }

    @Override
    public void run(final Transaction transaction) throws DbException {
        origin.run(transaction);
    }
}
