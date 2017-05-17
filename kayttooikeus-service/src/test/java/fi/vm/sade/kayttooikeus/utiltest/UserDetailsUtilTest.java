package fi.vm.sade.kayttooikeus.utiltest;


import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.dto.YhteystietojenTyypit;
import fi.vm.sade.kayttooikeus.util.CreateUtil;
import fi.vm.sade.kayttooikeus.util.UserDetailsUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserDetailsUtilTest {
    @Before
    public void setup() {

    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getName() {
        HenkiloDto henkiloDto = new HenkiloDto();
        henkiloDto.setEtunimet("arpa noppa");
        henkiloDto.setKutsumanimi("arpa");
        henkiloDto.setSukunimi("kuutio");
        String name = UserDetailsUtil.getName(henkiloDto);
        assertThat(name).isEqualTo("arpa kuutio");
    }

    @Test
    public void getCurrentUserOidNotFound() {
        this.expectedException.expect(NullPointerException.class);
        this.expectedException.expectMessage("No user name available from SecurityContext!");

         Authentication authentication = mock(Authentication.class);
         SecurityContext securityContext = mock(SecurityContext.class);
         when(securityContext.getAuthentication()).thenReturn(authentication);
         SecurityContextHolder.setContext(securityContext);

         UserDetailsUtil.getCurrentUserOid();
     }

    @Test
    public void getCurrentUserOid() {
         Authentication authentication = mock(Authentication.class);
         SecurityContext securityContext = mock(SecurityContext.class);
         when(securityContext.getAuthentication()).thenReturn(authentication);
         when(authentication.getName()).thenReturn("1.2.3.4.5");
         SecurityContextHolder.setContext(securityContext);

         String username = UserDetailsUtil.getCurrentUserOid();
         assertThat(username).isEqualTo("1.2.3.4.5");
     }

    @Test
    public void getLanguageCode() {
        HenkiloDto henkiloDto = new HenkiloDto();
        henkiloDto.setAsiointiKieli(new KielisyysDto("sv", "svenska"));

        String kielikoodi = UserDetailsUtil.getLanguageCode(henkiloDto);
        assertThat(kielikoodi).isEqualTo("sv");
    }

    @Test
    public void getLanguageCodeDefault() {
        HenkiloDto henkiloDto = new HenkiloDto();
        henkiloDto.setAsiointiKieli(new KielisyysDto());

        String kielikoodi = UserDetailsUtil.getLanguageCode(henkiloDto);
        assertThat(kielikoodi).isEqualTo("fi");
    }

    @Test
    public void getLanguageCodePerustieto() {
        HenkiloPerustietoDto henkiloDto = new HenkiloPerustietoDto();
        henkiloDto.setAsiointiKieli(new KielisyysDto("sv", "svenska"));

        String kielikoodi = UserDetailsUtil.getLanguageCode(henkiloDto);
        assertThat(kielikoodi).isEqualTo("sv");
    }

    @Test
    public void getLanguageCodePerustietoDefault() {
        HenkiloPerustietoDto henkiloDto = new HenkiloPerustietoDto();
        henkiloDto.setAsiointiKieli(new KielisyysDto());

        String kielikoodi = UserDetailsUtil.getLanguageCode(henkiloDto);
        assertThat(kielikoodi).isEqualTo("fi");
    }

    @Test
    public void getEmailByPrioritySingleValue() {
        HenkiloDto henkiloDto = new HenkiloDto();
        henkiloDto.setYhteystiedotRyhma(Sets.newHashSet(YhteystiedotRyhmaDto.builder()
                .yhteystieto(YhteystietoDto.builder()
                        .yhteystietoArvo("arpa@kuutio.fi")
                        .yhteystietoTyyppi(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI).build())
                .build()));
        Optional<String> emailByPriority =  UserDetailsUtil.getEmailByPriority(henkiloDto);
        assertThat(emailByPriority).contains("arpa@kuutio.fi");
    }

    @Test
    public void getEmailByPriorityNoneFound() {
        HenkiloDto henkiloDto = new HenkiloDto();
        Optional<String> emailByPriority =  UserDetailsUtil.getEmailByPriority(henkiloDto);
        assertThat(emailByPriority).isEmpty();
    }

    @Test
    public void getEmailByPriorityMultipleValue() {
        HenkiloDto henkiloDto = new HenkiloDto();
        henkiloDto.setYhteystiedotRyhma(Sets.newHashSet(CreateUtil.createYhteystietoSahkoposti("arpa@kuutio.fi", YhteystietojenTyypit.MUU_OSOITE),
                CreateUtil.createYhteystietoSahkoposti("arpa2@kuutio.fi", YhteystietojenTyypit.TYOOSOITE),
                CreateUtil.createYhteystietoSahkoposti("arpa3@kuutio.fi", YhteystietojenTyypit.VAPAA_AJAN_OSOITE)));
        Optional<String> emailByPriority =  UserDetailsUtil.getEmailByPriority(henkiloDto);
        assertThat(emailByPriority).contains("arpa2@kuutio.fi");
    }


}
