package posthoc.app.posthoc_server.handlers;

import java.util.List;

import posthoc.app.posthoc_server.params.FeatureAlgorithmParams;
import posthoc.app.posthoc_server.params.FeatureFormatParams;
import posthoc.app.posthoc_server.params.FeatureProblemTypeParams;
import posthoc.app.posthoc_server.results.FeatureAlgorithmResult;
import posthoc.app.posthoc_server.results.FeatureFormatResult;
import posthoc.app.posthoc_server.results.FeatureProblemTypeResult;

public class FeaturesHandler {
    public static List<FeatureAlgorithmResult> getAlgorithms(FeatureAlgorithmParams params) {
        return List.of(
                new FeatureAlgorithmResult("wastar", "WAStar", "Weighted A-Star"),
                new FeatureAlgorithmResult("idastar", "IDAStar", "Iterative Deepening A-Star"),
                new FeatureAlgorithmResult("lwastar", "LazyWAStar", "Lazy Weighted A-Star"),
                new FeatureAlgorithmResult("ehs", "EHS", "Enhanced Harmony Search"));
    }

    public static List<FeatureFormatResult> getFormats(FeatureFormatParams params) {
        return List.of(
                new FeatureFormatResult("grid", "Grid", "grid"),
                new FeatureFormatResult("xy", "Xy", "xy"));
    }

    public static List<FeatureProblemTypeResult> getProblemTypes(FeatureProblemTypeParams params) {
        return List.of(
                new FeatureProblemTypeResult("pathfinding", "Pathfinding", "Pathfinding problems"),
                new FeatureProblemTypeResult("plant-watering", "Plant Watering", "Plant Watering problems"));
    }
}
