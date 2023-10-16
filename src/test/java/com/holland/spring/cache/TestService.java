package com.holland.spring.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @Cacheable("status")
    public String test() {
        System.err.println("From calculation");
        return "OK";
    }
}
