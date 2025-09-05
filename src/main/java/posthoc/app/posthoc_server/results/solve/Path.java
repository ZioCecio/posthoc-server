package posthoc.app.posthoc_server.results.solve;

public class Path {
    public Pivot pivot;
    public double scale;

    public Path(Pivot pivot, double scale) {
        this.pivot = pivot;
        this.scale = scale;
    }
}
