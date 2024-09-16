package hdxian.jdbc.repository;

import hdxian.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * V3 - get Connections from TransactionManager (트랜잭션 시작, 종료(커밋 또는 롤백), 커넥션 릴리즈 수행)
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 */
@Slf4j
public class MemberRepositoryV3 {

    // DI
    private final DataSource dataSource; // 커넥션 가져오기. 근데 트랜잭션 매니저를 통해 가져올 것. (DataSourceUtils)

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
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
            log.error("[MemberRepositoryV3.save] SQL Ex occurs", e);
            throw e;
        } finally {
            close(null, pstmt, conn); // ResultSet not used yet.
        }

    }

    public Member findById(String memberId) throws SQLException {
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
                throw new NoSuchElementException("member not found: memberId=" + memberId);
            }

        } catch (SQLException e) {
            log.error("[MemberRepositoryV3.findById] SQL Ex occurs", e);
            throw e;
        } finally {
            close(rs, pstmt, conn);
        }

    }

    // it's similar with save
    public void update(String memberId, int money) throws SQLException {
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
            log.error("[MemberRepositoryV3.update] SQL Ex occurs", e);
            throw e;
        } finally {
            close(null, pstmt, conn); // ResultSet not used
        }

    }


    public void delete(String memberId) throws SQLException {
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
            log.error("[MemberRepositoryV3.update] SQL Ex occurs", e);
            throw e;
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
