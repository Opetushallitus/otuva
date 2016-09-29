package fi.vm.sade.kayttooikeus.config.dao;

import com.googlecode.flyway.core.Flyway;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.reflect.InvocationTargetException;

/**
 * User: tommiratamaa
 * Date: 26.8.2016
 * Time: 15.49
 */
@Configuration
@EnableJpaRepositories(basePackages = "fi.vm.sade.kayttooikeus.repository")
@EnableTransactionManagement
@Import({DaoConfigTest.class, DaoConfigDefault.class})
public class DaoConfig {
    private static final Logger logger = LoggerFactory.getLogger(DaoConfig.class);

    @Autowired
    private Environment environment;

    @Autowired
    private DaoConfigurations daoConfigurations;

    private Object inMemoryDatabaseServer;

    @Bean
    public InstrumentationLoadTimeWeaver loadTimeWeaver() {
        return new InstrumentationLoadTimeWeaver();
    }

    @Bean(name = "emf")
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean emf() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setLoadTimeWeaver(loadTimeWeaver());
        emf.setJpaProperties(daoConfigurations.getJpaProperties());
        emf.setDataSource(dbDataSource());
        return emf;
    }
    
    @Bean(name = "dbDataSource", destroyMethod = "close")
    public HikariDataSource dbDataSource() {
        return new HikariDataSource(hikariConfig());
    }

    @Bean
    public HikariConfig hikariConfig() {
        logger.info("hikariConfig, useInMemoryDb={}", daoConfigurations.isUseInMemoryDb());
        HikariConfig config = new HikariConfig();
        config.setPoolName("springHikariCP");
        if (!daoConfigurations.isUseInMemoryDb()) {
            config.setConnectionTestQuery("SELECT 1"); // for some reason causes unexpected end of statement with hsqldb
        } else {
            String db = daoConfigurations.getMemoryDataSourceProperties().getProperty("database").replace("jdbc:hsqldb:","");
            if (db.startsWith("file:")) {
                startupInMemoryDatabase(db);
            }
        }
        config.setDataSourceClassName(daoConfigurations.isUseInMemoryDb() ? "org.hsqldb.jdbc.JDBCDataSource" : "org.postgresql.ds.PGSimpleDataSource");
        config.setDataSourceProperties(daoConfigurations.isUseInMemoryDb() ? daoConfigurations.getMemoryDataSourceProperties() : daoConfigurations.getMainDataSourceProperties());
        config.setMaximumPoolSize(environment.getProperty("datasource.max-active", Integer.class, 15));
        config.setConnectionTimeout(environment.getProperty("datasource.max-wait", Integer.class, 10000));
        config.setMaxLifetime(environment.getProperty("datasource.max-lifetime-millis", Integer.class));
        return config;
    }

    private void startupInMemoryDatabase(String path) {
        try {
            Class<?> serverClz = Class.forName("org.hsqldb.Server");
            Object server = serverClz.newInstance();
            serverClz.getMethod("setDatabasePath", Integer.TYPE, String.class).invoke(server, 0, path);
            serverClz.getMethod("setDatabaseName", Integer.TYPE, String.class).invoke(server, 0, "auth");
            logger.info("Starting org.hsqldb.Server...");
            serverClz.getMethod("start").invoke(server);
            logger.info("Started org.hsqldb.Server");
            inMemoryDatabaseServer = server;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Dependency org.hsqldb:hsqldb missing for using an in-memory-database.", e);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to start org.hsqldb.Server: " + e.getMessage(), e);
        }
    }

    @Bean
    public ApplicationListener<ContextClosedEvent> shutdownInMemoryServerOnClose() {
        return new ApplicationListener<ContextClosedEvent>() { // don't replace with lambda
            @Override
            public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
                if (inMemoryDatabaseServer != null) {
                    try {
                        Class.forName("org.hsqldb.Server").getMethod("shutdown").invoke(inMemoryDatabaseServer);
                    } catch (Exception e) {
                        logger.error("Failed to close in memory hsqldb database.");
                    }
                }
            }
        };
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        if (!daoConfigurations.isUseFlyway()) {
            return null;
        }
        Flyway flyway = new Flyway();
        flyway.setInitOnMigrate(environment.getProperty("flyway.init-on-migrate", Boolean.class));
        flyway.setDataSource(new LazyConnectionDataSourceProxy(dbDataSource()));
        return flyway;
    }
    
    @Bean
    public JpaTransactionManager transactionManager() {
        return new JpaTransactionManager(emf().getObject());
    }
}
