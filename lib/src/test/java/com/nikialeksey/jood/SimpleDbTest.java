package com.nikialeksey.jood;

import com.nikialeksey.jood.args.IntArg;
import com.nikialeksey.jood.args.StringArg;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class SimpleDbTest {

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
            Assert.fail("Transaction should be fail");
        } catch (DbException e) {
            Assert.assertThat(
                connection.getAutoCommit(),
                IsEqual.equalTo(savedAutoCommit)
            );
        }
    }

}