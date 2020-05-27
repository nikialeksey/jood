package com.nikialeksey.jood;

import com.nikialeksey.jood.connection.DataSourcePool;
import com.nikialeksey.jood.connection.FixedPool;
import com.nikialeksey.jood.connection.Pool;
import com.nikialeksey.jood.sql.Sql;
import org.cactoos.Scalar;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class JdDb implements Db {

    private final Pool pool;

    public JdDb(final Scalar<Connection> conn) {
        this(new FixedPool(conn));
    }

    public JdDb(final DataSource ds) {
        this(new DataSourcePool(ds));
    }

    public JdDb(final Pool pool) {
        this.pool = pool;
    }

    @Override
    public QueryResult read(final Sql sql) throws JbException {
        final Connection connection = pool.connection();
        try {
            final PreparedStatement statement = sql.prepare(connection);
            return new JdQueryResult(
                pool,
                connection,
                statement,
                statement.executeQuery()
            );
        } catch (Exception e) {
            throw new JbException(
                "Can not execute the read query.",
                e
            );
        }
    }

    @Override
    public void write(final Sql sql) throws JbException {
        final Connection connection = pool.connection();
        try (
            final PreparedStatement statement = sql.prepare(connection)
        ) {
            statement.executeUpdate();
        } catch (Exception e) {
            throw new JbException(
                "Can not execute the write query.",
                e
            );
        } finally {
            pool.release(connection);
        }
    }

    @Override
    public QueryResult writeReturnGenerated(final Sql sql) throws JbException {
        final Connection connection = pool.connection();
        try {
            final PreparedStatement statement = sql.prepare(connection);
            statement.executeUpdate();
            return new JdQueryResult(
                pool,
                connection,
                statement,
                statement.getGeneratedKeys()
            );
        } catch (Exception e) {
            throw new JbException(
                "Can not execute the write query.",
                e
            );
        }
    }

    @Override
    public void run(final Transaction transaction) throws JbException {
        try {
            final Connection connection = pool.connection();
            final boolean savedAutoCommit = connection.getAutoCommit();
            pool.fix(connection);

            try {
                connection.setAutoCommit(false);
                transaction.run();
                if (pool.fixCount() == 1) {
                    connection.commit();
                }
            } catch (Exception e) {
                connection.rollback();
                throw new JbException(
                    "Transaction could not be completed, rollback.",
                    e
                );
            } finally {
                connection.setAutoCommit(savedAutoCommit);
                pool.unfix(connection);
            }
        } catch (Exception e) {
            throw new JbException(
                "Can not execute the transaction.",
                e
            );
        }
    }
}
