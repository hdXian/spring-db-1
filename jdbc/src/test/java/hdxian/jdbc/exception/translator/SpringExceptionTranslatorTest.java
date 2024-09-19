package hdxian.jdbc.exception.translator;

import com.zaxxer.hikari.HikariDataSource;
import hdxian.jdbc.connection.ConnectionConst;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hdxian.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

@Slf4j
public class SpringExceptionTranslatorTest {

    HikariDataSource dataSource;

    @BeforeEach
    public void before() {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
    }

    @Test
    public void sqlExceptionErrorCode() {
        String sql = "select bad grammar"; // bad grammar sql

        Connection conn = null;
        PreparedStatement psmt = null;

        try {
            conn = dataSource.getConnection();
            psmt = conn.prepareStatement(sql);

            psmt.executeQuery(); // return resultSet
        } catch (SQLException e) {
            // errorCode of h2
            int errorCode = e.getErrorCode();
            assertThat(errorCode).isEqualTo(42122);
            log.info("errorCode={}", errorCode);
            log.info("error", e);
        }

    }

    @Test
    public void exceptionTranslator() {
        String sql = "select bad grammar";

        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement psmt = conn.prepareStatement(sql);
            psmt.executeQuery(); // returns resultSet
        } catch (SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);
            SQLExceptionTranslator translator = new SQLErrorCodeSQLExceptionTranslator(dataSource); // needs dataSource on Constructor
            DataAccessException resultEx = translator.translate("select", sql, e); // translate(task, sql, ex) task: description, sql: executed sql, ex: SQLEx
            log.info("resultEx", resultEx);
            assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
        }


    }

}
