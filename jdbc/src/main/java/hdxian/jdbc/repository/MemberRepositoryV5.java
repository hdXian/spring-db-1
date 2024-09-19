package hdxian.jdbc.repository;

import hdxian.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * V5 - JdbcTemplate 적용
 * 반복되는 아래 작업들을 자동 수행
 * - 커넥션 조회 및 동기화
 * - psmt 초기화 및 파라미터 바인딩
 * - 쿼리 실행 및 결과 바인딩
 * - 예외 발생 시 스프링 예외 변환기 적용
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        // DataSource를 주입받아 template 필드 초기화
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member values (?, ?)";
        int affectedRows = template.update(sql, member.getMemberId(), member.getMoney());
        log.info("[MemberRepositoryV5.save] Query Ok, affected rows={}", affectedRows);
        return member;
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }



    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";
        int affectedRows = template.update(sql, money, memberId);
        log.info("[MemberRepositoryV5.update] Query Ok, affected rows={}", affectedRows);
    }


    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id = ?";
        int affectedRows = template.update(sql, memberId);
        log.info("[MemberRepositoryV5.delete] Query Ok, affected rows={}", affectedRows);
    }

    // 쿼리 결과를 Member에 매핑해 리턴하는 함수 template.queryForObject()에 사용
    private RowMapper<Member> memberRowMapper() {
        return ((rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        });
    }

}
