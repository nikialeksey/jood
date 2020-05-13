package com.nikialeksey.jood.connection;

import com.nikialeksey.jood.DbException;
import org.cactoos.Scalar;

import java.sql.Connection;

public class FixedPool implements Pool {

    private final Scalar<Connection> connection;

    public FixedPool(final Scalar<Connection> connection) {
        this.connection = connection;
    }

    @Override
    public Connection connection() throws DbException {
        try {
            return connection.value();
        } catch (Exception e) {
            throw new DbException(
                "Could not get the connection.",
                e
            );
        }
    }

    @Override
    public void release(final Connection connection) {
        // ignore
    }

    @Override
    public void fix(final Connection connection) {
        // ignore
    }

    @Override
    public void unfix(final Connection connection) {
        // ignore
    }
}
