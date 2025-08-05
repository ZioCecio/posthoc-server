package posthoc.app.posthoc_server.results.solve;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ComponentReference {
    @JsonProperty("$")
    public String ref;
}
