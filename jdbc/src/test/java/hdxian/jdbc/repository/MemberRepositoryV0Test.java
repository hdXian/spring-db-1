package hdxian.jdbc.repository;

import hdxian.jdbc.domain.Member;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void curd() throws SQLException {

        Member testUser1 = new Member("testUser1", 10000);
        repository.save(testUser1); // checked SQL Ex

    }
}