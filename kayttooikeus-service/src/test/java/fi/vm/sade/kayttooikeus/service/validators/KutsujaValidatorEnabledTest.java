package fi.vm.sade.kayttooikeus.service.validators;

import fi.vm.sade.kayttooikeus.config.KutsujaValidatorConfiguration;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("yksilointiNotDisabled")
@SpringJUnitConfig(classes = {KutsujaValidatorConfiguration.class})
public class KutsujaValidatorEnabledTest {

    @MockitoBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Autowired
    private KutsujaValidator kutsujaValidator;

    @Test
    public void tarkistaaHetunJaYksiloinnin() {
        HenkiloDto henkilo = new HenkiloDto();
        henkilo.setHetu("123456-7890");
        henkilo.setYksiloityVTJ(true);
        when(oppijanumerorekisteriClient.getHenkiloByOid(anyString())).thenReturn(henkilo);
        assertTrue(kutsujaValidator.isKutsujaYksiloity("1.23.456.7890"));
        verify(oppijanumerorekisteriClient).getHenkiloByOid(anyString());
    }

}
