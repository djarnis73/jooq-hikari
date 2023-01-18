package jooqbug;


import com.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.jupiter.api.Assertions.*;

public class JooqHikariMysqlTest {

    static {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
    }

    static MySQLContainer mysql = new MySQLContainer<>("mysql:8.0.23");
    static DSLContext jooq;
    static HikariDataSource ds;

    @BeforeAll
    public static void beforeAll() {
        mysql.start();
        ds = new HikariDataSource();
        ds.setJdbcUrl(mysql.getJdbcUrl());
        ds.setUsername(mysql.getUsername());
        ds.setPassword(mysql.getPassword());
        jooq = DSL.using(ds, SQLDialect.MYSQL);
    }

    @AfterAll
    public static void afterAll() {
        if (mysql.isRunning()) {
            ds.close();
            mysql.stop();
        }
    }

    @Test
    public void timeoutTestPlain() {
        // plain queries are fine
        DataAccessException e = assertThrows(DataAccessException.class, () ->
                jooq.query("select sleep(10)").queryTimeout(1).execute()
        );
        // verify that the underlying exception is a MySQLTimeoutException
        assertEquals(MySQLTimeoutException.class, e.getCause(MySQLTimeoutException.class).getClass());
    }

    @Test
    public void timeoutTestTransaction() {
        // when running in a transaction we get an NPE instead of the expected DataAccessException
        assertThrows(DataAccessException.class, () ->
                jooq.transaction(t -> {
                    t.dsl().query("select sleep(10)").queryTimeout(1).execute();
                })
        );
    }

}
