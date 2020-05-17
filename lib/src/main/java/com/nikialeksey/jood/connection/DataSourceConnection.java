package com.nikialeksey.jood.connection;

import java.sql.Connection;

public interface DataSourceConnection {
    Connection connection();
    void fix();
    void unfix();
    int fixCount();
}
