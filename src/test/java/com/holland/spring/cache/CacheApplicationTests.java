package com.holland.spring.cache;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootApplication
@SpringBootTest
class CacheApplicationTests {
    @Resource
    private TestService testService;

    @Test
    void contextLoads() throws InterruptedException {
        System.out.printf("第一次调用，使用的应该是 计算：%s\n\n", testService.test());

        System.out.printf("3秒内，使用的应该是 caffeine：%s\n\n", testService.test());
        Thread.sleep(3000);
        System.out.printf("3秒后，使用的应该是 redis：%s\n\n", testService.test());
        Thread.sleep(3000);
        System.out.printf("6秒后，使用的应该是 计算：%s\n\n", testService.test());
    }

}
