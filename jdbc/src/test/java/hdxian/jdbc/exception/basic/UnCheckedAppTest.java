package hdxian.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

@Slf4j
public class UnCheckedAppTest {

    @Test
    void unchecked() {
        TestController controller = new TestController();
        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
        // isInstanceOf(MyRuntimeSQLException.class) also passes this test.
        // SQLException occurs first in Service.logic()
    }

    @Test
    void printEx() {
        TestController controller = new TestController();
        try {
            controller.request();
        } catch (Exception e) {
//            e.printStackTrace();
            log.info("ex", e);
        }

    }

    static class TestController {

        TestService service = new TestService();

        public void request() {
            service.logic();
        }

    }

    static class TestService {

        TestRepository repository = new TestRepository();
        TestNetworkClient networkClient = new TestNetworkClient();

        public void logic() {
            repository.call();
            networkClient.connect();
        }
    }

    static class TestNetworkClient {
        public void connect() {
//            throw new ConnectException("test conn ex");
            throw new MyRuntimeConnectException("test runtime conn ex");
        }

    }

    static class TestRepository {

        // 체크 예외를 리포지토리에서 잡고, 런타임 예외로 바꿔서 던진다.
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                throw new MyRuntimeSQLException(e); // 기존 예외 정보를 만드시 포함해야 한다. (생성자에 기존 예외 e 전달)
            }
        }

        // 일단 체크 예외인 SQL Ex가 발생함.
        public void runSQL() throws SQLException {
            throw new SQLException("test SQL ex");
        }
    }

    // Runtime Exs
    static class MyRuntimeConnectException extends RuntimeException {

        public MyRuntimeConnectException(String message) {
            super(message);
        }

        public MyRuntimeConnectException(Throwable cause) {
            super(cause);
        }
    }

    static class MyRuntimeSQLException extends RuntimeException {

        public MyRuntimeSQLException() {
        }

        public MyRuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }

}
