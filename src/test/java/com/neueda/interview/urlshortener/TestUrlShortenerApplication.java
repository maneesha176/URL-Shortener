package com.neueda.interview.urlshortener;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.urlshortener")
@EntityScan("com.urlshortener.model")
@EnableJpaRepositories("com.urlshortener.repository")
public class TestUrlShortenerApplication {
}
