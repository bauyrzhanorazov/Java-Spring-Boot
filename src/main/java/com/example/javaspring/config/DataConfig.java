package com.example.javaspring.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.example.javaspring.entity")
@EnableJpaRepositories(basePackages = "com.example.javaspring.repository")
public class DataConfig {
}