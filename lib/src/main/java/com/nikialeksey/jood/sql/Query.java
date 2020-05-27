package com.nikialeksey.jood.sql;

import com.nikialeksey.jood.JbException;

public interface Query {
    String asString() throws JbException;
}
