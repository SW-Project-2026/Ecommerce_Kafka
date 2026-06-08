package com.logflow.log_pipeline.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.logflow.log_pipeline.pipeline"
    },
    entityManagerFactoryRef = "kafkaEntityManagerFactory",
    transactionManagerRef   = "kafkaTransactionManager"
)
class KafkaJpaConfig {}

@Configuration
@EnableJpaRepositories(
    basePackages            = "com.logflow.log_pipeline.be",
    entityManagerFactoryRef = "beEntityManagerFactory",
    transactionManagerRef   = "beTransactionManager"
)
class BeJpaConfig {}

@Configuration
public class DataSourceConfig {

    // ── Kafka DB (Primary) ──
    @Primary
    @Bean(name = "kafkaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.kafka")
    public DataSource kafkaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "kafkaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean kafkaEntityManagerFactory(
            @Qualifier("kafkaDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(
            "com.logflow.log_pipeline.pipeline"
        );
        em.setPersistenceUnitName("kafka");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(adapter);

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "validate");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        em.setJpaPropertyMap(props);

        return em;
    }

    @Primary
    @Bean(name = "kafkaTransactionManager")
    public PlatformTransactionManager kafkaTransactionManager(
            @Qualifier("kafkaEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(factory.getObject());
    }

    // ── BE DB (Secondary) ──
    @Bean(name = "beDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.be")
    public DataSource beDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "beEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean beEntityManagerFactory(
            @Qualifier("beDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.logflow.log_pipeline.be");
        em.setPersistenceUnitName("be");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(adapter);

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        em.setJpaPropertyMap(props);

        return em;
    }

    @Bean(name = "beTransactionManager")
    public PlatformTransactionManager beTransactionManager(
            @Qualifier("beEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(factory.getObject());
    }
}