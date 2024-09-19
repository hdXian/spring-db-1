package hdxian.jdbc.exception.translator;

import com.zaxxer.hikari.HikariDataSource;
import hdxian.jdbc.connection.ConnectionConst;
import hdxian.jdbc.domain.Member;
import hdxian.jdbc.repository.ex.MyDbException;
import hdxian.jdbc.repository.ex.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static hdxian.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ExTranslatorV1Test {

    TestRepository repository;
    TestService service;

    @BeforeEach
    public void before() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new TestRepository(dataSource);
        service = new TestService(repository);
    }

    @Test
    public void duplicateKey() {
        service.create("test1");
        service.create("test1");
    }


    @Slf4j
    @RequiredArgsConstructor
    static class TestService {
        private final TestRepository repository;

        public void create(String memberId) {

            // 런타임 예외인 MyDuplicateException에 대한 서비스 계층에서의 복구 시도
            try {
                repository.save(new Member(memberId, 0));
                log.info("saved memberId={}", memberId);

            } catch (MyDuplicateKeyException e) { // 키 중복 예외에 대한 복구
                log.info("key duplicated. attempt to recovering");
                String retryId = generateNewId(memberId);
                log.info("retryId={}", retryId);
                repository.save(new Member(retryId, 0));

            } catch (MyDbException e) { // 그냥 확인용
                log.info("data access layer Ex");
                throw e;
            }

        }

        private static String generateNewId(String memberId) {
             return memberId + new Random().nextInt(10000);
        }

    }

    @Slf4j
    @RequiredArgsConstructor
    static class TestRepository {
        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member (member_id, money) values (?, ?)";

            Connection conn = null;
            PreparedStatement psmt = null;

            try {
                conn = dataSource.getConnection();

                psmt = conn.prepareStatement(sql);
                psmt.setString(1, member.getMemberId());
                psmt.setInt(2, member.getMoney());

                psmt.executeUpdate();
                return member;
            } catch (SQLException e) {
                if (e.getErrorCode() == 23505) {
                    throw new MyDuplicateKeyException(e);
                }
                else
                    throw new MyDbException(e);
            } finally {
                JdbcUtils.closeStatement(psmt);
                JdbcUtils.closeConnection(conn);
            }

        }

    }


}
