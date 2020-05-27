package com.nikialeksey.jood.connection;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

public class JdDataSourceConnection implements DataSourceConnection {

    private final Connection connection;
    private final AtomicInteger fixCount;

    public JdDataSourceConnection(
        final Connection connection
    ) {
        this(connection, new AtomicInteger(0));
    }

    public JdDataSourceConnection(
        final Connection connection,
        final AtomicInteger fixCount
    ) {
        this.connection = connection;
        this.fixCount = fixCount;
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void fix() {
        fixCount.incrementAndGet();
    }

    @Override
    public void unfix() {
        fixCount.decrementAndGet();
    }

    @Override
    public int fixCount() {
        return fixCount.get();
    }
}
