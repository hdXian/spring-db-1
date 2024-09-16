package hdxian.jdbc.service;

import hdxian.jdbc.domain.Member;
import hdxian.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

/**
 * V3_1 - use TransactionManager
 */

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    // 계좌 이체 로직 - fromId의 돈은 감소, toId의 돈은 증가
    public void accountTransfer(String fromId, String toId, int amount) {

        // 트랜잭션 시작 (get or create new transaction)
        // 트랜잭션 상태를 리턴함. (트랜잭션의 속성을 설정 가능)
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            bizLogic(fromId, toId, amount);
            transactionManager.commit(status); // 성공하면 커밋 (커밋 혹은 롤백할 때 트랜잭션 상태를 넘겨야 함)
        } catch (Exception e) {
            transactionManager.rollback(status); // 실패하면 롤백
            log.error("[MemberServiceV3_1.accountTransfer] failed to exec transaction", e);
            throw new IllegalStateException(e);
        }
        // 트랜잭션 종료 시 커넥션 자동 릴리즈 (finally문 필요 x, release() 필요 x.)
    }

    private void bizLogic(String fromId, String toId, int amount) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - amount);
        validation(toMember); // toMember의 memberId가 ex면 예외 발생 (테스트용)
        memberRepository.update(toId, toMember.getMoney() + amount);
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex"))
            throw new IllegalStateException("an Error occurs in validation()");
    }

}
