package com.nikialeksey.jood.connection;

import com.nikialeksey.jood.JdException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourcePool implements Pool {

    private final DataSource source;
    private final ThreadLocal<DataSourceConnection> fixed;

    public DataSourcePool(final DataSource source) {
        this(source, new ThreadLocal<>());
    }

    public DataSourcePool(final DataSource source, final ThreadLocal<DataSourceConnection> fixed) {
        this.source = source;
        this.fixed = fixed;
    }

    @Override
    public Connection connection() throws JdException {
        try {
            final Connection result;
            if (fixed.get() != null) {
                result = fixed.get().connection();
            } else {
                result = source.getConnection();
            }
            return result;
        } catch (SQLException e) {
            throw new JdException(
                "Could not get the connection from data source.",
                e
            );
        }
    }

    @Override
    public void release(final Connection connection) throws JdException {
        if (fixed.get() == null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new JdException(
                    "Could not release the connection.",
                    e
                );
            }
        }
    }

    @Override
    public void fix(final Connection connection) {
        if (fixed.get() == null) {
            fixed.set(new JdDataSourceConnection(connection));
        }
        fixed.get().fix();
    }

    @Override
    public void unfix(final Connection connection) throws JdException {
        if (fixed.get() != null) {
            fixed.get().unfix();
            if (fixed.get().fixCount() == 0) {
                fixed.remove();
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new JdException(
                        "Could not close the fixed connection.",
                        e
                    );
                }
            }
        } else {
            throw new JdException(
                "You trying to unfix the connection when there is no fixed"
            );
        }
    }

    @Override
    public int fixCount() throws JdException {
        if (fixed.get() != null) {
            return fixed.get().fixCount();
        } else {
            throw new JdException(
                "Try to get fix count when there is not fixed"
            );
        }
    }
}
