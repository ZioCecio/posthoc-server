package posthoc.app.posthoc_server.params;

public class SolveInstance {
    public Integer start;
    public Integer end;
    public Integer[] plants;
    public Integer[] taps;
    public Integer[] pourAmounts;

    public String toString() {
        return "SolveInstance{" +
                "start=" + start +
                ", end=" + end +
                ", plants=" + java.util.Arrays.toString(plants) +
                ", taps=" + java.util.Arrays.toString(taps) +
                ", pourAmounts=" + java.util.Arrays.toString(pourAmounts) +
                '}';
    }
}
