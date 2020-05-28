package com.nikialeksey.jood.sql;

import com.nikialeksey.jood.JdException;

public interface Query {
    String asString() throws JdException;
}
