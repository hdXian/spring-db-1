package hdxian.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedExTest {

    @Test
    void checked_catch() {
        TestService service = new TestService();
        // callCatch() 메서드 내에서 try-catch로 잡았기 때문에 여기까지 예외가 전달되지 않음.
        service.callCatch();
    }

    @Test
    void checked_throw() {
        TestService service = new TestService();
        // callThrow() 메서드는 예외를 처리하지 않고 호출한 곳으로 던짐. 따라서 여기(callThrow()를 호출한 이 테스트 메서드)까지 예외가 전달됨.
        Assertions.assertThatThrownBy(() -> service.callThrow())
                        .isInstanceOf(MyCheckedException.class);
    }


    // all subclass of Exception is checked Exception.
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }
    }

    static class TestService {
        TestRepository repository = new TestRepository();

        // 예외를 잡아서 처리하는 코드. 이후는 정상 흐름으로 전환됨.
        void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e) {
                log.info("catch exception={}", e.getMessage(), e);
            }
        }

        // 예외를 호출한 곳으로 던지는 코드. 이 메서드를 호출한 곳까지 예외가 전달됨. throws를 메서드 선언부에 반드시 선언해야 함.
        void callThrow() throws MyCheckedException {
            repository.call();
        }

    }

    static class TestRepository {

        // 예외가 발생하는 메서드. 메서드 내에서 잡지 않고 호출한 곳으로 던졌다.
        void call() throws MyCheckedException {
            throw new MyCheckedException("tset ex");
        }
    }

}
