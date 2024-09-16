package hdxian.jdbc.service;

import hdxian.jdbc.domain.Member;
import hdxian.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * V3_2 - Transaction Template
 */

@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    // 계좌 이체 로직 - fromId의 돈은 감소, toId의 돈은 증가
    public void accountTransfer(String fromId, String toId, int amount) {

        // template callback pattern
        // 1. 내부적으로 트랜잭션 시작 (txTemplate는 TransactionManager를 가지고 있음)
        // 2. 람다로 넣어둔 로직 실행
        // 3. 람다 실행 후 콜백 메서드 실행 -> 콜백 메서드 내용: 실행 결과를 확인하고 성공하면 커밋, 실패하면 롤백
        txTemplate.executeWithoutResult((status) -> {
            try {
                bizLogic(fromId, toId, amount);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
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
