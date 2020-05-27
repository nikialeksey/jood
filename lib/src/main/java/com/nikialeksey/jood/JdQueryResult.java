package com.nikialeksey.jood;

import com.nikialeksey.jood.connection.Pool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdQueryResult implements QueryResult {

    private final Pool pool;
    private final Connection connection;
    private final Statement statement;
    private final ResultSet resultSet;

    public JdQueryResult(
        final Pool pool,
        final Connection connection,
        final Statement statement,
        final ResultSet resultSet
    ) {
        this.pool = pool;
        this.connection = connection;
        this.statement = statement;
        this.resultSet = resultSet;
    }

    @Override
    public ResultSet rs() {
        return resultSet;
    }

    @Override
    public Statement stmnt() {
        return statement;
    }

    @Override
    public void close() throws JbException {
        try {
            resultSet.close();
            statement.close();
            pool.release(connection);
        } catch (SQLException e) {
            throw new JbException("Can not close the result set and statement.", e);
        }
    }
}
