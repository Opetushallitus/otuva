package fi.vm.sade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceDelegatedAuthenticationPolicy;

import java.io.Serial;
import java.util.Collection;


@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class HakaAwareRegisteredServiceDelegationAuthenticationPolicy implements RegisteredServiceDelegatedAuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = -784106970642770921L;

    private Collection<String> allowedProviders;

    private boolean permitUndefined = true;

    private boolean exclusive;

    private String selectionStrategy;

    @Override
    @JsonIgnore
    public boolean isProviderAllowed(String provider, RegisteredService registeredService) {
        if (provider.startsWith("haka-") && getAllowedProviders().contains("haka")) {
            return true;
        } else {
            return getAllowedProviders().contains(provider);
        }
    }
}


