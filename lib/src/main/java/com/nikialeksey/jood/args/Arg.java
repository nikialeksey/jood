package com.nikialeksey.jood.args;

import com.nikialeksey.jood.JdException;

import java.sql.PreparedStatement;

public interface Arg {
    void printTo(PreparedStatement stmt, int index) throws JdException;
}
