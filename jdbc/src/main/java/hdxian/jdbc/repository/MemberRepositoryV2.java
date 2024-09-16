package hdxian.jdbc.repository;

import hdxian.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * V2 - use Connection on method parameter (find, update)
 *
 */
@Slf4j
public class MemberRepositoryV2 {

    // DI
    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
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
            log.info("[MemberRepositoryV2.save] query OK, affected rows={}", affectedRows);

            // 성공한 경우에만 member 리턴
            return member;

        } catch (SQLException e) {
            log.error("[MemberRepositoryV2.save] SQL Ex occurs", e);
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
            log.error("[MemberRepositoryV2.findById] SQL Ex occurs", e);
            throw e;
        } finally {
            close(rs, pstmt, conn);
        }

    }

    // use Connection param
    public Member findById(Connection con, String memberId) throws SQLException {
        // 1. generate sql
        String sql = "select * from member where member_id = ?";

        // 2. declare Statement (for use in finally)
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // 3. execute sql
        try {
//            conn = getConnection(); use parameter Connection, never get it by getConnection()
            pstmt = con.prepareStatement(sql);
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
            log.error("[MemberRepositoryV2.findById] SQL Ex occurs", e);
            throw e;
        } finally {
            // not close Connection at here. (will close it on Service layer)
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
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
            log.info("[MemberRepositoryV2.update] query OK, affected rows={}", affectedRows);
        } catch (SQLException e) {
            log.error("[MemberRepositoryV2.update] SQL Ex occurs", e);
            throw e;
        } finally {
            close(null, pstmt, conn); // ResultSet not used
        }

    }

    // use Connection param
    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";

        PreparedStatement pstmt = null;

        try {
//            conn = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int affectedRows = pstmt.executeUpdate(); // executeUpdate() returns affected rows
            log.info("[MemberRepositoryV2.update] query OK, affected rows={}", affectedRows);
        } catch (SQLException e) {
            log.error("[MemberRepositoryV2.update] SQL Ex occurs", e);
            throw e;
        } finally {
            // not close Connection at here
            JdbcUtils.closeStatement(pstmt);
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
            log.info("[MemberRepositoryV2.delete] query OK, affected rows={}", affectedRows);
        } catch (SQLException e) {
            log.error("[MemberRepositoryV2.update] SQL Ex occurs", e);
            throw e;
        } finally {
            close(null, pstmt, conn); // ResultSet not used
        }

    }

    private void close(ResultSet rs, Statement st, Connection con) {
        log.info("[MemberRepositoryV2.close] closing connection...");
        // each element must be closed independently.
        // reverse order of when the connection was created.
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(st);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        // use DataSource
        Connection con = dataSource.getConnection();
        log.info("get conn={}, class={}", con, con.getClass());
        return con;
    }

}
