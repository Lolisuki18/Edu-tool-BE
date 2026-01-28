package com.edutool.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    @Value("${spring.liquibase.drop-first:false}")
    private boolean dropFirst;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setDefaultSchema("public");
        liquibase.setDropFirst(dropFirst);
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
