package com.nikialeksey.jood.connection;

import com.nikialeksey.jood.JbException;

import java.sql.Connection;

public interface Pool {
    Connection connection() throws JbException;
    void release(Connection connection) throws JbException;
    void fix(Connection connection);
    void unfix(Connection connection) throws JbException;
    int fixCount() throws JbException;
}
