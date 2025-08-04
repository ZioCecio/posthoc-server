package posthoc.app.posthoc_server.params;

import java.util.List;

class SolveInstance {
    public int start;
    public int end;
}

public class SolveParams {
    public String format;
    public List<SolveInstance> instances;
    public String mapURI;
    public String algorithm;
}
