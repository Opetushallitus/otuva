package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.authentication.business.exception.IdentificationExpiredException;
import fi.vm.sade.kayttooikeus.config.properties.AuthProperties;
import fi.vm.sade.kayttooikeus.dto.AccessRightTypeDto;
import fi.vm.sade.kayttooikeus.dto.GroupTypeDto;
import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.IdentifiedHenkiloTypeDto;
import fi.vm.sade.kayttooikeus.model.Identification;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.repositories.IdentificationRepository;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.IdentificationPopulator.identification;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttajatiedotPopulator.kayttajatiedot;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.PalveluPopulator.palvelu;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class IdentificationServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private IdentificationService identificationService;

    @Autowired
    private IdentificationRepository identificationRepository;

    @MockBean
    private AuthProperties authProperties;

    @MockBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void generateAuthTokenForHenkiloNotFoundTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("no henkilo found with oid:[1.1.1.1.1]");
        identificationService.generateAuthTokenForHenkilo("1.1.1.1.1", "key", "identifier");
    }

    @Test
    public void generateAuthTokenForHenkiloTest() {
        populate(henkilo("1.2.3.4.5"));
        populate(henkilo("1.2.3.4.6"));
        given(authProperties.getExpirationMonths()).willReturn(24);

        String token = identificationService.generateAuthTokenForHenkilo("1.2.3.4.5",
                "key", "identifier");
        assertTrue(token.length() > 20);
        Optional<Identification> identification = identificationRepository.findByAuthtoken(token);
        assertTrue(identification.isPresent());
        assertEquals("identifier", identification.get().getIdentifier());
        assertEquals("key", identification.get().getIdpEntityId());
        assertEquals("1.2.3.4.5", identification.get().getHenkilo().getOidHenkilo());

        // expiration date should be set for haka
        token = identificationService.generateAuthTokenForHenkilo("1.2.3.4.6", "haka", "hakaidentifier");
        assertTrue(token.length() > 20);
        identification = identificationRepository.findByAuthtoken(token);
        assertTrue(identification.isPresent());
        assertTrue(identification.get().getExpirationDate().after(new DateTime().plusMonths(23).toDate()));
        assertTrue(identification.get().getExpirationDate().before(new DateTime().plusMonths(25).toDate()));
        assertEquals("hakaidentifier", identification.get().getIdentifier());
        assertEquals("haka", identification.get().getIdpEntityId());
        assertEquals("1.2.3.4.6", identification.get().getHenkilo().getOidHenkilo());
    }

    @Test
    public void generateAuthTokenExpiredTest() throws Exception {
        populate(identification("haka", "identifier", henkilo("1.2.3.4.6"))
                .withExpirationDate(new LocalDate().minusMonths(1).toDate()));

        thrown.expect(IdentificationExpiredException.class);
        thrown.expectMessage("Haka identification expired");
        identificationService.generateAuthTokenForHenkilo("1.2.3.4.6", "haka", "identifier");
    }

    @Test(expected = NotFoundException.class)
    public void getHenkiloOidByIdpAndIdentifierNotFoundTest() throws Exception {
        identificationService.getHenkiloOidByIdpAndIdentifier("haka", "identifier");
    }

    @Test
    public void getHenkiloOidByIdpAndIdentifierTest() throws Exception {
        populate(identification("haka", "identifier", henkilo("1.2.3.4.5")));
        String oid = identificationService.getHenkiloOidByIdpAndIdentifier("haka", "identifier");
        assertEquals(oid, "1.2.3.4.5");
    }

    @Test
    public void validateAuthTokenTest() throws Exception {
        Identification identification = populate(identification("haka", "identifier",
                henkilo("1.2.3.4.5")).withAuthToken("12345"));

        Kayttajatiedot kayttajatiedot = new Kayttajatiedot();
        kayttajatiedot.setHenkilo(identification.getHenkilo());
        kayttajatiedot.setUsername("hakakäyttäjä");
        identification.getHenkilo().setKayttajatiedot(kayttajatiedot);

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus(palvelu("KOODISTO"), "READ"))));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "4.5.6.7.8"),
                kayttoOikeusRyhma("RYHMA2").withOikeus(oikeus("KOODISTO", "CRUD"))));

        IdentifiedHenkiloTypeDto dto = identificationService.validateAuthToken("12345");
        assertEquals("1.2.3.4.5", dto.getOidHenkilo());
        assertEquals(HenkiloTyyppi.VIRKAILIJA, dto.getHenkiloTyyppi());
        assertEquals("haka", dto.getIdpEntityId());
        assertEquals("identifier", dto.getIdentifier());
        assertEquals("hakakäyttäjä", dto.getKayttajatiedot().getUsername());
        assertFalse(dto.isPassivoitu());

        List<AccessRightTypeDto> accessRights = dto.getAuthorizationData().getAccessrights().getAccessRight();
        assertEquals(3, accessRights.size());

        List<AccessRightTypeDto> rights = accessRights.stream().filter(right -> right.getPalvelu().equals("HENKILOHALLINTA")).collect(toList());
        assertEquals(1, rights.size());
        assertEquals("CRUD", rights.get(0).getRooli());
        assertEquals("3.4.5.6.7", rights.get(0).getOrganisaatioOid());
        rights = accessRights.stream().filter(right -> right.getPalvelu().equals("KOODISTO")).collect(toList());
        assertEquals(2, rights.size());
        assertTrue(rights.stream().map(AccessRightTypeDto::getRooli).collect(toList()).containsAll(Arrays.asList("READ", "CRUD")));
        assertTrue(rights.stream().map(AccessRightTypeDto::getOrganisaatioOid).collect(toList()).containsAll(Arrays.asList("3.4.5.6.7", "4.5.6.7.8")));

        List<GroupTypeDto> groups = dto.getAuthorizationData().getGroups().getGroup();
        assertEquals(2, groups.size());
        List<GroupTypeDto> group = groups.stream().filter(groupType -> groupType.getNimi().equals("RYHMA")).collect(toList());
        assertEquals(1, group.size());
        assertEquals("3.4.5.6.7", group.get(0).getOrganisaatioOid());
        group = groups.stream().filter(groupType -> groupType.getNimi().equals("RYHMA2")).collect(toList());
        assertEquals(1, group.size());
        assertEquals("4.5.6.7.8", group.get(0).getOrganisaatioOid());

    }

    @Test(expected = NotFoundException.class)
    public void validateAuthTokenNotFoundTest() {
        identificationService.validateAuthToken("12345");
    }

    @Test(expected = NotFoundException.class)
    public void validateAuthTokenUsedTest() {
        populate(identification("haka", "identifier", henkilo("1.2.3.4.5")).withAuthToken("12345"));
        identificationService.validateAuthToken("12345");
        identificationService.validateAuthToken("12345");
    }

    @Test
    public void generateTokenWithHetuEmptyTest() throws Exception {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("query.hetu.invalid");
        identificationService.generateTokenWithHetu("");
    }

    @Test
    public void generateTokenWithHetuHenkiloNotFoundTest() throws Exception {
        given(oppijanumerorekisteriClient.findByHetu("220294-7132"))
                .willReturn(HenkiloPerustietoDto.builder()
                        .oidHenkilo("1.2.3.4.5").build());

        thrown.expect(NotFoundException.class);
        thrown.expectMessage("henkilo not found");
        identificationService.generateTokenWithHetu("220294-7132");
    }

    @Test
    public void generateTokenWithHetuTest() throws Exception {
        populate(kayttajatiedot(henkilo("1.2.3.4.5"), "user1"));
        given(oppijanumerorekisteriClient.findByHetu("220294-7132"))
                .willReturn(HenkiloPerustietoDto.builder()
                        .oidHenkilo("1.2.3.4.5").build());

        //create new
        String token = identificationService.generateTokenWithHetu("220294-7132");
        assertTrue(token.length() > 20);
        Optional<Identification> identification = identificationRepository.findByAuthtoken(token);
        assertTrue(identification.isPresent());
        assertEquals("vetuma", identification.get().getIdpEntityId());
        assertEquals("user1", identification.get().getIdentifier());
        Long id = identification.get().getId();

        //update old
        token = identificationService.generateTokenWithHetu("220294-7132");
        assertTrue(token.length() > 20);
        identification = identificationRepository.findByAuthtoken(token);
        assertTrue(identification.isPresent());
        assertEquals("vetuma", identification.get().getIdpEntityId());
        assertEquals("user1", identification.get().getIdentifier());
        assertEquals(id, identification.get().getId());
    }
}
