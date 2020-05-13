package com.nikialeksey.jood.connection;

import com.nikialeksey.jood.DbException;

import java.sql.Connection;

public interface Pool {
    Connection connection() throws DbException;
    void release(Connection connection) throws DbException;
    void fix(Connection connection);
    void unfix(Connection connection) throws DbException;
}
