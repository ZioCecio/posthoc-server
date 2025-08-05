package posthoc.app.posthoc_server.results.solve;

public class Event {
    public String type;
    public long id;
    public int x;
    public int y;
    public Long pId;
    public Double g;
    public Integer f;

    public Event(String type, long id, int x, int y, Long pId, Double g, Integer f) {
        this.type = type;
        this.id = id;
        this.x = x;
        this.y = y;
        this.pId = pId;
        this.g = g;
        this.f = f;
    }
}
