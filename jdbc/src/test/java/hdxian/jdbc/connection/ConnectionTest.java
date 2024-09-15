package hdxian.jdbc.connection;

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
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource: implements Datasource
        // public DriverManagerDataSource(url, username, password)
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        useDataSource(dataSource);
    }


    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

}
