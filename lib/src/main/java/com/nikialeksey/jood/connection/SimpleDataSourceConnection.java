package com.nikialeksey.jood.connection;

import org.cactoos.list.ListOf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleDataSourceConnection implements DataSourceConnection {

    private final Connection connection;
    private final AtomicInteger fixCount;

    public SimpleDataSourceConnection(
        final Connection connection
    ) {
        this(connection, new AtomicInteger(0));
    }

    public SimpleDataSourceConnection(
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
