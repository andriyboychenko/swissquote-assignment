package com.example.swissquote;

import com.example.swissquote.infrastructure.persistence.JpaOperatorUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class SwissquoteApplicationTests {

    @MockBean
    private JpaOperatorUserRepository jpaOperatorUserRepository;

    @Test
    void contextLoads() {
    }
}
