package hdxian.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnCheckedExTest {

    @Test
    void call_catch() {
        TestService service = new TestService();
        service.callCatch();
    }

    @Test
    void call_throw() {
        TestService service = new TestService();
        Assertions.assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }


    // subclasses of RuntimeEx is unchecked exception.
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    static class TestRepository {
        // 언체크 예외는 메서드 선언부에 throws를 생략할 수 있다.
        void call() {
            throw new MyUncheckedException("test unchecked ex");
        }
    }

    static class TestService {
        TestRepository repository = new TestRepository();

        // unchecked exception도 필요하다면 catch로 잡으면 된다.
        void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                log.info("catch exception={}", e.getMessage(), e);
            }
        }

        // repo.call()은 예외를 던지고 있지만 여기서 처리하지 않음. 즉 이 메서드를 호출한 곳까지 예외가 전달됨.
        // 하지만 메서드 선언부에 throws를 생략 가능.
        void callThrow() {
            repository.call();
        }

    }

}
