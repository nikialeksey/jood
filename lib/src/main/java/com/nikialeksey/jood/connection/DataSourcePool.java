package com.nikialeksey.jood.connection;

import com.nikialeksey.jood.DbException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourcePool implements Pool {

    private final DataSource source;
    private final ThreadLocal<Connection> fixed;

    public DataSourcePool(final DataSource source) {
        this(source, new ThreadLocal<>());
    }

    public DataSourcePool(final DataSource source, final ThreadLocal<Connection> fixed) {
        this.source = source;
        this.fixed = fixed;
    }

    @Override
    @SuppressWarnings("nullfree")
    public Connection connection() throws DbException {
        try {
            final Connection result;
            if (fixed.get() != null) {
                result = fixed.get();
            } else {
                result = source.getConnection();
            }
            return result;
        } catch (SQLException e) {
            throw new DbException(
                "Could not get the connection from data source.",
                e
            );
        }
    }

    @Override
    public void release(final Connection connection) throws DbException {
        try {
            if (fixed.get() != connection) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DbException(
                "Could not release the connection.",
                e
            );
        }
    }

    @Override
    public void fix(final Connection connection) {
        fixed.set(connection);
    }

    @Override
    public void unfix(final Connection connection) throws DbException {
        fixed.remove();
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DbException(
                "Could not close the fixed connection.",
                e
            );
        }
    }
}
