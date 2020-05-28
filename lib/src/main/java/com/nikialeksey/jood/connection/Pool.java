package com.nikialeksey.jood.connection;

import com.nikialeksey.jood.JdException;

import java.sql.Connection;

public interface Pool {
    Connection connection() throws JdException;
    void release(Connection connection) throws JdException;
    void fix(Connection connection);
    void unfix(Connection connection) throws JdException;
    int fixCount() throws JdException;
}
