package fi.vm.sade.kayttooikeus.service.impl;

import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import fi.vm.sade.kayttooikeus.dto.GoogleAuthSetupDto;
import fi.vm.sade.kayttooikeus.dto.MfaProvider;
import fi.vm.sade.kayttooikeus.model.GoogleAuthToken;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.repositories.GoogleAuthTokenRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.service.MfaService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@Transactional
@RequiredArgsConstructor
public class MfaServiceImpl implements MfaService {
    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final PermissionCheckerService permissionCheckerService;
    private final HenkiloDataRepository henkiloDataRepository;
    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final GoogleAuthTokenRepository googleAuthTokenRepository;

    private String getNewGoogleAuthSecretKey(Henkilo henkilo) {
      String secretKey = secretGenerator.generate();
      GoogleAuthToken token = new GoogleAuthToken(null, henkilo, secretKey, null);
      googleAuthTokenRepository.save(token);
      return secretKey;
    }
  
    @Override
    public GoogleAuthSetupDto setupGoogleAuth() {
        String currentUserOid = permissionCheckerService.getCurrentUserOid();
        Henkilo currentUser = henkiloDataRepository.findByOidHenkilo(currentUserOid)
            .orElseThrow(() -> new IllegalStateException(String.format("Käyttäjää %s ei löydy", currentUserOid)));
        String username = currentUser.getKayttajatiedot().getUsername();

        String secretKey = kayttajatiedotRepository
            .findGoogleAuthToken(username)
            .map((token) -> token.getSecretKey())
            .orElseGet(() -> getNewGoogleAuthSecretKey(currentUser));

        QrData data = qrDataFactory.newBuilder()
            .label("Opintopolku:" + username)
            .secret(secretKey)
            .issuer("Opetushallitus")
            .build();

        try {
            String qrCodeDataUri = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
            return new GoogleAuthSetupDto(secretKey, qrCodeDataUri);
        } catch (QrGenerationException qre) {
            throw new RuntimeException("Failed to generate QR data", qre);
        }
    }
}
