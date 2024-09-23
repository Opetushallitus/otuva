package fi.vm.sade.kayttooikeus.config.security;

import org.apereo.cas.client.session.SessionMappingStorage;

public interface OphSessionMappingStorage extends SessionMappingStorage {

    void clean();

}
