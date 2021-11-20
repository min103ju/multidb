package com.citizen.multidb.config;

import com.citizen.multidb.constants.DbDestination;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = DbConfig.MULTI_DB_ENTITY_MANAGER,
    transactionManagerRef = DbConfig.MULTI_DB_TX_MANAGER,
    basePackages = DbConfig.MULTI_DB_COMPONENT_PACKAGE
)
public class DbConfig {

    public static final String MULTI_DB_ENTITY_MANAGER = "multidbEntityManager";
    public static final String MULTI_DB_TX_MANAGER = "multidbTransactionManager";
    public static final String MULTI_DB_COMPONENT_PACKAGE = "com.citizen.multidb.domain";

    private final String MULTI_DB_MASTER_DATA_SOURCE = "multidbMasterDataSource";
    private final String MULTI_DB_SLAVE_DATA_SOURCE = "multidbSlaveDataSource";
    private final String MULTI_DB_ROUTING_DATA_SOURCE = "multidbRoutingDataSource";
    private final String MULTI_DB_DATA_SOURCE = "multidbDataSource";

    private final String MULTI_DB_MASTER_PROPERTIES_PREFIX = "spring.datasource.master";
    private final String MULTI_DB_SLAVE_PROPERTIES_PREFIX = "spring.datasource.slave";

    private final String MULTI_DB_PERSISTENCE_UNIT = "multidb";

    @ConfigurationProperties(prefix = MULTI_DB_MASTER_PROPERTIES_PREFIX)
    @Bean(name = MULTI_DB_MASTER_DATA_SOURCE)
    public DataSource multidbMasterDataSource() {
        return new HikariDataSource();
    }

    @ConfigurationProperties(prefix = MULTI_DB_SLAVE_PROPERTIES_PREFIX)
    @Bean(name = MULTI_DB_SLAVE_DATA_SOURCE)
    public DataSource multidbSlaveDataSource() {
        return new HikariDataSource();
    }

    @Bean(name = MULTI_DB_ROUTING_DATA_SOURCE)
    public DataSource multidbRoutingDataSource(
        @Qualifier(MULTI_DB_MASTER_DATA_SOURCE) DataSource multiMasterDataSource,
        @Qualifier(MULTI_DB_SLAVE_DATA_SOURCE) DataSource multiSlaveDataSource
    ) {
        ReplicationRoutingDataSource replicationRoutingDataSource = new ReplicationRoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DbDestination.MULTI_DB_MASTER, multiMasterDataSource);
        dataSourceMap.put(DbDestination.MULTI_DB_SLAVE, multiSlaveDataSource);
        replicationRoutingDataSource.setTargetDataSources(dataSourceMap);
        replicationRoutingDataSource.setDefaultTargetDataSource(multiMasterDataSource);

        return replicationRoutingDataSource;

    }

    @Primary
    @DependsOn({MULTI_DB_MASTER_DATA_SOURCE, MULTI_DB_SLAVE_DATA_SOURCE,
        MULTI_DB_ROUTING_DATA_SOURCE})
    @Bean(name = MULTI_DB_DATA_SOURCE)
    public DataSource multidbDataSource(
        @Qualifier(MULTI_DB_ROUTING_DATA_SOURCE) DataSource multidbRoutingDataSource) {
        return new LazyConnectionDataSourceProxy(multidbRoutingDataSource);
    }

    @Primary
    @Bean(name = MULTI_DB_ENTITY_MANAGER)
    public LocalContainerEntityManagerFactoryBean multidbEntityManager(
        @Qualifier(MULTI_DB_DATA_SOURCE) DataSource multidbDataSource,
        EntityManagerFactoryBuilder builder
    ) {
        return builder
            .dataSource(multidbDataSource)
            .packages(MULTI_DB_COMPONENT_PACKAGE)
            .persistenceUnit(MULTI_DB_PERSISTENCE_UNIT)
            .build();
    }

    @Primary
    @Bean(name = MULTI_DB_TX_MANAGER)
    public PlatformTransactionManager multidbTransactionManager(
        @Qualifier(MULTI_DB_ENTITY_MANAGER) EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
