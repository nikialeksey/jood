package com.nikialeksey.jood;

import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import java.sql.ResultSet;

public class SimpleDbTest {

    @Test
    public void writeWithGeneratedKeys() throws Exception {
        final Db db = new H2Db();
        db.write("CREATE TABLE t (" +
            "id INTEGER NOT NULL AUTO_INCREMENT PRIMARY  KEY, " +
            "name VARCHAR(10) NOT NULL" +
            ")");

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

}