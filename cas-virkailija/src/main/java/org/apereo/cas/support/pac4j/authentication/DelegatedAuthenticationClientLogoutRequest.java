package org.apereo.cas.support.pac4j.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Overrides DelegatedAuthenticationClientLogoutRequest from CAS since this class is serialized and
 * deserialized to transient tickets during SAML authentication and it requires additional constructors
 * to be compliant with Jackson deserialization.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DelegatedAuthenticationClientLogoutRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4280830773566610694L;

    private int status;

    private String message;

    private String location;

    private String target;
}
