package com.st;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJdbcRepositories
@ComponentScan
@PropertySource("classpath:application.properties")
public class ConfigurationApplicationContext extends JdbcConfiguration {

    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.user}")
    private String user;
    @Value("${jdbc.password}")
    private String password;
    @Value("${jdbc.timeoutMs:5000}")
    private int timeout;
    @Value("${application.timeGenerator.connection.reconnectTimeoutInMs:5000}")
    private int reconnectDelayMs;
    @Value("${application.timeGenerator.newValue.delayInMs:1000}")
    private int delayToGenerateNewTimestampMs;
    @Value("${application.saveBatchSize:500}")
    private int saveBatchSize;

    @Bean(value = "dataSource")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setInitializationFailTimeout(reconnectDelayMs);
        config.setConnectionTimeout(timeout);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        Properties dsProperties = new Properties();
        dsProperties.setProperty("useSSL", "false");
        dsProperties.setProperty("allowPublicKeyRetrieval", "true");
        config.setDataSourceProperties(dsProperties);
        return new HikariDataSource(config);
    }

    @Bean
    NamedParameterJdbcOperations operations() {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean
    public GeneratorService generatorService() {
        return new GeneratorService(reconnectDelayMs, saveBatchSize, delayToGenerateNewTimestampMs);
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

}
