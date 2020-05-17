package com.nikialeksey.jood;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.nikialeksey.jood.args.IntArg;
import com.nikialeksey.jood.args.StringArg;
import com.nikialeksey.jood.sql.ReturnGeneratedSql;
import com.nikialeksey.jood.sql.SimpleSql;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimpleDbTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void writeWithGeneratedKeys() throws Exception {
        final Db db = new H2Db("writeWithGeneratedKeys");
        db.write(
            "CREATE TABLE t (" +
                "id INTEGER NOT NULL AUTO_INCREMENT PRIMARY  KEY, " +
                "name VARCHAR(10) NOT NULL" +
                ")"
        );

        try (
            final QueryResult qr = db.writeReturnGenerated(
                "INSERT INTO t(name) VALUES('lalala')"
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(1));
        }
    }

    @Test
    public void executeTransaction() throws Exception {
        final Db db = new H2Db("executeTransaction");
        db.write(
            "CREATE TABLE t (" +
                "id INTEGER NOT NULL PRIMARY  KEY, " +
                "name VARCHAR(10) NOT NULL" +
                ")"
        );

        db.run(() -> {
            for (int i = 0; i < 100; i++) {
                db.write(
                    "INSERT INTO t(id, name) VALUES(?, ?)",
                    new IntArg(i),
                    new StringArg(String.valueOf(i))
                );
            }
        });

        try (
            final QueryResult qr = db.read("SELECT COUNT(*) FROM t")
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(100));
        }
    }

    @Test
    public void transactionRollback() throws Exception {
        final Connection connection = DriverManager.getConnection(
            "jdbc:sqlite::memory:"
        );
        final Db db = new SimpleDb(() -> connection);
        final boolean savedAutoCommit = connection.getAutoCommit();

        try {
            db.run(() -> {
                throw new RuntimeException();
            });
            Assert.fail("Transaction must be fail");
        } catch (DbException e) {
            Assert.assertThat(
                connection.getAutoCommit(),
                IsEqual.equalTo(savedAutoCommit)
            );
        }
    }

    @Test
    public void simpleQueryToDataSourceSqliteDb() throws Exception {
        final ComboPooledDataSource ds = new ComboPooledDataSource();
        final File dbFile = folder.newFile();
        ds.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());

        final Db db = new SimpleDb(ds);
        db.write(new SimpleSql("CREATE TABLE a (n INTEGER NOT NULL)"));
        db.write(new SimpleSql("INSERT INTO a VALUES(5)"));
        try (
            final QueryResult qr = db.read(new SimpleSql("SELECT * FROM a"))
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(5));
        }
    }

    @Test
    public void simpleQueryToDataSourceH2Db() throws Exception {
        final ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:simpleQueryToDataSourceH2Db");

        final Db db = new SimpleDb(ds);
        db.write(new SimpleSql("CREATE TABLE a (n INTEGER NOT NULL)"));
        db.write(new SimpleSql("INSERT INTO a VALUES(5)"));
        try (
            final QueryResult qr = db.read(new SimpleSql("SELECT * FROM a"))
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(5));
        }
    }

    @Test
    public void writeWithGeneratedKeysOnH2DataSource() throws Exception {
        final ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:writeWithGeneratedKeysOnH2DataSource");

        final Db db = new SimpleDb(ds);
        db.write(
            new SimpleSql(
                "CREATE TABLE t (" +
                    "id INTEGER NOT NULL AUTO_INCREMENT PRIMARY  KEY, " +
                    "name VARCHAR(10) NOT NULL" +
                    ")"
            )
        );

        try (
            final QueryResult qr = db.writeReturnGenerated(
                new ReturnGeneratedSql(
                    "INSERT INTO t(name) VALUES('lalala')"
                )
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(1));
        }
    }

    @Test
    public void multiThreadedTransactionOnH2DataSourceDb() throws Exception {
        final ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:writeWithGeneratedKeys");
        final Db db = new SimpleDb(ds);

        db.write(new SimpleSql("CREATE TABLE t1 (n INTEGER NOT NULL)"));
        db.write(new SimpleSql("CREATE TABLE t2 (n INTEGER NOT NULL)"));

        final CountDownLatch latch = new CountDownLatch(1);
        final List<Future<?>> futures = new ArrayList<>();
        final ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            final int n = i;
            futures.add(
                pool.submit(() -> {
                    try {
                        latch.await();
                        db.run(() -> {
                            for (int j = 0; j < 10; j++) {
                                db.write(
                                    "INSERT INTO t1 (n) VALUES (?)",
                                    new IntArg(n)
                                );
                            }
                        });
                    } catch (DbException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
            );
            futures.add(
                pool.submit(() -> {
                    try {
                        latch.await();
                        for (int j = 0; j < 10; j++) {
                            db.write(
                                "INSERT INTO t2 (n) VALUES (?)",
                                new IntArg(n)
                            );
                        }
                    } catch (DbException | InterruptedException e) {
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
            final QueryResult qr1 = db.read(
                new SimpleSql("SELECT COUNT(*) FROM t1")
            );
            final QueryResult qr2 = db.read(
                new SimpleSql("SELECT COUNT(*) FROM t2")
            )
        ) {
            final ResultSet rs1 = qr1.rs();
            final ResultSet rs2 = qr2.rs();
            Assert.assertThat(rs1.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs2.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs1.getInt(1), IsEqual.equalTo(rs2.getInt(1)));
        }
    }

    @Test
    public void simpleNestedTransactionWithDataSource() throws Exception {
        final ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:simpleNestedTransactionWithDataSource");
        final Db db = new SimpleDb(ds);

        db.write(new SimpleSql("CREATE TABLE t (n INTEGER NOT NULL)"));

        db.run(() -> {
            db.run(() -> {
                db.write(new SimpleSql("INSERT INTO t(n) VALUES(1)"));
            });
            db.write(new SimpleSql("INSERT INTO t(n) VALUES(2)"));
        });

        try (
            final QueryResult qr = db.read(
                new SimpleSql("SELECT COUNT(*) FROM t")
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(2));
        }
    }

    @Test
    public void rollbackNestedTransactionWhenOutTransactionFailsWithDataSource() throws Exception {
        final ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:rollbackNestedTransactionWhenOutTransactionFailsWithDataSource");
        final Db db = new SimpleDb(ds);

        db.write(new SimpleSql("CREATE TABLE t (n INTEGER NOT NULL)"));

        try {
            db.run(() -> {
                db.run(() -> {
                    db.write(new SimpleSql("INSERT INTO t(n) VALUES(1)"));
                });
                db.write(new SimpleSql("INSERT INTO t(n) VALUES(2)"));
                throw new RuntimeException("Fail");
            });
            Assert.fail("Transaction must be failed!");
        } catch (DbException e) {
            // right way
        }

        try (
            final QueryResult qr = db.read(
                new SimpleSql("SELECT COUNT(*) FROM t")
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(0));
        }
    }

    @Test
    public void rollbackNestedTransactionWhenNestedTransactionFailsWithDataSource() throws Exception {
        final ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:rollbackNestedTransactionWhenNestedTransactionFailsWithDataSource");
        final Db db = new SimpleDb(ds);

        db.write(new SimpleSql("CREATE TABLE t (n INTEGER NOT NULL)"));

        try {
            db.run(() -> {
                db.run(() -> {
                    db.write(new SimpleSql("INSERT INTO t(n) VALUES(1)"));
                    throw new RuntimeException("Fail");
                });
                db.write(new SimpleSql("INSERT INTO t(n) VALUES(2)"));
            });
            Assert.fail("Transaction must be failed!");
        } catch (DbException e) {
            // right way
        }

        try (
            final QueryResult qr = db.read(
                new SimpleSql("SELECT COUNT(*) FROM t")
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(0));
        }
    }

    @Test
    public void simpleNestedTransactionWithFixedConnection() throws Exception {
        final Db db = new H2Db("simpleNestedTransactionWithFixedConnection");

        db.write(new SimpleSql("CREATE TABLE t (n INTEGER NOT NULL)"));

        db.run(() -> {
            db.run(() -> {
                db.write(new SimpleSql("INSERT INTO t(n) VALUES(1)"));
            });
            db.write(new SimpleSql("INSERT INTO t(n) VALUES(2)"));
        });

        try (
            final QueryResult qr = db.read(
                new SimpleSql("SELECT COUNT(*) FROM t")
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(2));
        }
    }

    @Test
    public void rollbackNestedTransactionWhenOutTransactionFailsWithFixedConnection() throws Exception {
        final Db db = new H2Db("rollbackNestedTransactionWhenOutTransactionFailsWithFixedConnection");

        db.write(new SimpleSql("CREATE TABLE t (n INTEGER NOT NULL)"));

        try {
            db.run(() -> {
                db.run(() -> {
                    db.write(new SimpleSql("INSERT INTO t(n) VALUES(1)"));
                });
                db.write(new SimpleSql("INSERT INTO t(n) VALUES(2)"));
                throw new RuntimeException("Fail");
            });
            Assert.fail("Transaction must be failed!");
        } catch (DbException e) {
            // right way
        }

        try (
            final QueryResult qr = db.read(
                new SimpleSql("SELECT COUNT(*) FROM t")
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(0));
        }
    }

    @Test
    public void rollbackNestedTransactionWhenNestedTransactionFailsWithFixedConnection() throws Exception {
        final Db db = new H2Db("rollbackNestedTransactionWhenNestedTransactionFailsWithFixedConnection");

        db.write(new SimpleSql("CREATE TABLE t (n INTEGER NOT NULL)"));

        try {
            db.run(() -> {
                db.run(() -> {
                    db.write(new SimpleSql("INSERT INTO t(n) VALUES(1)"));
                    throw new RuntimeException("Fail");
                });
                db.write(new SimpleSql("INSERT INTO t(n) VALUES(2)"));
            });
            Assert.fail("Transaction must be failed!");
        } catch (DbException e) {
            // right way
        }

        try (
            final QueryResult qr = db.read(
                new SimpleSql("SELECT COUNT(*) FROM t")
            )
        ) {
            final ResultSet rs = qr.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(0));
        }
    }

}