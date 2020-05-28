package com.nikialeksey.jood.sql;

import com.nikialeksey.jood.JdException;

import java.sql.Connection;
import java.sql.PreparedStatement;

public interface Sql {
    PreparedStatement prepare(Connection connection) throws JdException;
}
