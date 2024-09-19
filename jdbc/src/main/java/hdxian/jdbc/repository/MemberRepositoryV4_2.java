package hdxian.jdbc.repository;

import hdxian.jdbc.domain.Member;
import hdxian.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * V4_2 - SpringExceptionTranslator 사용
 * 여러 DB에서 발생하는 Exception을 스프링에서 추상화하여 제공
 * 스프링에 종속적인 기술이지만, DB나 접근 기술에 따라 코드를 바꿀 필요가 없어진다.
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    // DI
    private final DataSource dataSource; // 커넥션 가져오기. 근데 트랜잭션 매니저를 통해 가져올 것. (DataSourceUtils)
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource); // 구현체를 여기에 넣음.
    }

    @Override
    public Member save(Member member) {
        // 1. generate insert sql
        String sql = "insert into member values (?, ?)";

        // 2. declare Connection, Statement
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 3. get Connection from Driver Manager (use custom DBConnectionUtil)
            conn = getConnection();

            // 4. prepare Statement
            pstmt = conn.prepareStatement(sql);

            // 5. binding parameter in sql
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            // 6. execute sql
            int affectedRows = pstmt.executeUpdate(); // executeUpdate() returns number of affected rows
            log.info("[MemberRepositoryV3.save] query OK, affected rows={}", affectedRows);

            // 성공한 경우에만 member 리턴
            return member;

        } catch (SQLException e) {
            throw exTranslator.translate("save", sql, e);
        } finally {
            close(null, pstmt, conn); // ResultSet not used yet.
        }

    }

    @Override
    public Member findById(String memberId) {
        // 1. generate sql
        String sql = "select * from member where member_id = ?";

        // 2. declare Connection, Statement (for use in finally)
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // 3. execute sql
        try {
            // get connection, prepare statement
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);

            // execute sql, get result
            rs = pstmt.executeQuery();

            if (rs.next()) {  // result always 0 or 1 (select by PK)
                Member member = new Member();
                String id = rs.getString("member_id");
                int money = rs.getInt("money");
                member.setMemberId(id);
                member.setMoney(money);
                return member;
            }
            else {
                // if no such member
                throw new NoSuchElementException("member not found: memberId=" + memberId); // Runtime Exception
            }

        } catch (SQLException e) {
            throw exTranslator.translate("findById", sql, e);
        } finally {
            close(rs, pstmt, conn);
        }

    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int affectedRows = pstmt.executeUpdate(); // executeUpdate() returns affected rows
            log.info("[MemberRepositoryV3.update] query OK, affected rows={}", affectedRows);
        } catch (SQLException e) {
            throw exTranslator.translate("update", sql, e);
        } finally {
            close(null, pstmt, conn); // ResultSet not used
        }

    }


    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, memberId);

            int affectedRows = pstmt.executeUpdate(); // executeUpdate() returns affected rows
            log.info("[MemberRepositoryV3.delete] query OK, affected rows={}", affectedRows);
        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e);
        } finally {
            close(null, pstmt, conn); // ResultSet not used
        }

    }

    private void close(ResultSet rs, Statement st, Connection con) {
        log.info("[MemberRepositoryV3.close] closing connection...");

        // reverse order
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(st);
//        JdbcUtils.closeConnection(con);
        // 트랜잭션 매니저에 의해 관리되는 커넥션인 경우 닫지 않고 넘김.
        // 관리되는 커넥션이 아닌 경우 여기에서 닫음.
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 트랜잭션 동기화(트랜잭션동안 같은 커넥션 사용)를 위해선 DataSourceUtils를 사용해야 함.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get conn={}, class={}", con, con.getClass());
        return con;
    }

}
