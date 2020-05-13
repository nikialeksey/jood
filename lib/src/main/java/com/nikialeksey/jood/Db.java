package com.nikialeksey.jood;

import com.nikialeksey.jood.args.Arg;
import com.nikialeksey.jood.sql.Sql;

public interface Db {
    QueryResult read(String query, Arg...args) throws DbException;

    QueryResult read(Sql sql) throws DbException;

    void write(String query, Arg... args) throws DbException;

    void write(Sql sql) throws DbException;

    QueryResult writeReturnGenerated(
        String query,
        Arg... args
    ) throws DbException;

    QueryResult writeReturnGenerated(Sql sql) throws DbException;

    void run(Transaction transaction) throws DbException;
}
