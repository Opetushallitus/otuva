package fi.vm.sade.kayttooikeus.config.security;

import fi.vm.sade.security.CustomUserDetailsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
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

    @Value("${cas.sendRenew}")
    private boolean sendRenew;

    @Value("${cas_service}")
    private String casService;

    @Value("${web.url.cas}")
    private String webUrlCas;

    @Value("${cas.user-search-base}")
    private String userSearchBase;

    @Value("${cas.user-search-filter}")
    private String userSearchFilter;

    @Value("${cas.group-search-base}")
    private String groupSearchBase;

    @Value("${cas.group-search-filter}")
    private String groupSearchFilter;

    @Value("${cas.group-role-attribute}")
    private String groupRoleAttribute;

    @Value("${ldap.url.with.base}")
    private String ldapUrlWithBase;

    @Value("${ldap.manager-dn}")
    private String ldapManagerDn;

    @Value("${ldap.manager-password}")
    private String ldapManagerPassword;


//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        CustomCasAuthenticationFilter casFilter = new CustomCasAuthenticationFilter();
//
//        http
//                .addFilter(casFilter)
//                .authorizeRequests()
//                .anyRequest().authenticated()
//                .and()
//                .httpBasic();
//    }

    @Bean
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(webUrlCas + "/login");
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Bean
    public ServiceProperties serviceProperties(){
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casService + "/j_spring_cas_security_check");
        serviceProperties.setSendRenew(sendRenew);
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsContextMapper mapper = new CustomUserDetailsMapper();
        DefaultSpringSecurityContextSource source = new DefaultSpringSecurityContextSource(ldapUrlWithBase);
        source.setUserDn(ldapManagerDn);
        source.setPassword(ldapManagerPassword);

        auth.ldapAuthentication()
                .contextSource(source)
                .rolePrefix("Role_")
                .userSearchBase(userSearchBase)
                .userSearchFilter(userSearchFilter)
                .groupSearchBase(groupSearchBase)
                .groupRoleAttribute(groupRoleAttribute)
                .userDetailsContextMapper(mapper);

    }

}
