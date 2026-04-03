package com.spartangoldengym.analiticas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class RedshiftConfig {

    @Value("${redshift.url}")
    private String url;

    @Value("${redshift.username}")
    private String username;

    @Value("${redshift.password}")
    private String password;

    @Value("${redshift.driver-class-name}")
    private String driverClassName;

    @Bean
    public DataSource redshiftDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate redshiftJdbcTemplate(DataSource redshiftDataSource) {
        return new JdbcTemplate(redshiftDataSource);
    }
}
