package org.web3j.spring.autoconfigure.context;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.web3j.quorum.Quorum;

import static org.mockito.Mockito.mock;

@SpringBootApplication
public class SpringApplicationTest {
    @Bean
    @Primary
    public Quorum nameService() {
        return mock(Quorum.class, Mockito.RETURNS_DEEP_STUBS);
    }

}
