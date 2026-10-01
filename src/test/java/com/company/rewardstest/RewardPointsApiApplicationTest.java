package com.company.rewardstest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.company.rewards.RewardPointsApiApplication;

@SpringBootTest
public class RewardPointsApiApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    public void contextLoads() {
    }

    @Test
    public void shouldStartSpringBootApplication() {
    	RewardPointsApiApplication.main(new String[] {});
        assertNotNull(applicationContext);
        assertNotNull(applicationContext.getBean(RewardPointsApiApplication.class));
    }
}
