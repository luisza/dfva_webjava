package dfva_webjava.java.demo;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentSign {
 
    private Map<String, String> properties ;
 
    @JsonAnySetter
    public void add(String key, String value) {
    	if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, String> getValues() {
        return properties;
    }
	

}
