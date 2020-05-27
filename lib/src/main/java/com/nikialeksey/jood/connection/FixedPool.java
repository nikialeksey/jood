package com.nikialeksey.jood.connection;

import com.nikialeksey.jood.JbException;
import org.cactoos.Scalar;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedPool implements Pool {

    private final Scalar<Connection> connection;
    private final AtomicInteger fixCount;

    public FixedPool(final Scalar<Connection> connection) {
        this(
            connection,
            new AtomicInteger(0)
        );
    }

    public FixedPool(
        final Scalar<Connection> connection,
        final AtomicInteger fixCount
    ) {
        this.connection = connection;
        this.fixCount = fixCount;
    }

    @Override
    public Connection connection() throws JbException {
        try {
            return connection.value();
        } catch (Exception e) {
            throw new JbException(
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
        fixCount.incrementAndGet();
    }

    @Override
    public void unfix(final Connection connection) {
        fixCount.decrementAndGet();
    }

    @Override
    public int fixCount() {
        return fixCount.get();
    }
}
