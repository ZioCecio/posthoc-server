package posthoc.app.posthoc_server.params;

import java.net.URLDecoder;
import java.util.List;

public class SolveParams {
    public String format;
    public List<SolveInstance> instances;
    public String mapURI;
    public String algorithm;

    public String toString() {
        return "SolveParams{" +
                "format='" + format + '\'' +
                ", instances=" + instances +
                ", mapURI='" + mapURI + '\'' +
                ", algorithm='" + algorithm + '\'' +
                '}';
    }

    public int getMapLen() {
        String decodedMapURI = URLDecoder.decode(mapURI, java.nio.charset.StandardCharsets.UTF_8);
        return decodedMapURI.split("\n").length - 4; // the first 4 lines are headers
    }
}
