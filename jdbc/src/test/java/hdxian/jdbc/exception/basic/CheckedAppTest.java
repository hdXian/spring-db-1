package hdxian.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

@Slf4j
public class CheckedAppTest {

    // 체크 예외를 사용할 때의 문제점
    // 1. 컨트롤러, 서비스 계층에서 해결할 수 없는 예외에 대한 처리가 강제된다. (대부분 그냥 throws로 넘길 수밖에 없다.)
    // 2. 다른 계층에서 발생하는 예외에 대한 의존 관계가 발생한다.
    // (데이터 접근 기술 등이 변경되어 다른 Exception을 던질 경우, 이를 의존하는 서비스와 컨트롤러의 코드를 모두 변경해야 함)

    // 그럼 다 바꾸면 되는거 아님?
    // -> 다 바꾼다는 것부터 의미없고 생산성 떨어지는 작업임. (대부분 애초에 다른 계층에서 신경쓸 예외가 아님)

    // 그냥 Exception으로 통째로 던지면 안됨?
    // -> 그럴꺼면 뭐하러 예외를 체크함? 정작 진짜로 체크해야 할 중요한 예외까지 다 놓치게 됨.

    @Test
    void checked() {
        TestController controller = new TestController();
        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
        // isInstanceOf(SQLException.class) also passes this test.
        // SQLException occurs first in Service.logic()
    }

    static class TestController {

        TestService service = new TestService();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }

    }

    static class TestService {

        TestRepository repository = new TestRepository();
        TestNetworkClient networkClient = new TestNetworkClient();

        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.connect();
        }
    }

    static class TestNetworkClient {
        public void connect() throws ConnectException {
            throw new ConnectException("test conn ex");
        }
    }

    static class TestRepository {
        public void call() throws SQLException {
            throw new SQLException("test SQL ex");
        }
    }

}
