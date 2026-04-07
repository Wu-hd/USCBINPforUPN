package com.uscbinp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = "com.uscbinp",
    exclude = DataSourceAutoConfiguration.class
)
public class UscbinpApplication {

    public static void main(String[] args) {
        SpringApplication.run(UscbinpApplication.class, args);
    }
}
