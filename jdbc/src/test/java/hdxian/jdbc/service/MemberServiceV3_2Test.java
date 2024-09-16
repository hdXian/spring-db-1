package hdxian.jdbc.service;


import com.zaxxer.hikari.HikariDataSource;
import hdxian.jdbc.domain.Member;
import hdxian.jdbc.repository.MemberRepositoryV3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

import static hdxian.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


// 트랜잭션 - 트랜잭션 매니저
// 트랜잭션 매니저는 트랜잭션 시작, 종료, 커넥션 릴리즈를 추상화하여 제공.
// 또한 트랜잭션 동기화를 수행. (하나의 트랜잭션이 수행될 때 같은 커넥션이 이용되어야 함.
// 이에 트랜잭션 매니저는 트랜잭션 동기화 매니저를 활용하고, 동기화 매니저는 다시 내부적으로 LocalThread를 이용해 커넥션을 관리.)

class MemberServiceV3_2Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_2 memberService;

    @BeforeEach
    public void before() {

        // dataSource 생성
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // TransactionManager 생성
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource); // JDBC 트랜잭션 매니저 구현체

        // DI
        memberRepository = new MemberRepositoryV3(dataSource); // repo depends on DataSource (to get connection from TransactionManager)
        // 트랜잭션 템플릿을 사용하는 ServiceV3_2는 생성자에서 PlatformTransactionManager을 주입받고, 생성자 내부에서 TranasctionTemplate를 생성해 사용함.
        memberService = new MemberServiceV3_2(transactionManager, memberRepository); // Service depends on TransactionManager, repository
    }

    // it's better to use transaction...
    @AfterEach
    public void after() throws SQLException {
        // 없는 멤버 delete해도 DB에서 오류가 발생하지는 않음.
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    public void accountTransfer() throws SQLException {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        // A -> B: transfer 2000
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    public void accountTransferEx() throws SQLException {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when
        // A -> Ex: transfer 2000 (will an Exception occurs.)
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx = memberRepository.findById(memberEx.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000); // rollback -> money of memberA was recovered.
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }


}