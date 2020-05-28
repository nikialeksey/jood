package com.nikialeksey.jood;

import org.cactoos.list.ListOf;

import java.util.Collection;

public class JdMigrations implements Migrations {

    private final Collection<Migration> migrations;

    public JdMigrations(final Migration... migrations) {
        this(new ListOf<>(migrations));
    }

    public JdMigrations(final Collection<Migration> migrations) {
        this.migrations = migrations;
    }

    @Override
    public void apply(
        final int number,
        final Db db
    ) throws JdException {
        for (final Migration migration : migrations) {
            if (migration.number() == number) {
                migration.execute(db);
            }
        }
    }
}
