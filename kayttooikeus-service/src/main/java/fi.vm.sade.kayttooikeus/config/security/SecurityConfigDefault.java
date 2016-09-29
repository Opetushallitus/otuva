package fi.vm.sade.kayttooikeus.config.security;


import fi.vm.sade.security.CustomUserDetailsMapper;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.EhCacheBasedTicketCache;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 * Created by autio on 19.9.2016.
 */
@EnableWebSecurity
@Profile("default")
@EnableGlobalMethodSecurity(prePostEnabled = true,
        proxyTargetClass = true, jsr250Enabled = true)
public class SecurityConfigDefault extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigDefault.class);

    @Autowired
    private Environment environment;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilter(casAuthenticationFilter())
                .authenticationProvider(casAuthenticationProvider())
                .exceptionHandling()
                .authenticationEntryPoint(casAuthenticationEntryPoint())
                .and()
                .csrf().disable()
                .authorizeRequests()
                .anyRequest()
                .authenticated();

    }

    @Bean
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(environment.getProperty("cas.web.url") + "/login");
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Bean
    public ServiceProperties serviceProperties(){
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(environment.getProperty("cas.service") + "/j_spring_cas_security_check");
        serviceProperties.setSendRenew(environment.getProperty("cas.sendRenew", Boolean.class));
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }


//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        logger.info("configure global AuthenticationManagerBuilder");
//        UserDetailsContextMapper mapper = new CustomUserDetailsMapper();
//
//        DefaultSpringSecurityContextSource source = new DefaultSpringSecurityContextSource(environment.getProperty("ldap.url.with.base"));
//        source.afterPropertiesSet();
//        source.setUserDn(environment.getProperty("ldap.manager-dn"));
//        source.setPassword(environment.getProperty("ldap.manager-password"));
//
//        auth.ldapAuthentication()
//                .contextSource(source)
//                .rolePrefix("ROLE_")
//                .userSearchBase(environment.getProperty("cas.user-search-base"))
//                .userSearchFilter(environment.getProperty("cas.user-search-filter"))
//                .groupSearchBase(environment.getProperty("cas.group.search-base"))
//                .groupRoleAttribute(environment.getProperty("cas.group.role-attribute"))
//                .userDetailsContextMapper(mapper);
//
//    }


    private EhCacheBasedTicketCache ehCacheBasedTicketCache(){
        EhCacheBasedTicketCache eh = new EhCacheBasedTicketCache();
        eh.setCache(casTicketsCacheBean().getObject());
        return eh;
    }

    @Bean
    public EhCacheFactoryBean casTicketsCacheBean() {
        EhCacheFactoryBean ehCache = new EhCacheFactoryBean();

        ehCache.setCacheManager(casTicketCache().getObject());
        ehCache.setDiskExpiryThreadIntervalSeconds(120);
        ehCache.setDiskPersistent(false);
        ehCache.setMaxElementsOnDisk(10000000);
        ehCache.setMemoryStoreEvictionPolicy("LRU");
        ehCache.setOverflowToDisk(true);
        ehCache.setName("casTickets");
        ehCache.setTimeToLive(7200);
        ehCache.setTimeToIdle(7200);
        ehCache.setEternal(false);

        return ehCache;
    }

    @Bean
    public EhCacheManagerFactoryBean casTicketCache() {
        EhCacheManagerFactoryBean casTicketCache = new EhCacheManagerFactoryBean();

        casTicketCache.setShared(false);
        casTicketCache.setCacheManagerName("casTicketCache");

        return casTicketCache;
    }


    @Bean
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(authenticationUserDetailsService());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
//        casAuthenticationProvider.setTicketValidator(casServiceTicketValidator());
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
        casAuthenticationProvider.setKey(environment.getProperty("cas.key"));
        casAuthenticationProvider.setStatelessTicketCache(ehCacheBasedTicketCache());
        return casAuthenticationProvider;
    }

    @Bean
    public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService() {
        return (CasAssertionAuthenticationToken casAssertionAuthenticationToken)
                -> {
            logger.info("token " + casAssertionAuthenticationToken);
            if (casAssertionAuthenticationToken != null) {
                logger.info("name " + casAssertionAuthenticationToken.getName());
            }
            LdapUserDetailsService service = ldapUserDetailsService();

            logger.info("ldapservice " + ldapUserDetailsService());
            logger.info("name " + casAssertionAuthenticationToken.getName());
            logger.info("cred " + casAssertionAuthenticationToken.getCredentials().toString());
            UserDetails details = service.loadUserByUsername(casAssertionAuthenticationToken.getName());
            logger.info("details: " + details);

            return details;
//            return ldapUserDetailsService().loadUserByUsername(casAssertionAuthenticationToken.getName());
        };
    }

    @Bean
    public LdapUserDetailsService ldapUserDetailsService() {
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(environment.getProperty("cas.user-search-base"),
                environment.getProperty("cas.user-search-filter"), ldapContextSource());
        LdapUserDetailsService ldapUserDetailsService = new LdapUserDetailsService(userSearch);
        ldapUserDetailsService.setUserDetailsMapper(userDetailsContextMapper());
        return ldapUserDetailsService;
    }

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(environment.getProperty("ldap.url.with.base"));
        ldapContextSource.setUserDn(environment.getProperty("ldap.manager-dn"));
        ldapContextSource.setPassword(environment.getProperty("ldap.manager-password"));
        return ldapContextSource;
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper() {
        LdapUserDetailsMapper ldapUserDetailsMapper = new LdapUserDetailsMapper();
        ldapUserDetailsMapper.setRolePrefix("ROLE_");
        ldapUserDetailsMapper.setConvertToUpperCase(true);
        return  ldapUserDetailsMapper;
    }


//
//    private Cas20ProxyTicketValidator casServiceTicketValidator() {
//        Cas20ProxyTicketValidator validator = new Cas20ProxyTicketValidator(webUrlCas);
//        validator.setProxyCallbackUrl(casService+"j_spring_cas_security_proxyreceptor");
////        validator.setProxyGrantingTicketStorage(ticketStorage());
//        validator.setAcceptAnyProxy(true);
//        return validator;
//    }

    @Bean
    public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
//        Cas20ServiceTicketValidator validator = new Cas20ProxyTicketValidator(webUrlCas);
//        validator.setProxyCallbackUrl(casService+"j_spring_cas_security_proxyreceptor");
//        return validator;
        return new Cas20ServiceTicketValidator(environment.getProperty("cas.web.url"));
    }


    @Bean
    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setAuthenticationManager(authenticationManager());
        return casAuthenticationFilter;
    }


//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.addFilter(casAuthenticationFilter())
//                .exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint())
//                .and()
//                .csrf().disable()
//                .authorizeRequests()
//                .anyRequest().authenticated();
//    }
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        logger.info("configure auth");
//        auth
//                .authenticationProvider(casAuthenticationProvider());
//    }

}
