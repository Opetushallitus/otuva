package fi.vm.sade.kayttooikeus.config.spring;

import fi.vm.sade.kayttooikeus.config.spring.dao.DaoConfig;
import fi.vm.sade.kayttooikeus.config.spring.security.SpringSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 12.46
 */
@Configuration
@ComponentScan({
    "fi.vm.sade.kayttooikeus.dao",
    "fi.vm.sade.kayttooikeus.service"
})
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({PropertiesConfig.class, DaoConfig.class, SpringSecurityConfig.class})
public class SpringAppConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource src = new ResourceBundleMessageSource();
        src.setBasename("Messages");
        return src;
    }
    
    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public EhCacheManagerFactoryBean kayttoOikeusCacheManagerFactory() {
        EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setConfigLocation(applicationContext.getResource("classpath:/ehcache.xml"));
        return bean;
    }
    
    @Bean
    public EhCacheCacheManager cacheManager() {
        EhCacheCacheManager manager = new EhCacheCacheManager();
        manager.setCacheManager(kayttoOikeusCacheManagerFactory().getObject());
        return manager;
    }
}
