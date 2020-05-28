package com.nikialeksey.jood;

import com.nikialeksey.jood.sql.Sql;

public interface Db {

    QueryResult read(Sql sql) throws JdException;

    void write(Sql sql) throws JdException;

    QueryResult writeReturnGenerated(Sql sql) throws JdException;

    void run(Transaction transaction) throws JdException;
}
