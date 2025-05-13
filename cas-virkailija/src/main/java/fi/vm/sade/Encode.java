package fi.vm.sade;

import org.springframework.web.util.UriComponentsBuilder;

public class Encode {
    public static String uri(String httpUrl) {
        return UriComponentsBuilder.fromUriString(httpUrl).build().toUriString();
    }
}