package posthoc.app.posthoc_server.handlers;

import posthoc.app.posthoc_server.params.AboutParams;
import posthoc.app.posthoc_server.results.AboutResult;

public class AboutHandler {
    public static AboutResult getInfo(AboutParams params) {
        return new AboutResult("ENHSP", "1.0.2", "Description");
    }    
}
