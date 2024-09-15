package hdxian.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hdxian.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    // see the difference between using DriverManager and using Datasource

    /**
     * both uses DriverManager -> create new Connection each getConnection()
     * but DataSource NOT need to connection info in getConnection()
     * DriverManager: getConnection(url, username, password)
     * DataSource: new DataSourceImpl(url, username, password)
     * getConnection()
     */


    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException, InterruptedException {
        // DriverManagerDataSource: implements Datasource
        // public DriverManagerDataSource(url, username, password)
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        useDataSource(dataSource);
    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
//        dataSource.setMaximumPoolSize(1); // after set MaximumPoolSize 1, we can see that an exception(SQLTransientConnectionException) occurs.
        dataSource.setPoolName("My Pool");

        useDataSource(dataSource);
        Thread.sleep(1000); // wait for add Connection on pool thread (for view log)
    }


    private void useDataSource(DataSource dataSource) throws SQLException, InterruptedException {

        Connection con1 = dataSource.getConnection();
//        Thread.sleep(1000);
//        con1.close(); // must have to call close() after use a Connection (just for test)
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

}
