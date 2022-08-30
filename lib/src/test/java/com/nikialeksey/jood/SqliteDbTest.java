package com.nikialeksey.jood;

import com.nikialeksey.jood.args.IntArg;
import com.nikialeksey.jood.args.LongArg;
import com.nikialeksey.jood.args.StringArg;
import com.nikialeksey.jood.sql.JdSql;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import java.sql.ResultSet;

public class SqliteDbTest {
    @Test
    public void select() throws Exception {
        final Db db = new SqliteDb();
        db.write(new JdSql("CREATE TABLE a (`name` TEXT NOT NULL)"));
        db.write(
            new JdSql("INSERT INTO a (`name`) VALUES (?)", new StringArg("A"))
        );
        try (
            final QueryResult result = db.read(
                new JdSql("SELECT `name` FROM a")
            )
        ) {
            final ResultSet rs = result.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getString("name"), IsEqual.equalTo("A"));
        }
    }

    @Test
    public void queryWithLimit() throws Exception {
        final Db db = new SqliteDb();
        db.write(new JdSql("CREATE TABLE a (name TEXT NOT NULL)"));
        db.write(
            new JdSql("INSERT INTO a (name) VALUES (?)", new StringArg("A"))
        );
        db.write(
            new JdSql("INSERT INTO a (name) VALUES (?)", new StringArg("B"))
        );
        db.write(
            new JdSql("INSERT INTO a (name) VALUES (?)", new StringArg("C"))
        );
        db.write(
            new JdSql("INSERT INTO a (name) VALUES (?)", new StringArg("D"))
        );

        try (
            final QueryResult result = db.read(
                new JdSql("SELECT name FROM a LIMIT ?", new IntArg(1))
            )
        ) {
            final ResultSet rs = result.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.next(), IsEqual.equalTo(false));
        }
    }

    @Test
    public void insertLong() throws Exception {
        final Db db = new SqliteDb();
        db.write(new JdSql("CREATE TABLE a (number BIGINT NOT NULL)"));
        db.write(
            new JdSql("INSERT INTO a (number) VALUES (?)", new LongArg(1))
        );
        db.write(
            new JdSql("INSERT INTO a (number) VALUES (?)", new LongArg(2))
        );
        db.write(
            new JdSql("INSERT INTO a (number) VALUES (?)", new LongArg(3))
        );
        db.write(
            new JdSql("INSERT INTO a (number) VALUES (?)", new LongArg(4))
        );

        try (
            final QueryResult result = db.read(
                new JdSql("SELECT count(*) FROM a")
            )
        ) {
            final ResultSet rs = result.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getInt(1), IsEqual.equalTo(4));
        }
    }

    @Test
    public void alterRenameUsingWriteWithoutExceptions() throws Exception {
        // case from https://github.com/xerial/sqlite-jdbc/issues/497
        final Db db = new SqliteDb();
        db.write(new JdSql("CREATE TABLE t (t TEXT)"));
        db.write(new JdSql("ALTER TABLE t RENAME TO t2"));
    }
}