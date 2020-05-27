package com.nikialeksey.jood;

public interface Migration {
    int number();

    void execute(Db db) throws JbException;
}
