package hdxian.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hdxian.jdbc.connection.ConnectionConst;
import hdxian.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hdxian.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    // DriverManager creates new Connection each query.
    // DriverManagerDataSource is an "implementation of DataSource" what using DriverManager.
    @Test
    void curdDriverManager() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new MemberRepositoryV1(dataSource); // Inject DriverManagerDataSource as DataSource

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

        // delete
        repository.delete(testMember.getMemberId());
        // NoSucElementEx will be thrown (find deleted member)
        assertThatThrownBy(() -> repository.findById(testMember.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

    }

    // Connection Pooling uses pre-created Connections each query.
    // HikariDataSource is an "implementation of DataSource" what using HikariCP. (HikariCP is one of the open source Connection Pooling library.)
    @Test
    void curdHikariCP() throws SQLException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource); // Inject DriverManagerDataSource as DataSource

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

        // delete
        repository.delete(testMember.getMemberId());
        // NoSucElementEx will be thrown (find deleted member)
        assertThatThrownBy(() -> repository.findById(testMember.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

    }

}