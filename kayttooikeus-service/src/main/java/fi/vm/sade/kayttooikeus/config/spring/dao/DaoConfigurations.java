package fi.vm.sade.kayttooikeus.config.spring.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
            p.putAll(this.values);
            return p;
        }
    }
}
