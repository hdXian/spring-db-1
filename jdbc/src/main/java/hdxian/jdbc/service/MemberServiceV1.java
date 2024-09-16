package hdxian.jdbc.service;

import hdxian.jdbc.domain.Member;
import hdxian.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    // 계좌 이체 로직 - fromId의 돈은 감소, toId의 돈은 증가
    public void accountTransfer(String fromId, String toId, int amount) throws SQLException {

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - amount);
        validation(toMember); // toMember의 memberId가 ex면 예외 발생 (테스트용)
        memberRepository.update(toId, toMember.getMoney() + amount);
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex"))
            throw new IllegalStateException("an Error occurs during transfer");
    }

}
