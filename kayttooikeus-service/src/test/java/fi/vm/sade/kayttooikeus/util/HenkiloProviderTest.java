package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HenkiloProviderTest {

    private HenkiloProvider provider;

    @Mock
    private OppijanumerorekisteriClient oppijanumerorekisteriClientMock;

    @Before
    public void setup() {
        provider = new HenkiloProvider(oppijanumerorekisteriClientMock);

        when(oppijanumerorekisteriClientMock.getHenkiloByOid(any())).thenAnswer((InvocationOnMock invocation) -> {
            String oid = invocation.getArgument(0);
            return HenkiloDto.builder().oidHenkilo(oid).build();
        });
    }

    @Test
    public void getByOidShouldCache() {
        HenkiloDto dto1 = provider.getByOid("oid1");
        HenkiloDto dto2 = provider.getByOid("oid1");
        assertThat(dto2).isSameAs(dto1);
        verify(oppijanumerorekisteriClientMock).getHenkiloByOid(eq("oid1"));
        HenkiloDto dto3 = provider.getByOid("oid2");
        assertThat(dto3).isNotSameAs(dto1);
        verify(oppijanumerorekisteriClientMock).getHenkiloByOid(eq("oid2"));
    }

}
