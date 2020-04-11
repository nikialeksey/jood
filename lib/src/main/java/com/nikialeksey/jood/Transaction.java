package com.nikialeksey.jood;

public interface Transaction {
    void run() throws DbException;
}
