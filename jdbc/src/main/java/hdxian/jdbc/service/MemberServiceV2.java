package hdxian.jdbc.service;

import hdxian.jdbc.domain.Member;
import hdxian.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * V2 - apply Transaction by getting Connection at Service layer
 *
 */

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    // 계좌 이체 로직 - fromId의 돈은 감소, toId의 돈은 증가
    public void accountTransfer(String fromId, String toId, int amount) throws SQLException {
        Connection con = dataSource.getConnection();

        try {
            con.setAutoCommit(false); // disable auto commit -> start of service transaction
            bizLogic(con, fromId, toId, amount);
            con.commit(); // commit if success to exec
        } catch (Exception e) {
            con.rollback();
            log.error("[MemberServiceV2.accountTransfer] failed to exec transaction", e);
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }

    }

    private void bizLogic(Connection conn, String fromId, String toId, int amount) throws SQLException {
        Member fromMember = memberRepository.findById(conn, fromId);
        Member toMember = memberRepository.findById(conn, toId);

        memberRepository.update(conn, fromId, fromMember.getMoney() - amount);
        validation(toMember); // toMember의 memberId가 ex면 예외 발생 (테스트용)
        memberRepository.update(conn, toId, toMember.getMoney() + amount);
    }

    private static void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (Exception e) {
                log.error("[MemberServiceV2.release] an error occurs while closing Connection", e);
            }
        }
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex"))
            throw new IllegalStateException("an Error occurs in validation()");
    }

}
