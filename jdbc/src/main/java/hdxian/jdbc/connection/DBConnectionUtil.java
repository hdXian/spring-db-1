package hdxian.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hdxian.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() {

        try {
            // DriverManager gets JDBC Driver from libs automatically.
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("[DBConnectionUtil] success to get connection: connection=[{}], class=[{}]", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            // throw converted Exception (SQLEx -> RuntimeEx)
            throw new IllegalStateException(e);
        }

    }

}
