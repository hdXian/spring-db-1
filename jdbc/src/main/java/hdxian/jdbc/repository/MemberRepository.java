package hdxian.jdbc.repository;

import hdxian.jdbc.domain.Member;

import java.sql.SQLException;


public interface MemberRepository {
    public Member save(Member member);

    public Member findById(String memberId);

    public void update(String memberId, int money);

    public void delete(String memberId);
}
