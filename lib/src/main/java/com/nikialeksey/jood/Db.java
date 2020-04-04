package com.nikialeksey.jood;

import com.nikialeksey.jood.args.Arg;

public interface Db {
    QueryResult read(String query, Arg...args) throws DbException;

    void write(String query, Arg... args) throws DbException;
}
