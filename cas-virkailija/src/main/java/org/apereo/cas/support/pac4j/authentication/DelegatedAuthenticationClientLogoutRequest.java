package org.apereo.cas.support.pac4j.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * Overrides DelegatedAuthenticationClientLogoutRequest from CAS since this class is serialized and
 * deserialized to transient tickets during SAML authentication and it requires additional constructors
 * to be compliant with Jackson deserialization.
 */
@SuperBuilder
@RequiredArgsConstructor
@Getter
@Setter
public class DelegatedAuthenticationClientLogoutRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4280830773566610694L;

    private final int status;

    private final String message;

    private final String location;

    private final String target;
}
