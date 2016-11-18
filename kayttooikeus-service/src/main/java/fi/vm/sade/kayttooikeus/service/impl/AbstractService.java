package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;

public abstract class AbstractService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected String getCurrentUserOid() {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        if (oid == null) {
            throw new NullPointerException("No user name available from SecurityContext!");
        }
        return oid;
    }

    protected Set<String> getCasRoles(){
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        return Sets.newHashSet(Iterables.transform(authorities, (Function<GrantedAuthority, String>) GrantedAuthority::getAuthority));
    }
}
