package fi.vm.sade.kayttooikeus.config.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Configuration
public class UrlConfiguration extends OphProperties {
    private final ObjectMapper objectMapper;

    @Autowired
    public UrlConfiguration(Environment environment, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        addFiles("/kayttooikeus-service-oph.properties");
        addOptionalFiles(environment.getProperty("spring.config.location"));
    }

    @Override
    // The default implementation wont resolve variables, e.g. ${url-virkailija}
    public String frontPropertiesToJson() {
        Map<String,String> results = new HashMap<>();
        Pattern var = Pattern.compile(".*(\\$\\{(.*?)\\}).*");
        for (Entry<Object,Object> kv : frontProperties.entrySet()) {
            String val = kv.getValue().toString();
            Matcher m = var.matcher(val);
            while (m.matches()) {
                val = val.replace(m.group(1), getProperty(m.group(2)));
                m = var.matcher(val);
            }
            results.put(kv.getKey().toString(), val);
        }
        try {
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
