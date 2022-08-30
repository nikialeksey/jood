# Jood

![Elegant Objects Respected Here](http://www.elegantobjects.org/badge.svg)

![nullfree status](https://youshallnotpass.dev/nullfree/nikialeksey/jood)
![staticfree status](https://youshallnotpass.dev/staticfree/nikialeksey/jood)
![allpublic status](https://youshallnotpass.dev/allpublic/nikialeksey/jood)
![setterfree status](https://youshallnotpass.dev/setterfree/nikialeksey/jood)
![nomultiplereturn status](https://youshallnotpass.dev/nomultiplereturn/nikialeksey/jood)

[![Lib version](https://img.shields.io/maven-central/v/com.nikialeksey/jood.svg?label=maven)](https://maven-badges.herokuapp.com/maven-central/com.nikialeksey/jood)
[![Build Status](https://travis-ci.org/nikialeksey/jood.svg?branch=master)](https://travis-ci.org/nikialeksey/jood)
[![codecov](https://codecov.io/gh/nikialeksey/jood/branch/master/graph/badge.svg)](https://codecov.io/gh/nikialeksey/jood)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/nikialeksey/jood/blob/master/LICENSE)

![logo](https://raw.githubusercontent.com/nikialeksey/jood/master/assets/github-logo.png)

## What is it?
**Jood** is object oriented sql-database library written in Java.

## Getting started

#### Gradle
```groovy
implementation 'com.nikialeksey:jood:x.y.z'
```

#### Maven
```xml
<dependency>
  <groupId>com.nikialeksey</groupId>
  <artifactId>jood</artifactId>
  <version>x.y.z</version>
</dependency>
```

Where `x.y.z` actual version from
[![Lib version](https://img.shields.io/maven-central/v/com.nikialeksey/jood.svg?label=maven)](https://maven-badges.herokuapp.com/maven-central/com.nikialeksey/jood)

### Connection
```java
Connection connection = DriverManager.getConnection(...);
Db db = new JdDb(() -> connection);
```

For example, connect to `sqlite` in-memory:
```java
Connection connection = DriverManager.getConnection(
    "jdbc:sqlite::memory:"
);
Db db = new JdDb(() -> connection);
```

### Data source
```java
DataSource ds = ...;
Db db = new JdDb(ds);
```

For example, connect to `h2` in-memory through the `c3p0` pooled data source:
```java
ComboPooledDataSource ds = new ComboPooledDataSource();
ds.setJdbcUrl("jdbc:h2:mem:db_name");
Db db = new JdDb(ds);
```

### Simple queries
```java
Db db = ...;
db.write(new JdSql("CREATE TABLE a (n INTEGER NOT NULL)"));
db.write(new JdSql("INSERT INTO a(n) VALUES(5)"));
try (
    QueryResult qr = db.read(new JdSql("SELECT * FROM a"))
) {
    ResultSet rs = qr.rs();
    while (rs.next()) {
        String n = rs.getString("n");
    }
}
```

### Query arguments
```java
db.write(
    new JdSql(
        "INSERT INTO a(n) VALUES(?)",
        new IntArg(5)
    )
);
```

### Transactions
```java
Db db = ...;
db.run(() -> { // all changes inside will be commited after successfull execution
    db.run(() -> { // transactions could be inner
        for (int i = 0; i < 1000; i++) {
            db.write(new JdSql("INSERT INTO a(n) VALUES(1)"));
        }
    });
    for (int i = 0; i < 3000; i++) {
        db.write(new JdSql("INSERT INTO a(n) VALUES(2)"));
    }
});
```

### Migrations
```java
Db origin = ...;
Db db = new MigrationsDb(
    origin,
    new JdMigrations(
        new Migration() {
            @Override
            public int number() {
                return 0; // migration index
            }

            @Override
            public void execute(final Db db) throws JbException {
                db.write(
                    new JdSql(
                        "CREATE TABLE user (name TEXT NOT NULL)"
                    )
                );
            }
        },
        new Migration() {
            @Override
            public int number() {
                return 1; // migration index
            }

            @Override
            public void execute(final Db db) throws JbException {
                db.write(
                    new JdSql(
                        "ALTER TABLE user " +
                            "ADD lastname TEXT NOT NULL DEFAULT ''"
                    )
                );
            }
        }  
    ),
    2 // db version number
)
```