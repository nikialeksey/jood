package com.nikialeksey.jood;

import com.nikialeksey.jood.sql.Sql;

public interface Db {

    QueryResult read(Sql sql) throws JbException;

    void write(Sql sql) throws JbException;

    QueryResult writeReturnGenerated(Sql sql) throws JbException;

    void run(Transaction transaction) throws JbException;
}
