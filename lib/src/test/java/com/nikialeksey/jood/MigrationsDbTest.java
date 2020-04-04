package com.nikialeksey.jood;

import com.nikialeksey.jood.args.StringArg;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import java.sql.ResultSet;

public class MigrationsDbTest {
    @Test
    public void simpleMigrations() throws Exception {
        final Db db = new MigrationsDb(
            new SqliteDb(),
            new SimpleMigrations(
                new Migration() {
                    @Override
                    public int number() {
                        return 0;
                    }

                    @Override
                    public void execute(final Db db) throws DbException {
                        db.write("CREATE TABLE names (name TEXT NOT NULL)");
                    }
                },
                new Migration() {
                    @Override
                    public int number() {
                        return 1;
                    }

                    @Override
                    public void execute(final Db db) throws DbException {
                        db.write("ALTER TABLE names ADD lastname TEXT NOT NULL DEFAULT ''");
                    }
                }
            ),
            2
        );

        db.write(
            "INSERT INTO names VALUES(?, ?)",
            new StringArg("Alexey"),
            new StringArg("Nikitin")
        );
        try (
            final QueryResult queryResult = db.read("SELECT * FROM names")
        ) {
            final ResultSet rs = queryResult.rs();
            Assert.assertThat(rs.next(), IsEqual.equalTo(true));
            Assert.assertThat(rs.getString("name"), IsEqual.equalTo("Alexey"));
            Assert.assertThat(rs.getString("lastname"), IsEqual.equalTo("Nikitin"));
        }
    }
}