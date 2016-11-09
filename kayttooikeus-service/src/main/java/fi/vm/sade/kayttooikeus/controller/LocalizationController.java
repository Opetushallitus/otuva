package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.config.ExposedResourceMessageBundleSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/l10n")
public class LocalizationController {
    private static final Locale FI = new Locale("fi");
    private static final Locale SV = new Locale("sv");
    private static final Locale EN = new Locale("en");
    
    @Autowired
    private ExposedResourceMessageBundleSource messageSource;

    @RequestMapping(method = GET)
    public Map<String,Map<String,String>> list() {
        //TODO: call localization service and use these ase base values:
        return Stream.of(FI,SV,EN).collect(toMap(locale -> locale.getLanguage().toLowerCase(),
            locale -> messageSource.getMessages(locale).entrySet().stream()
                .collect(toMap(e -> e.getKey().toString().toUpperCase(),
                        e -> e.getValue().toString()))));
    }
}
