package fi.vm.sade.kayttooikeus.service.impl;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import fi.vm.sade.kayttooikeus.dto.GoogleAuthSetupDto;
import fi.vm.sade.kayttooikeus.model.GoogleAuthToken;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.repositories.GoogleAuthTokenRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MfaServiceImplTest {
    private MfaServiceImpl mfaServiceImpl;

    private SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private QrDataFactory qrDataFactory = new QrDataFactory(HashingAlgorithm.SHA1, 6, 30);
    private QrGenerator qrGenerator = new ZxingPngQrGenerator();

    @Mock
    private PermissionCheckerService permissionCheckerService;
    @Mock
    private HenkiloDataRepository henkiloDataRepository;
    @Mock
    private KayttajatiedotRepository kayttajatiedotRepository;
    @Mock
    private GoogleAuthTokenRepository googleAuthTokenRepository;

    private Kayttajatiedot kayttajatiedot = Kayttajatiedot.builder().build();
    private Henkilo henkilo = Henkilo.builder().kayttajatiedot(kayttajatiedot).build();
    private GoogleAuthToken token = new GoogleAuthToken(1, henkilo, "SECRETKEY1SECRETKEY1SECRETKEY123", null);

    @Before
    public void setup() {
        mfaServiceImpl = new MfaServiceImpl(
            secretGenerator,
            qrDataFactory,
            qrGenerator,
            permissionCheckerService,
            henkiloDataRepository,
            kayttajatiedotRepository,
            googleAuthTokenRepository
        );
    }

    @Test
    public void setupGoogleAuthUsesExistingGauthToken() {
        when(permissionCheckerService.getCurrentUserOid()).thenReturn("1.2.3.4.5");
        when(kayttajatiedotRepository.findGoogleAuthToken(any())).thenReturn(Optional.of(token));
        when(henkiloDataRepository.findByOidHenkilo(any())).thenReturn(Optional.of(henkilo));

        GoogleAuthSetupDto dto = mfaServiceImpl.setupGoogleAuth();

        verify(googleAuthTokenRepository, times(0)).save(any());
        assertThat(dto.getSecretKey(), equalTo(token.getSecretKey()));
        assertThat(dto.getQrCodeDataUri(), startsWith("data:image/png;base64,"));
    }

    @Test
    public void setupGoogleAuthCreatesNewGauthToken() {
        when(permissionCheckerService.getCurrentUserOid()).thenReturn("1.2.3.4.5");
        when(kayttajatiedotRepository.findGoogleAuthToken(any())).thenReturn(Optional.empty());
        when(henkiloDataRepository.findByOidHenkilo(any())).thenReturn(Optional.of(henkilo));

        GoogleAuthSetupDto dto = mfaServiceImpl.setupGoogleAuth();

        verify(googleAuthTokenRepository, times(1)).save(any());
        assertThat(dto.getSecretKey(), matchesPattern("^[A-Z0-9]{32}$"));
        assertThat(dto.getQrCodeDataUri(), startsWith("data:image/png;base64,"));
    }
}
