package hdxian.jdbc.repository;

import hdxian.jdbc.domain.Member;

import java.sql.SQLException;

/**
 * 구현체에서 예외를 던지려면 인터페이스에도 해당 예외를 던지도록 선언되어 있어야 함.
 * 구현체는 인터페이스에 선언된 예외와 그 하위 예외만을 던질 수 있음.
 * 또한, 인터페이스에서 체크 예외를 던지면 특정 기술에 종속되기 때문에,
 * 코드 변경을 용이하게 하는 인터페이스의 역할이 퇴색됨.
 */
public interface MemberRepositoryEx {

    public Member save(Member member) throws SQLException;

    public Member findById(String memberId) throws SQLException;

    public void update(String memberId, int money) throws SQLException;

    public void delete(String memberId) throws SQLException;


}
