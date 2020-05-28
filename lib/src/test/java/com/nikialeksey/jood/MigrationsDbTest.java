package com.nikialeksey.jood;

import com.nikialeksey.jood.args.IntArg;
import com.nikialeksey.jood.args.StringArg;
import com.nikialeksey.jood.sql.JdSql;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MigrationsDbTest {
    @Test
    public void simpleMigrations() throws Exception {
        final Db db = new MigrationsDb(
            new SqliteDb(),
            new JdMigrations(
                new Migration() {
                    @Override
                    public int number() {
                        return 0;
                    }

                    @Override
                    public void execute(final Db db) throws JdException {
                        db.write(
                            new JdSql(
                                "CREATE TABLE names (name TEXT NOT NULL)"
                            )
                        );
                    }
                },
                new Migration() {
                    @Override
                    public int number() {
                        return 1;
                    }

                    @Override
                    public void execute(final Db db) throws JdException {
                        db.write(
                            new JdSql(
                                "ALTER TABLE names " +
                                    "ADD lastname TEXT NOT NULL DEFAULT ''"
                            )
                        );
                    }
                }
            ),
            2
        );

        db.write(
            new JdSql(
                "INSERT INTO names VALUES(?, ?)",
                new StringArg("Alexey"),
                new StringArg("Nikitin")
            )
        );
        try (
            final QueryResult queryResult = db.read(
                new JdSql("SELECT * FROM names")
            )
        ) {
            final ResultSet rs = queryResult.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getString("name"), IsEqual.equalTo("Alexey"));
            Assert.assertThat(rs.getString("lastname"), IsEqual.equalTo("Nikitin"));
        }
    }

    @Test
    public void multithreadingMigrations() throws Exception {
        final ExecutorService pool = Executors.newFixedThreadPool(10);

        final Db db = new MigrationsDb(
            new SqliteDb(),
            new JdMigrations(
                new Migration() {
                    @Override
                    public int number() {
                        return 0;
                    }

                    @Override
                    public void execute(final Db db) throws JdException {
                        db.write(
                            new JdSql(
                                "CREATE TABLE nums (num INTEGER NOT NULL)"
                            )
                        );
                    }
                }
            ),
            1
        );

        final CountDownLatch latch = new CountDownLatch(1);
        final List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            futures.add(
                pool.submit(() -> {
                    try {
                        latch.await();
                        db.write(
                            new JdSql(
                                "INSERT INTO nums (num) VALUES (?)",
                                new IntArg(1)
                            )
                        );
                    } catch (JdException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
            );
        }
        latch.countDown();
        for (final Future<?> future : futures) {
            future.get();
        }
        try (
            final QueryResult qr = db.read(
                new JdSql("SELECT COUNT(*) FROM nums")
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(100));
        }
    }
}