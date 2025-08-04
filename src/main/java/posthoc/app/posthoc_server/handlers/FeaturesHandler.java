package posthoc.app.posthoc_server.handlers;

import java.util.List;

import posthoc.app.posthoc_server.params.FeatureAlgorithmParams;
import posthoc.app.posthoc_server.results.FeatureAlgorithmResult;

public class FeaturesHandler {
    public static List<FeatureAlgorithmResult> getAlgorithms(FeatureAlgorithmParams params) {
        return List.of(
            new FeatureAlgorithmResult("wastar", "WAStar", "Weighted A-Star"),
            new FeatureAlgorithmResult("idastar", "IDAStar", "Iterative Deepening A-Star"),
            new FeatureAlgorithmResult("lwastar", "LazyWAStar", "Lazy Weighted A-Star"),
            new FeatureAlgorithmResult("ehs", "EHS", "Enhanced Harmony Search")
        );
    }
}
