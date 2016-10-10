package fi.vm.sade.kayttooikeus.config.dao;

import lombok.Getter;
import lombok.experimental.Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import static java.util.stream.Collectors.toMap;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 16.10
 */
@Getter @Builder
public class DaoConfigurations {
    private final boolean useInMemoryDb;
    private final boolean useFlyway;
    private final Properties jpaProperties;
    private final Properties mainDataSourceProperties;
    private final Properties memoryDataSourceProperties;
    
    
    public static class PropertiesBuilder {
        private final Map<String,Object> values = new HashMap<>();
        
        public static PropertiesBuilder builder() {
            return new PropertiesBuilder();
        }
        
        public PropertiesBuilder put(String key, Object value) {
            this.values.put(key, value);
            return this;
        }
        
        public Properties build() {
            Properties p = new Properties();
            p.putAll(this.values.entrySet().stream().filter(e -> e.getValue() != null)
                    .collect(toMap(Entry::getKey, Entry::getValue)));
            return p;
        }
    }
}
