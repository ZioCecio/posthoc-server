package posthoc.app.posthoc_server.results.solve;

import java.util.UUID;

public class Event {
    public String type;
    public UUID id;
    public int x;
    public int y;
    public UUID pId;
    public Double g;
    public Integer f;

    public Event(String type, UUID id, int x, int y, UUID pId, Double g, Integer f) {
        this.type = type;
        this.id = id;
        this.x = x;
        this.y = y;
        this.pId = pId;
        this.g = g;
        this.f = f;
    }
}
