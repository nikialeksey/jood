package com.nikialeksey.jood;

import com.nikialeksey.jood.args.Arg;
import com.nikialeksey.jood.connection.DataSourcePool;
import com.nikialeksey.jood.connection.FixedPool;
import com.nikialeksey.jood.connection.Pool;
import com.nikialeksey.jood.sql.ReturnGeneratedSql;
import com.nikialeksey.jood.sql.SimpleSql;
import com.nikialeksey.jood.sql.Sql;
import org.cactoos.Scalar;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SimpleDb implements Db {

    private final Pool pool;

    public SimpleDb(final Scalar<Connection> conn) {
        this(new FixedPool(conn));
    }

    public SimpleDb(final DataSource ds) {
        this(new DataSourcePool(ds));
    }

    public SimpleDb(final Pool pool) {
        this.pool = pool;
    }

    @Override
    public QueryResult read(final String query, final Arg... args) throws DbException {
        final Connection connection = pool.connection();
        try {
            final PreparedStatement statement = new SimpleSql(query, args)
                .prepare(connection);
            return new SimpleQueryResult(
                pool,
                connection,
                statement,
                statement.executeQuery()
            );
        } catch (Exception e) {
            throw new DbException(
                String.format(
                    "Can not execute the read query '%s'.",
                    query
                ),
                e
            );
        }
    }

    @Override
    public QueryResult read(final Sql sql) throws DbException {
        final Connection connection = pool.connection();
        try {
            final PreparedStatement statement = sql.prepare(connection);
            return new SimpleQueryResult(
                pool,
                connection,
                statement,
                statement.executeQuery()
            );
        } catch (Exception e) {
            throw new DbException(
                "Can not execute the read query.",
                e
            );
        }
    }

    @Override
    public void write(final String query, final Arg... args) throws DbException {
        final Connection connection = pool.connection();
        try (
            final PreparedStatement statement = new SimpleSql(query, args)
                .prepare(connection)
        ) {
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DbException(
                String.format(
                    "Can not execute the write query '%s'.",
                    query
                ),
                e
            );
        } finally {
            pool.release(connection);
        }
    }

    @Override
    public void write(final Sql sql) throws DbException {
        final Connection connection = pool.connection();
        try (
            final PreparedStatement statement = sql.prepare(connection)
        ) {
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DbException(
                "Can not execute the write query.",
                e
            );
        } finally {
            pool.release(connection);
        }
    }

    @Override
    public QueryResult writeReturnGenerated(
        final String query,
        final Arg... args
    ) throws DbException {
        final Connection connection = pool.connection();
        try {
            final PreparedStatement statement = new ReturnGeneratedSql(
                query,
                args
            ).prepare(connection);
            statement.executeUpdate();
            return new SimpleQueryResult(
                pool,
                connection,
                statement,
                statement.getGeneratedKeys()
            );
        } catch (Exception e) {
            throw new DbException(
                String.format(
                    "Can not execute the write query '%s'.",
                    query
                ),
                e
            );
        }
    }

    @Override
    public QueryResult writeReturnGenerated(final Sql sql) throws DbException {
        final Connection connection = pool.connection();
        try {
            final PreparedStatement statement = sql.prepare(connection);
            statement.executeUpdate();
            return new SimpleQueryResult(
                pool,
                connection,
                statement,
                statement.getGeneratedKeys()
            );
        } catch (Exception e) {
            throw new DbException(
                "Can not execute the write query.",
                e
            );
        }
    }

    @Override
    public void run(final Transaction transaction) throws DbException {
        try {
            final Connection connection = pool.connection();
            final boolean savedAutoCommit = connection.getAutoCommit();
            pool.fix(connection);

            try {
                connection.setAutoCommit(false);
                transaction.run();
                connection.commit();
            } catch (DbException e) {
                connection.rollback();
                throw new DbException(
                    "Transaction could not be completed, rollback.",
                    e
                );
            } finally {
                connection.setAutoCommit(savedAutoCommit);
                pool.unfix(connection);
            }
        } catch (Exception e) {
            throw new DbException(
                "Can not execute the transaction.",
                e
            );
        }
    }
}
