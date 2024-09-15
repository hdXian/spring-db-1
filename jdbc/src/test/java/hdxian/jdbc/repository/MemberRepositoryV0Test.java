package hdxian.jdbc.repository;

import hdxian.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void curd() throws SQLException {

        // save
        Member testMember = new Member("testUser1", 10000);
        repository.save(testMember); // checked SQL Ex

        // findById
        Member findMember = repository.findById(testMember.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(testMember);

        // update money 10000 -> 20000
        repository.update(testMember.getMemberId(), 20000);
        Member updatedMember = repository.findById(testMember.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

    }
}