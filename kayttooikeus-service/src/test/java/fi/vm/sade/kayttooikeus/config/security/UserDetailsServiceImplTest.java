package fi.vm.sade.kayttooikeus.config.security;


import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.PalveluRepository;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.it.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(SpringExtension.class)
public class UserDetailsServiceImplTest extends AbstractServiceIntegrationTest {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private KayttajatiedotService kayttajatiedotService;
    @Autowired
    private PalveluRepository palveluRepository;
    @Autowired
    private KayttoOikeusRepository kayttoOikeusRepository;

    @Test
    public void usernameNotFoundException() {
        String kayttajatunnus = "kayttajatunnus123";

        Throwable throwable = catchThrowable(() -> userDetailsService.loadUserByUsername(kayttajatunnus));
        assertThat(throwable).isInstanceOf(UsernameNotFoundException.class);

        kayttajatiedotService.create("oid123", new KayttajatiedotCreateDto(kayttajatunnus));
        UserDetails userDetails = userDetailsService.loadUserByUsername(kayttajatunnus);
        assertThat(userDetails).extracting(UserDetails::getUsername).isEqualTo("oid123");
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

}
