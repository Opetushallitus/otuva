package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.config.ExposedResourceMessageBundleSource;
import fi.vm.sade.kayttooikeus.service.external.KoodistoClient;
import fi.vm.sade.kayttooikeus.service.external.KoodistoClient.KoodiArvoDto;
import fi.vm.sade.kayttooikeus.service.external.KoodistoClient.KoodiMetadataDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/l10n")
public class LocalizationController {
    private static final Locale FI = new Locale("fi");
    private static final Locale SV = new Locale("sv");
    private static final Locale EN = new Locale("en");
    private static final Supplier<Stream<Locale>> LOCALES = () -> Stream.of(FI,SV,EN);
    private final ExposedResourceMessageBundleSource messageSource;
    private final KoodistoClient koodistoClient;

    @Autowired
    public LocalizationController(ExposedResourceMessageBundleSource messageSource, KoodistoClient koodistoClient) {
        this.messageSource = messageSource;
        this.koodistoClient = koodistoClient;
    }

    @RequestMapping(method = GET)
    public Map<String,Map<String,String>> list() {
        return LOCALES.get().collect(toMap(locale -> locale.getLanguage().toLowerCase(),
            locale -> messageSource.getMessages(locale).entrySet().stream()
                .collect(toMap(e -> e.getKey().toString().toUpperCase(),
                        e -> e.getValue().toString()))));
    }
    
    @RequestMapping(value = "/languages")
    public List<LanguageCode> languages() {
        Map<String,KoodiArvoDto> koodistoKoodisByLocale = koodistoClient.listKoodisto("kieli").stream()
                        .collect(toMap(arvo -> arvo.getKoodiArvo().toLowerCase(), identity()));
        return LOCALES.get().map(l -> new LanguageCode(l.getLanguage(), 
                koodistoKoodisByLocale.get(l.getLanguage())
                    .getMetadata().stream().collect(toMap(meta -> meta.getKieli().toLowerCase(), KoodiMetadataDto::getNimi))
            )).collect(toList());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class LanguageCode {
        private String code;
        private Map<String,String> name;
    }
}
