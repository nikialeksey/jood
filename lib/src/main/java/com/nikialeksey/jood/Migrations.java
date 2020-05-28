package com.nikialeksey.jood;

public interface Migrations {
    void apply(int number, Db db) throws JdException;
}
