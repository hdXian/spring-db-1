package hdxian.jdbc.repository;

import hdxian.jdbc.connection.DBConnectionUtil;
import hdxian.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * V0 - use JDBC Driver Manager
 *
 */
@Slf4j
@Repository
public class MemberRepositoryV0 {

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

            // 성공한 경우에만 member 리턴
            return member;

        } catch (SQLException e) {
            log.error("[MemberRepositoryV0] SQL Ex occurs", e);
            throw e;
        } finally {
            close(pstmt, conn, null); // ResultSet not used yet.
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
            log.error("[MemberRepositoryV0.findById] SQL Ex occurs", e);
            throw e;
        } finally {
            close(pstmt, conn, rs);
        }

    }

    private void close(Statement st, Connection con, ResultSet rs) {
        log.error("[MemberRepositoryV0.close] closing connection...");
        // each element must be closed independently

        if(st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                log.error("[MemberRepositoryV0.close(statement)] SQL ex occurs", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("[MemberRepositoryV0.close(conn)] SQL ex occurs", e);
            }
        }

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("[MemberRepositoryV0.close(rs)] SQL ex occurs", e);
            }
        }

    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }

}
