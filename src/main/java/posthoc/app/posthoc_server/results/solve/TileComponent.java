package posthoc.app.posthoc_server.results.solve;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TileComponent {
    @JsonProperty("$")
    public String type;

    public int width;
    public int height;
    public String x;
    public String y;
    public String fill;

    @JsonProperty("$if")
    public String condition;
}
