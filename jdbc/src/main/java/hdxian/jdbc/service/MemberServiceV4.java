package hdxian.jdbc.service;

import hdxian.jdbc.domain.Member;
import hdxian.jdbc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

//import java.sql.SQLException; // 더 이상 SQLException을 의존하지 않음

/**
 * V4 - remove throws SQLException
 */

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV4 {

    private final MemberRepository memberRepository;

    // 이 메서드는 트랜잭션을 적용해서 실행된다.
    // -> AOP 적용 대상이 되어 스프링 컨테이너에 프록시 객체가 생성되고, 프록시 객체에는 트랜잭션을 수행하는 코드가 추가된다.
    // 앞으로 스프링 컨테이너에서 이 MemberService가 주입되는 곳에는 모두 프록시 객체가 대신 주입된다.
    @Transactional
    public void accountTransfer(String fromId, String toId, int amount) {
        bizLogic(fromId, toId, amount);
    }

    private void bizLogic(String fromId, String toId, int amount) {
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
